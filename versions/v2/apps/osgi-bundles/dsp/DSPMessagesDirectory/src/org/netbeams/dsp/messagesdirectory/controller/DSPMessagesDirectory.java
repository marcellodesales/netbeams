package org.netbeams.dsp.messagesdirectory.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.messagesdirectory.model.DirectoryData;
import org.netbeams.dsp.messagesdirectory.model.MessagesQueueState;

/**
 * The DSPMessagesDirectory represents the directory of messages being produced by DSP components.
 * 
 * All messages are indexed in a data structure called outbound Queue, that represents all the messages sorted by the
 * destination URL of the component. For each of them, a Queue of DirectoryData is used to hold information about a
 * given DSP Message, its state, the MessagesContainer UUDI and the URL itself.
 * 
 * The data is indexed to each producer, and the data is organized in a FIFO Queue.
 * 
 * More information at http://code.google.com/p/netbeams/issues/detail?id=24
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public enum DSPMessagesDirectory implements DSPComponent {

    /**
     * Singleton instance of the Messages Directory
     */
    INSTANCE;

    private static final Logger log = Logger.getLogger(DSPMessagesDirectory.class);

    /**
     * This is the hash of the IP address of the destination and the Queue of the DirectoryData of the 
     * outbound messages.
     */
    private Map<String, Queue<DirectoryData>> outboundQueue;

    public static final String COMPONENT_TYPE = "org.netbeams.dsp.messagesdirectory";
    private static ComponentDescriptor componentDescriptor;
    private String componentNodeId;
    private DSPContext context;

    private DSPMessagesDirectory() {
        this.outboundQueue = new LinkedHashMap<String, Queue<DirectoryData>>();
    }

    /**
     * @return the default implementation of the queue
     */
    private Queue<DirectoryData> makeMessageQueue() {
        return new LinkedList<DirectoryData>();
    }

    /**
     * Adds new values from a given producer reference.
     * 
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public synchronized void addMessageToOutboundQueue(Message dspMessage) {
        String destinationIp = dspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue();
        Queue<DirectoryData> outMsgs = this.outboundQueue.get(destinationIp);
        if (outMsgs == null) {
            outMsgs = this.makeMessageQueue();
        }
        outMsgs.add(DirectoryData.makeNewInstance(dspMessage));
        this.outboundQueue.put(destinationIp, outMsgs);
    }

    /**
     * @param destinitionIp is the ip address of the destination component.
     * @return a new instance of MessagesContainer of the current messages that are on the QUEUED state for the given
     *         component destination.
     */
    public synchronized MessagesContainer retrieveQueuedMessagesForTransmission(String destinitionIp) {
        MessagesContainer container = DSPMessagesFactory.INSTANCE.makeDSPMessagesContainer(destinitionIp);
        log.debug("The Messages queue is being queried for the ip address " + destinitionIp);
        Queue<DirectoryData> outboutQueue = this.outboundQueue.get(destinitionIp);
        if (outboutQueue != null) {
            log.debug("The size of the messages in the outbound queue is " + outboutQueue.size());
            
            for (DirectoryData data : outboutQueue) {
                if (data.getState().equals(MessagesQueueState.QUEUED)) {
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
        for (DirectoryData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(MessagesQueueState.QUEUED) && data.getContainerId().equals(containerId)) {
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
        for (DirectoryData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(MessagesQueueState.TRANSMITTED)) {
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
        for (DirectoryData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(MessagesQueueState.QUEUED) && data.getContainerId().equals(containerId)) {
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
        for (DirectoryData data : this.outboundQueue.get(destinationIpAddress)) {
            if (data.getState().equals(MessagesQueueState.ACKNOWLEDGED)) {
                messages.add(data.getMessage());
            }
        }
        return messages;
    }

    public void deliver(Message message) throws DSPException {
        log.debug("message class=" + message.getClass().getName());

        if (message instanceof AcknowledgementMessage) {
            
            String messagesContainerUUID = (String) message.getHeader().getCorrelationID();
            String messageIpAddr = message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue();
            this.setMessagesToAcknowledged(messageIpAddr, UUID.fromString(messagesContainerUUID));
        }
    }
    
    public Message deliverWithReply(Message message) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    public ComponentDescriptor getComponentDescriptor() {
        return componentDescriptor;
    }

    public void startComponent() throws DSPException {
        log.info("MessagesDirectory.startComponent()");
    }
    
    public void stopComponent() throws DSPException {
        log.info("MessagesDirectory.stopComponent()");
    }

    public String getComponentNodeId() {
        return this.componentNodeId;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        log.info("MessagesDirectory.initComponent()");
        this.context = context;
        this.componentNodeId = componentNodeId;
        log.info("Context: " + this.context.toString() + "  with component ID " + this.componentNodeId);
    }
}