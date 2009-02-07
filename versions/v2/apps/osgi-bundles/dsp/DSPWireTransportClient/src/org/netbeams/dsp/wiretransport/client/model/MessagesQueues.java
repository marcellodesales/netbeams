package org.netbeams.dsp.wiretransport.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;

/**
 * MessagesQueues holds outbound messages to be sent to a remote DSP instance.
 * 
 * @author marcello
 * 
 */
public enum MessagesQueues {

    /**
     * Singleton for the messages Queues
     */
    INSTANCE;

    private static final Logger log = Logger.getLogger(MessagesQueues.class);

    static {
        log.info("Initializing the local Messages Queues");
    }

    /**
     * This is the hash of the IP address of the destination and the Queue of the DirectoryData of the outbound
     * messages.
     */
    private Map<String, Queue<QueueMessageData>> outboundQueue;

    /**
     * Constructs a new MessagesQueues.
     */
    private MessagesQueues() {
        this.outboundQueue = new LinkedHashMap<String, Queue<QueueMessageData>>();
    }

    /**
     * @return the default implementation of the queue
     */
    private Queue<QueueMessageData> makeMessageQueue() {
        return new LinkedList<QueueMessageData>();
    }

    /**
     * Adds new values from a given producer reference.
     * 
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public synchronized void addMessageToOutboundQueue(Message dspMessage) {
        String destinationIp = dspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue();
        Queue<QueueMessageData> outMsgs = this.outboundQueue.get(destinationIp);
        if (outMsgs == null) {
            outMsgs = this.makeMessageQueue();
        }
        outMsgs.add(QueueMessageData.makeNewInstance(dspMessage));
        this.outboundQueue.put(destinationIp, outMsgs);
    }

    /**
     * @param destinitionIp is the IP address of the destination component.
     * @return a new instance of MessagesContainer of the current messages that are on the QUEUED state for the given
     *         component destination.
     */
    public synchronized MessagesContainer retrieveQueuedMessagesForTransmission(String destinitionIp) {
        MessagesContainer container = DSPMessagesFactory.INSTANCE.makeDSPMessagesContainer(destinitionIp);
        log.debug("The Messages queue is being queried for the ip address " + destinitionIp);
        Queue<QueueMessageData> outboutQueue = this.outboundQueue.get(destinitionIp);
        if (outboutQueue != null) {
            log.debug("The size of the messages in the outbound queue is " + outboutQueue.size());

            for (QueueMessageData data : outboutQueue) {
                if (data.getState().equals(QueueMessageState.QUEUED)) {
                    container.getMessage().add(data.getMessage());
                    data.setMessagesContainerId(UUID.fromString(container.getUudi()));
                }
            }
        }
        return container;
    }

    /**
     * @return an array of MessagesContainer for each IP address for different DSP components.
     */
    public synchronized MessagesContainer[] retrieveAllQueuedMessagesForTransmission() {
        Set<String> ipAddresses = this.outboundQueue.keySet();
        MessagesContainer[] msgContainers = new MessagesContainer[ipAddresses.size()];
        int i = -1;
        for (String ipAddr : ipAddresses) {
            msgContainers[++i] = this.retrieveQueuedMessagesForTransmission(ipAddr);
        }
        return msgContainers;
    }

    /**
     * @param componentDestinition is the address of the component
     * @param containerId is the identification of the MessagesContainer created during before the transmission
     */
    public synchronized void setMessagesToTransmitted(String destinationIpAddress, UUID containerId) {
        for (QueueMessageData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(QueueMessageState.QUEUED) && data.getContainerId().equals(containerId)) {
                data.changeStateToTransmitted();
            }
        }
    }

    /**
     * @param componentDestinition is the address of the destination of the component.
     * @return the current list of messages on the TRANSMITTED state. The ones that were tried to be sent to the server.
     */
    public synchronized List<Message> retrieveTransmittedMessages(String destinationIpAddress) {
        List<Message> messages = new ArrayList<Message>();
        for (QueueMessageData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(QueueMessageState.TRANSMITTED)) {
                messages.add(data.getMessage());
            }
        }
        return messages;
    }

    /**
     * @param componentDestinition is the component destination.
     * @param containerId is the identification of the container.
     */
    public synchronized void setMessagesToAcknowledged(String destinationIpAddress, UUID containerId) {
        for (QueueMessageData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(QueueMessageState.QUEUED) && data.getContainerId().equals(containerId)) {
                data.changeStateToAcknowledged();
            }
        }
    }

    /**
     * @param componentDestinition is the component URL for the messages.
     * @return the current list of the messages on the state ACKNOWLEDGED.
     */
    public synchronized List<Message> retrieveAcknowledgedMessages(String destinationIpAddress) {
        List<Message> messages = new ArrayList<Message>();
        for (QueueMessageData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(QueueMessageState.ACKNOWLEDGED)) {
                messages.add(data.getMessage());
            }
        }
        return messages;
    }
    //    
    // public void deliver(Message message) throws DSPException {
    // log.debug("message class=" + message.getClass().getName());
    //
    // if (message instanceof AcknowledgementMessage) {
    //            
    // String messagesContainerUUID = (String) message.getHeader().getCorrelationID();
    // String messageIpAddr = message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue();
    // this.setMessagesToAcknowledged(messageIpAddr, UUID.fromString(messagesContainerUUID));
    // }
    // }
}
