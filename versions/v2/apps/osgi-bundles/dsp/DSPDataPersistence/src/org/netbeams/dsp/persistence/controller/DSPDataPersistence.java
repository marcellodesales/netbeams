package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.persistence.model.TransientPersistenceLayer;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
import org.netbeams.dsp.persistence.model.component.data.PropertyKey;
import org.netbeams.dsp.persistence.util.DSPJSONUtil;
import org.netbeams.dsp.util.NetworkUtil;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoAdmin;
import com.mongodb.MongoException;

public class DSPDataPersistence implements DSPComponent {

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPDataPersistence.class);
    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public ScheduledExecutorService scheduler;
    /**
     * Main component type descriptor
     */
    private static final String COMPONENT_TYPE = "org.netbeams.dsp.persistence";
    /**
     * Default component descriptor
     */
    private static final ComponentDescriptor COMPONENT_DESCRIPTOR = null;
    /**
     * The DSPContext defines the external context
     */
    private DSPContext dspContext;
    /**
     * The DSP Node ID
     */
    private String componentNodeId;

    /**
     * Constructs a new DSP Data Persistence with an internal scheduler that flushes the current data to the database on
     * a given scheduled amount of time.
     */
    public DSPDataPersistence() {
        log.info("Starting DSP Data Persistence...");
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * The DSPDataFlusher updates the persistence layer with the current messages set in the queue.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspDataFlusher extends Thread {

        private final Logger thLog = Logger.getLogger(DspDataFlusher.class);

        /**
         * Initializes the data flusher.
         */
        public DspDataFlusher() {
            thLog.info("Starting the DSP Data Flusher to transfer messages to database...");
        }

        public void run() {

            try {
                TransientPersistenceLayer transientLayer = TransientPersistenceLayer.INSTANCE;
                thLog.debug("Retrieving all transient messages to be flushed...");
                List<PersistentMessageUnit> tranMsgs = transientLayer.retrieveAllTransientMessages();

                if (tranMsgs.size() > 0) {
                    thLog.debug("Preparing to transfer messages " + tranMsgs.size() + " to database...");

                    for (PersistentMessageUnit tranMsg : tranMsgs) {

                        DSPMongoCRUDService.insertMessageUnit(tranMsg);

                        tranMsg.setStateToFlushed();

                        // TODO: save the key_value pair into the persistent data store.
                        // TODO: SEPARATE ALL THE PROPERTIES FROM EACH COLLECTION OF ITEMS
                        /*
                         * Imagine from JSON values... Write a parser or the driver that returns all the properties to
                         * the application. { "sondeData": [ { "samplingTimeStamp": { "time": 1242009066438, "timezone":
                         * "America/Los_Angeles" }, "temp": 87.0, "cond": 35.0, "resist": 45.0, "sal": 67.0, "press":
                         * 443.0, "depth": 565.0, "ph": 12.0, "phmV": 65.0, "odoSat": 39.0, "odoConc": 88.0, "turbid":
                         * 40.0, "battery": 5.0 }, { "temp": 87.0, "cond": 35.0, "resist": 45.0, "sal": 67.0, "press":
                         * 443.0, "depth": 565.0, "ph": 12.0, "phmV": 65.0, "odoSat": 39.0, "odoConc": 88.0, "turbid":
                         * 40.0, "battery": 5.0 } ] } Since the values are going to be related to a given sensor, 2
                         * pending strategies will be evaluated: a sampling per message; each value of a given sampling
                         * per message. All properties needs to be uniquely identified, and the samplings are related
                         * with the time stamp. %%%%%%%%%%%%% Key-Value Data Store %%%%%%%%%%% KEY
                         * 192.168.0.2:SondeDataType VALUE { "temp": 87.0, "cond": 35.0, "resist": 45.0, "sal": 67.0,
                         * "press": 443.0, "depth": 565.0, "ph": 12.0, "phmV": 65.0, "odoSat": 39.0, "odoConc": 88.0,
                         * "turbid": 40.0, "battery": 5.0 } %%%%%%%%%%%%% Relational Database %%%%%%%%%%% sensor_id =
                         * 192.168.0.2 message_content = SondeDataType samplingTime = 04/29/2009 10:23:34 PST key = temp
                         * Value = 87.0 ... ... ... ... ...
                         */
                    }

                } else {
                    thLog.debug("There are no transient messages in the transient persistent layer...");
                    thLog.debug(transientLayer.retrieveAllPersistentMessages().size()
                            + " messages were saved on the db during the current session");
                }

            } catch (Exception e) {
                thLog.error(e.getMessage(), e);
            }
        }
    }
    

    /**
     * Shuts down all the threads started by the scheduler.
     */
    private void shutdownFlushWorkers() {
        if (!this.scheduler.isShutdown()) {
            log.debug("Shutting down flush workers...");
            this.scheduler.shutdown();
        }
    }

    /**
     * Updates the internal Data Persistence properties.
     * 
     * @param updateMessage
     *            is an update message to configure the component.
     * @throws DSPException
     *             if any problem happens during the update.
     */
    private void updateComponentProperties(UpdateMessage updateMessage) throws DSPException {

        MessageContent propertiesNode = updateMessage.getBody().getAny();

        DSProperties properties = (DSProperties) propertiesNode;
        boolean hasDelayChanged = false;
        for (DSProperty property : properties.getProperty()) {
            System.setProperty(property.getName(), property.getValue());
            if (property.getName().equals("TRANSIENT_DATA_FLUSHER_DELAY") && !hasDelayChanged
                    && !System.getProperty(property.getName()).equals(property.getValue())) {
                hasDelayChanged = true;
            }
            log.debug("Update Property: " + property.getName() + "=" + property.getValue());
        }

        long delay = this.getDelayForFlushingIntoDatabase();
        if (!this.scheduler.isShutdown()) {
            log.info("Starting scheduling the transient data flusher to wake up at every " + delay + " seconds...");
            this.scheduler.scheduleWithFixedDelay(new DspDataFlusher(), delay, delay, TimeUnit.SECONDS);
        } else if (hasDelayChanged) {
            this.shutdownFlushWorkers();
            log.info("Rescheduling the transient data flusher to wake up at every " + delay + " seconds...");
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            this.scheduler.scheduleWithFixedDelay(new DspDataFlusher(), delay, delay, TimeUnit.SECONDS);
        }
        // Send the acknowledgment IFF the producer was the management.
        if (updateMessage.getHeader().getProducer().getComponentType().equals("org.netbeams.dsp.management")) {
            this.sendBackAcknowledge(updateMessage);
        }
    }

    private void sendBackAcknowledge(UpdateMessage message) throws DSPException {
        DSProperties props = new DSProperties();

        // Obtain original producer
        ComponentIdentifier origProducer = message.getHeader().getProducer();
        String originalMessageId = message.getMessageID();
        // Create reply message

        String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(getComponentNodeId(),
                localIPAddress, getComponentType());

        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
        header.setCorrelationID(originalMessageId);

        Message replyMsg = DSPMessagesFactory.INSTANCE.makeDSPAcknowledgementMessage(header, props);
        this.deliver(replyMsg);
    }

    /**
     * @return the value for the transport delay set from the system properties.
     */
    private long getDelayForFlushingIntoDatabase() {
        long delay = 600;
        try {
            delay = Long.valueOf(System.getProperty("TRANSIENT_DATA_FLUSHER_DELAY")).longValue();
        } catch (NumberFormatException nfe) {
            log.error(nfe.getMessage(), nfe);
            log.error("The property TRANSIENT_DATA_FLUSHER_DELAY must be set with an int value.");
            log.error("Using default value of 600 seconds for TRANSIENT_DATA_FLUSHER_DELAY");
        }
        return delay;
    }

    public void deliver(Message message) throws DSPException {
        log.debug("Delivering message, ID " + message.getMessageID());

        // Processing start-up or configuration messages
        if (message instanceof UpdateMessage) {

            log.debug("Update messages delivered: configuring DSP Data Persistence with Properties");
            this.updateComponentProperties((UpdateMessage) message);

        } else if (message instanceof QueryMessage) {

            log.debug("Query message delivered...");
            this.queryComponentProperties((QueryMessage) message);

        } else {

            log.debug("Adding the DSP message to the Transient Persistence layer...");
            // add the message to the queues
            TransientPersistenceLayer.INSTANCE.addMessageToPersistenceLayer(message);
        }
    }

    private void queryComponentProperties(QueryMessage queryMessage) throws DSPException {
        MessageContent content = queryMessage.getBody().getAny();
        log.debug("Content class " + content.getClass().getName());

        if (content instanceof DSProperties) {
            log.debug("Got query configuration");

            DSProperties props = new DSProperties();

            DSProperty prp = new DSProperty();
            prp.setName("TRANSIENT_DATA_FLUSHER_DELAY");
            prp.setValue(System.getProperty("TRANSIENT_DATA_FLUSHER_DELAY"));
            props.getProperty().add(prp);

            // Obtain original producer
            ComponentIdentifier origProducer = queryMessage.getHeader().getProducer();
            String originalMessageId = queryMessage.getMessageID();
            // Create reply message

            String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
            ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(getComponentNodeId(),
                    localIPAddress, getComponentType());

            Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
            header.setCorrelationID(originalMessageId);

            Message replyMsg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, props);
            this.deliver(replyMsg);
        } else {
            log.debug("Query message dropped because it did not contain DSProperties: " + content.getClass().getName());
        }
    }

    public Message deliverWithReply(Message message) throws DSPException {
        log.error("Delivering message with reply not implemented.");
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        log.error("Delivering message with reply with delay not implemented.");
        return null;
    }

    public ComponentDescriptor getComponentDescriptor() {
        return COMPONENT_DESCRIPTOR;
    }

    public void startComponent() throws DSPException {
        log.info("Starting component");
    }

    public void stopComponent() throws DSPException {
        log.info("Stopping component");
        this.shutdownFlushWorkers();
    }

    public String getComponentNodeId() {
        return this.componentNodeId;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        this.dspContext = context;
        this.componentNodeId = componentNodeId;
        log.info("Initializing component ID " + componentNodeId + " with context " + this.dspContext.toString());
    }
}
