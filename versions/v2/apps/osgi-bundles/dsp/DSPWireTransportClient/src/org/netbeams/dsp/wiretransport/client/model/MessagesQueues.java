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
    
    public Map<String, Queue<QueueMessageData>> getOutboundQueues() {
        return this.outboundQueue;
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
        // Adding the message ID to be a sequential number for a given IP address. This will follow the
        // window slide protocol where a certain number of messages are acknowledged based by the highest number.
        outMsgs.add(QueueMessageData.makeNewInstance(outMsgs.size() + 1, dspMessage));
        this.outboundQueue.put(destinationIp, outMsgs);
    }

    /**
     * @param destinitionIp is the IP address of the destination component.
     * @return a new instance of MessagesContainer of the current messages that are on the QUEUED state for the given
     *         component destination.
     */
    public synchronized MessagesContainer retrieveQueuedMessagesForTransmission(String destinationIp) {
        log.debug("Retrieving all queued messages for the destination IP " + destinationIp);
        return this.retrieveAllMessagesByIpAndState(destinationIp, QueueMessageState.QUEUED);
    }
    
    /**
     * Retrieves all messages for a given IP address no matter what state it is.
     * @param destinationIp the given IP address.
     * @return a new instance of Messages container of the current messages for a given IP no matter which state.
     */
    private synchronized MessagesContainer retrieveAllMessagesOnClientForIp(String destinationIp) {
        log.debug("Retrieving all messages registed on the running client for the destination IP " + destinationIp);
        return this.retrieveAllMessagesByIpAndState(destinationIp, null);
    }
    
    /**
     * Retrieves all the messages for a given destination IP and state
     * @param destinationIp is required for the lookup search.
     * @param state if null, all messages in any state is retrieved
     * @return a messages container with the messages that satisfy the given parameters.
     */
    public synchronized MessagesContainer retrieveAllMessagesByIpAndState(String destinationIp, QueueMessageState state) {
        MessagesContainer container = DSPMessagesFactory.INSTANCE.makeDSPMessagesContainer(destinationIp);
        Queue<QueueMessageData> outboutQueue = this.outboundQueue.get(destinationIp);
        int largestSequenceNumber = 0;
        if (outboutQueue != null) {
            log.debug("The size of the messages in the outbound queue is " + outboutQueue.size());
            for (QueueMessageData data : outboutQueue) {
                largestSequenceNumber = data.getSequenceNumber() > largestSequenceNumber ? data.getSequenceNumber() : 
                                                                                           largestSequenceNumber;
                if (state != null) {
                    //if the state is given, then it's restricted by it...
                    if (data.getState().equals(state)) {
                        container.getMessage().add(data.getMessage());
                    }
                } else { //if the state is null, just get anything...
                    container.getMessage().add(data.getMessage());
                }
                data.setMessagesContainerId(UUID.fromString(container.getUudi()));
            }
            
        } else log.debug("There are no messages on the outbound queue for the client IP " + destinationIp);
        container.setWindowSize(largestSequenceNumber);
        return container;   
    }
    
    /**
     * @return an array of MessagesContainer for each IP address for different DSP components.
     */
    public synchronized MessagesContainer[] retrieveAllMessagesOnClient() {
        log.debug("Retrieving all messages registered on this client, no matter the state of the messages...");
        Set<String> ipAddresses = this.outboundQueue.keySet();
        MessagesContainer[] msgContainers = new MessagesContainer[ipAddresses.size()];
        int i = -1;
        for (String ipAddr : ipAddresses) {
            log.debug("The Messages queue is being queried for the ip address " + ipAddr);
            msgContainers[++i] = this.retrieveAllMessagesOnClientForIp(ipAddr);
        }
        return msgContainers;
    }

    /**
     * @return an array of MessagesContainer for each IP address for different DSP components.
     */
    public synchronized MessagesContainer[] retrieveAllQueuedMessagesForTransmission() {
        Set<String> ipAddresses = this.outboundQueue.keySet();
        MessagesContainer[] msgContainers = new MessagesContainer[ipAddresses.size()];
        int i = -1;
        for (String ipAddr : ipAddresses) {
            log.debug("The Messages queue is being queried for the ip address " + ipAddr);
            msgContainers[++i] = this.retrieveQueuedMessagesForTransmission(ipAddr);
        }
        return msgContainers;
    }

    /**
     * @param componentDestinition is the component destination.
     * @param maxMessageId is the highest message id from the acknowledgment frame that was received.
     */
    public synchronized void setMessagesToAcknowledged(String destinationIpAddress, int maxMessageId) {
        log.debug("===> Acknowledging messages up to message ID " + maxMessageId);
        for (QueueMessageData data : this.outboundQueue.get(destinationIpAddress)) {
            log.debug("Traying to acknowledge message ID =>" + data.getMessage().getMessageID());
            if (data.getState().equals(QueueMessageState.QUEUED)
                    && Integer.valueOf(data.getMessage().getMessageID()) <= maxMessageId) {
                data.changeStateToAcknowledged();
                log.debug("Message Acknowledged =>" + data.getMessage().getMessageID());
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
}
