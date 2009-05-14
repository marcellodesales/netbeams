package org.netbeams.dsp.wiretransport.client.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private Map<String, List<QueueMessageData>> outboundQueue;

    private Map<String, Integer> numberMessagesQueuedIndex;

    /**
     * Constructs a new MessagesQueues.
     */
    private MessagesQueues() {
        this.outboundQueue = Collections.synchronizedMap(new LinkedHashMap<String, List<QueueMessageData>>());
        this.numberMessagesQueuedIndex = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
    }

    public synchronized Map<String, List<QueueMessageData>> getOutboundQueues() {
        return this.outboundQueue;
    }

    /**
     * @return the default implementation of the queue
     */
    private List<QueueMessageData> makeMessageQueue() {
        return Collections.synchronizedList(new LinkedList<QueueMessageData>());
    }

    /**
     * Adds new values from a given producer reference.
     * 
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public synchronized void addMessageToOutboundQueue(Message dspMessage) {
        String destinationIp = dspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue();

        List<QueueMessageData> outMsgs = this.outboundQueue.get(destinationIp);
        if (outMsgs == null) {
            outMsgs = this.makeMessageQueue();
            this.numberMessagesQueuedIndex.put(destinationIp, 0);
        }

        int numberMsgQueuedForIp = this.numberMessagesQueuedIndex.get(destinationIp).intValue();
        this.numberMessagesQueuedIndex.put(destinationIp, new Integer(++numberMsgQueuedForIp));

        // Adding the message ID to be a sequential number for a given IP address. This will follow the
        // window slide protocol where a certain number of messages are acknowledged based by the highest number.
        outMsgs.add(QueueMessageData.makeNewInstance(numberMsgQueuedForIp, dspMessage));

        log.debug("Added message ID " + dspMessage.getMessageID() + " to the outboud queue for IP " + destinationIp);
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
     * Retrieves all the messages for a given destination IP and state
     * 
     * @param destinationIp is required for the lookup search.
     * @param state if null, all messages in any state is retrieved
     * @return a messages container with the messages that satisfy the given parameters.
     */
    public synchronized MessagesContainer retrieveAllMessagesByIpAndState(String destinationIp, QueueMessageState state) {
        MessagesContainer container = DSPMessagesFactory.INSTANCE.makeDSPMessagesContainer(destinationIp);
        List<QueueMessageData> outboutQueue = this.outboundQueue.get(destinationIp);
        int largestSequenceNumber = 0;
        if (outboutQueue != null) {
            int counter = 0;
            synchronized (outboutQueue) {
                for (QueueMessageData data : outboutQueue) {
                    largestSequenceNumber = data.getSequenceNumber() > largestSequenceNumber ? data.getSequenceNumber()
                            : largestSequenceNumber;
                    if (state != null) {
                        // if the state is given, then it's restricted by it...
                        if (data.getState().equals(state)) {
                            container.getMessage().add(data.getMessage());
                            ++counter;
                        }
                    } else { // if the state is null, just get anything...
                        container.getMessage().add(data.getMessage());
                        ++counter;
                    }
                    data.setMessagesContainerId(UUID.fromString(container.getUudi()));
                }
            }
            log.debug("The size of the messages in the outbound queue for the client IP " + destinationIp + " is "
                    + counter);

        } else
            log.debug("There are no messages on the outbound queue for the client IP " + destinationIp);
        container.setWindowSize(largestSequenceNumber);
        return container;
    }

    /**
     * @return an array of MessagesContainer for each IP address for different DSP components.
     */
    public synchronized MessagesContainer[] retrieveAllQueuedMessagesForTransmission() {
        Set<String> ipAddresses = this.outboundQueue.keySet();
        List<MessagesContainer> mConts = new LinkedList<MessagesContainer>();
        for (String ipAddr : ipAddresses) {
            MessagesContainer mContForIp = this.retrieveQueuedMessagesForTransmission(ipAddr);
            if (mContForIp.getMessage().size() > 0) {
                log.debug("There are " + mContForIp.getMessage().size() + " messages queued for ip address " + ipAddr);
                mConts.add(mContForIp);
            } else {
                log.debug("There are no messages queued for ip address " + ipAddr);
            }
        }
        if (mConts.size() > 0) {
            MessagesContainer[] msgContainers = new MessagesContainer[mConts.size()];
            for (int j = 0; j < mConts.size(); j++) {
                msgContainers[j] = mConts.get(j);
            }
            return msgContainers;
        }
        return new MessagesContainer[0];
    }
    
    /**
     * @param componentDestinition is the address of the component
     * @param containerId is the identification of the MessagesContainer created during before the transmission
     */
    public synchronized void setMessagesToTransmitted(MessagesContainer messagesContainer) {
        if (messagesContainer != null) {
            List<QueueMessageData> queued = this.outboundQueue.get(messagesContainer.getDestinationHost());
            if (queued != null) {
                for (QueueMessageData data : queued) {
                    // when a query message is queued of an IP, its container id is still null
                    if (data.getState().equals(QueueMessageState.QUEUED) && data.getContainerId() != null
                            && data.getContainerId().toString().equals(messagesContainer.getUudi())) {
                        data.changeStateToTransmitted();
                    }
                }
            }
        }
    }

    /**
     * @param componentDestinition is the component destination.
     * @param maxWindowSize is the highest window size value to be acknowledged.
     */
    public synchronized void setMessagesToAcknowledged(String destinationIpAddress, int maxWindowSize) {
        log.debug("===> Acknowledging messages up window size value of " + maxWindowSize);
        
        List<QueueMessageData> ipMessages = this.outboundQueue.get(destinationIpAddress);
        if (ipMessages != null) {
            synchronized (ipMessages) {
                for (Iterator<QueueMessageData> it = ipMessages.iterator(); it.hasNext();) {
                    QueueMessageData data = it.next();
                    if (data.getState().equals(QueueMessageState.TRANSMITTED) && data.getSequenceNumber() <= maxWindowSize) {
                        data.changeStateToAcknowledged();
                        log.debug("Message Acknowledged =>" + data.getMessage().getMessageID());
                        log.debug("sequence number of " + data.getSequenceNumber());
                        log.debug("Removing message ID " + data.getMessage().getMessageID() + " from queue");
                        it.remove();
                    }
                }
            }
        }   
    }

    /**
     * Acknowledges a specific message by a given correlation ID
     * 
     * @param destinationIpAddress is the destination address of the Message
     * @param correlationID is the ID of the message that was sent as a correlation ID.
     */
    public void acknowledgeMessageFromCorrelationId(String destinationIpAddress, String messageId) {
        if (messageId != null) {
            List<QueueMessageData> messages = this.outboundQueue.get(destinationIpAddress);
            if (messages != null) {
                synchronized (messages) {
                    for (Iterator<QueueMessageData> it = messages.iterator(); it.hasNext();) {
                        QueueMessageData data = it.next();
                        if (data.getMessage().getMessageID().equals(messageId)) {
                            data.changeStateToAcknowledged();
                            log.debug("Message Correlated Acknowledged =>" + data.getMessage().getMessageID());
                            log.debug("Sequence number of " + data.getSequenceNumber());
                            log.debug("Removing message ID " + data.getMessage().getMessageID() + " from queue");
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }
}
