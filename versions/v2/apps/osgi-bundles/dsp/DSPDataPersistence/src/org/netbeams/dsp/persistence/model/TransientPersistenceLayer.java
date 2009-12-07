package org.netbeams.dsp.persistence.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.Message;
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

    private Set<PersistentMessageUnit> temporaryTransientMessages;

    /**
     * Constructs a new TransientPersistenceLayer.
     */
    private TransientPersistenceLayer() {
        temporaryTransientMessages = Collections.synchronizedSet(new LinkedHashSet<PersistentMessageUnit>());
    }

    /**
     * @return the transient messages in memory ready to be flushed into the database.
     */
    public synchronized Set<PersistentMessageUnit> retrieveTransientMessagesUnitSet() {
        Set<PersistentMessageUnit> transientMessages = new HashSet<PersistentMessageUnit>();
        for (PersistentMessageUnit tranMessage: this.temporaryTransientMessages) {
            transientMessages.add(tranMessage);
        }
        return transientMessages;
    }

    /**
     * Adds a given DSP message into the transient persistence layer.
     * 
     * @param newDspMessage is the message to be persisted
     */
    public synchronized void addMessageToPersistenceLayer(Message newDspMessage) {
        String destinationIp = newDspMessage.getHeader().getConsumer().getComponentLocator().getNodeAddress()
                .getValue();

        SensorLocation.Builder sl = new SensorLocation.Builder();
        if (destinationIp.equals("LOCAL") || destinationIp.equals("LOCALHOST")) {
            destinationIp = NetworkUtil.getCurrentEnvironmentNetworkIp();
        }
        sl.setIpAddress(destinationIp);
        sl.setLatitude(new Double(System.getProperty("SENSOR_1_LATITUDE")));
        sl.setLongitude(new Double(System.getProperty("SENSOR_1_LONGITUDE")));

        PersistentMessageUnit pMsg = new PersistentMessageUnit(newDspMessage, sl.build(), Calendar.getInstance());

        synchronized (this.temporaryTransientMessages) {
            this.temporaryTransientMessages.add(pMsg);
        }
        log.debug("Sensor location: " + pMsg.getSensorLocation());
        log.debug("Message ID: " + pMsg.getDspMessage().getMessageID());
        log.debug("Message Type: " + pMsg.getMessageType());
        log.debug("Message Content Type: " + pMsg.getMessageContentType());
        log.debug("Stored at " + pMsg.getCollectionDateTime());
    }

    /**
     * @param pmu is the message unit already flushed.
     */
    public synchronized void setMessageToFlushed(PersistentMessageUnit pmu) {
        log.debug("message to remove" + pmu);
        if (pmu != null) {
            synchronized (this.temporaryTransientMessages) {
                for (Iterator<PersistentMessageUnit> it = temporaryTransientMessages.iterator(); it.hasNext();) {
                    PersistentMessageUnit messageUnit = it.next();
                    if (messageUnit.getDspMessage().getMessageID().equals(pmu.getDspMessage().getMessageID())) {
                        it.remove();
                        break;
                    }
                }
            }
        }
    }
}
