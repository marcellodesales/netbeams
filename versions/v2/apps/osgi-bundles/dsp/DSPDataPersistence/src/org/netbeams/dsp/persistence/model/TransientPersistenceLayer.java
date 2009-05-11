package org.netbeams.dsp.persistence.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.persistence.model.component.data.KeyValuePairsLinkedMap;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageState;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
import org.netbeams.dsp.persistence.model.location.SensorLocation;
import org.netbeams.dsp.util.NetworkUtil;

/**
 * Transient Persistent Layer holds DSP messages to be sent to a remote Database system.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 * 
 */
public enum TransientPersistenceLayer {

    /**
     * Singleton for the messages Queues
     */
    INSTANCE;

    private static final Logger log = Logger.getLogger(TransientPersistenceLayer.class);

    static {
        log.info("Initializing the local transient persistence layer");
    }

    /**
     * This is the hash of the IP address of the destination and the Queue of the DirectoryData of the outbound
     * messages.
     */
    private KeyValuePairsLinkedMap keyValuePairs;

    /**
     * Constructs a new TransientPersistenceLayer.
     */
    private TransientPersistenceLayer() {
        this.keyValuePairs = new KeyValuePairsLinkedMap();
    }

    /**
     * Adds a given DSP message into the transient persistence layer.
     * 
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public synchronized void addMessageToPersistenceLayer(Message newDspMessage) {
        String destinationIp = newDspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress()
                .getValue();
        
        if (destinationIp.equals("LOCAL") || destinationIp.equals("LOCALHOST")) {
            destinationIp = NetworkUtil.getCurrentEnvironmentNetworkIp();
        }
        
        SensorLocation sl = new SensorLocation.Builder().setIpAddress(destinationIp).build();
        PersistentMessageUnit pMsg = new PersistentMessageUnit(newDspMessage, sl);

        this.keyValuePairs.put(sl, pMsg);
        log.debug("Sensor location: " + pMsg.getSensorLocation());
        log.debug("Message ID: " + pMsg.getDspMessage().getMessageID());
        log.debug("Message Type: " + pMsg.getMessageType());
        log.debug("Message Content Type: " + pMsg.getMessageContentType());
        log.debug("Stored at " + pMsg.getCollectionDateTime());
    }
    
    /**
     * @return a list of all persistent message unit on the transient state.
     */
    public synchronized List<PersistentMessageUnit> retrieveAllTransientMessages() {
        log.debug("Retrieving all messages on transient state");
        return this.retrieveAllMessagesByState(PersistentMessageState.TRANSIENT);
    }

    /**
     * @return a list of all persistent message unit on the flushed state.
     */
    public synchronized List<PersistentMessageUnit> retrieveAllPersistentMessages() {
        log.debug("Retrieving all messages on persistent state");
        return this.retrieveAllMessagesByState(PersistentMessageState.FLUSHED);
    }

    /**
     * @param state is a given state of the persistent unit
     * @return a list of all messages on a given state.
     */
    private synchronized List<PersistentMessageUnit> retrieveAllMessagesByState(PersistentMessageState state) {
        List<PersistentMessageUnit> transientMessages = new LinkedList<PersistentMessageUnit>();
        for (PersistentMessageUnit pMsg : this.keyValuePairs.values()) {
            if (pMsg.getState().equals(state)) {
                transientMessages.add(pMsg);
            }
        }
        return transientMessages;
    }

    /**
     * Sets the state of all transient messages to the flushed state
     */
    public synchronized void setMessagesToFlushedState() {
        List<PersistentMessageUnit> transientMessages = this.retrieveAllTransientMessages();
        for (PersistentMessageUnit tranMsg : transientMessages) {
            tranMsg.setStateToFlushed();
        }
    }
}
