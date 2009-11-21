package org.netbeams.dsp.persistence.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.Message;
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

    private Map<PersistentMessageState, Set<PersistentMessageUnit>> temporaryTransientMessages;

    /**
     * Constructs a new TransientPersistenceLayer.
     */
    private TransientPersistenceLayer() {
        temporaryTransientMessages = Collections
                .synchronizedMap(new HashMap<PersistentMessageState, Set<PersistentMessageUnit>>());
        temporaryTransientMessages.put(PersistentMessageState.TRANSIENT, new LinkedHashSet<PersistentMessageUnit>());
        temporaryTransientMessages.put(PersistentMessageState.FLUSHED, new LinkedHashSet<PersistentMessageUnit>());
    }

    /**
     * @param state is the given state
     * @return the list of transient messages from the temporary hash.
     */
    private synchronized Set<PersistentMessageUnit> getInMemoryMessagesUnitSetByState(PersistentMessageState state) {
        return temporaryTransientMessages.get(state);
    }

    /**
     * @return the transient messages in memory ready to be flushed into the database.
     */
    public synchronized Set<PersistentMessageUnit> retrieveTransientMessagesUnitSet() {
        return getInMemoryMessagesUnitSetByState(PersistentMessageState.TRANSIENT);
    }

    /**
     * @return the messages unit that have been already flushed into the database.
     */
    public synchronized Set<PersistentMessageUnit> retrieveFlushedMessagesUnitSet() {
        return getInMemoryMessagesUnitSetByState(PersistentMessageState.FLUSHED);
    }

    /**
     * Adds an instance of the PersistentMessageUnit into the transient messages unit set in memory.
     * 
     * @param persistentUnit is the persistent message unit to be added.
     */
    private void addPersistentMessageUnitIntoTransientSet(PersistentMessageUnit persistentUnit) {
        Set<PersistentMessageUnit> transientMessagesUnit = this.retrieveTransientMessagesUnitSet();
        transientMessagesUnit.add(persistentUnit);
    }

    /**
     * Adds an instance of the PersistentMessageUnit into the flushed messages unit set in memory.
     * 
     * @param persistentUnit is the persistent message unit to be added.
     */
    private void addPersistentMessageUnitIntoFlushedSet(PersistentMessageUnit persistentUnit) {
        Set<PersistentMessageUnit> flushedMessagesUnit = this.retrieveFlushedMessagesUnitSet();
        flushedMessagesUnit.add(persistentUnit);
    }

    /**
     * Adds a given DSP message into the transient persistence layer.
     * 
     * @param newDspMessage is the message to be persisted
     */
    public synchronized void addMessageToPersistenceLayer(Message newDspMessage) {
        String destinationIp = newDspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress()
                .getValue();

        if (destinationIp.equals("LOCAL") || destinationIp.equals("LOCALHOST")) {
            destinationIp = NetworkUtil.getCurrentEnvironmentNetworkIp();
        }

        SensorLocation sl = new SensorLocation.Builder().setIpAddress(destinationIp).build();
        PersistentMessageUnit pMsg = new PersistentMessageUnit(newDspMessage, sl, Calendar.getInstance());

        this.addPersistentMessageUnitIntoTransientSet(pMsg);
        log.debug("Sensor location: " + pMsg.getSensorLocation());
        log.debug("Message ID: " + pMsg.getDspMessage().getMessageID());
        log.debug("Message Type: " + pMsg.getMessageType());
        log.debug("Message Content Type: " + pMsg.getMessageContentType());
        log.debug("Stored at " + pMsg.getCollectionDateTime());
    }

    /**
     * Sets the state of all transient messages to the flushed state
     */
    public synchronized void setMessagesToFlushedState() {
        Set<PersistentMessageUnit> transientMessages = this.retrieveTransientMessagesUnitSet();
        for (PersistentMessageUnit tranMsg : transientMessages) {
            tranMsg.setStateToFlushed();
            this.addPersistentMessageUnitIntoFlushedSet(tranMsg);
        }
    }
}
