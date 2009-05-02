package org.netbeams.dsp.wiretransport.client.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.DSPXMLUnmarshaller;
import org.netbeams.dsp.util.NetworkUtil;
import org.netbeams.dsp.wiretransport.client.model.MessagesQueues;

public class DSPWireTransportHttpClient implements DSPComponent {

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPWireTransportHttpClient.class);
    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public ScheduledExecutorService scheduler;
    /**
     * Main component type descriptor
     */
    private static final String COMPONENT_TYPE = "org.netbeams.dsp.wiretransport.client";
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
     * Constructs a new DSP Wire Transport HTTP client with an internal scheduler that retrieves messages from the local
     * Messages Queues.
     */
    public DSPWireTransportHttpClient() {
        log.info("Starting DSP Transport Client...");
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * @param messages is the messages container instance with the set of messages located at the outbound queue at the
     *        messages directory.
     * @return The xml version of the DSP Messages of the given container.
     */
    public static String serializeMessagesContainer(MessagesContainer messages) {
        return messages.toXml();
    }

    /**
     * @param responseStream the marshalled version of the Messages Container
     * @return the Messages Container instance from the the xml version of the message
     */
    public MessagesContainer deserializeMessagesContainer(String responseStream) {
        try {
            return DSPXMLUnmarshaller.INSTANCE.unmarshallStream(responseStream);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return null;
        }
    }

    /**
     * The DSPMouseWorker implements a pattern of a given worker thread that is used at different types by the Executor.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspTransportSender extends Thread {

        private final Logger thLog = Logger.getLogger(DspTransportSender.class);

        /**
         * Initializes the transport sender with service reference to the Messages Directory.
         */
        public DspTransportSender() {
            thLog.info("Starting the DSP Wire Transport Client...");
        }

        public void run() {

            URL destinationURL = null;
            try {
                MessagesQueues messagesQueue = MessagesQueues.INSTANCE;
                thLog.debug("Retrieving all messages from the Messages Queue for transmission...");
                MessagesContainer[] msgContainers = messagesQueue.retrieveAllQueuedMessagesForTransmission();
                thLog.debug("There are " + msgContainers.length + " Messages Containers...");

                if (msgContainers.length > 0) {
                    thLog.debug("Sending each Messages Container to their own destinition...");
                } else {
                    thLog.debug("No messages registered in the Messages Queue...");
                    thLog.debug("Stopping the Wire Transport client for " + getDelayForTransportSender()
                            + " seconds...");
                }

                for (MessagesContainer messagesForRequest : msgContainers) {

                    thLog.debug("Messages Container UUID: " + messagesForRequest.getUudi());
                    thLog.debug("Messages Container destiniation IP is " + messagesForRequest.getDestinationHost());
                    thLog.debug("# of messages " + messagesForRequest.getMessage().size());

                    if (messagesForRequest.getMessage().size() > 0) {
                        thLog.debug("Ready to serialize " + messagesForRequest.getMessage().size() + " messages...");
                        String messagesForRequestInXml = serializeMessagesContainer(messagesForRequest);
                        thLog.debug("HTTP Request BODY: " + messagesForRequestInXml);

                        destinationURL = new URL("http://" + messagesForRequest.getDestinationHost() + ":"
                                + System.getProperty("WIRE_TRANSPORT_SERVER_PORT")
                                + System.getProperty("HTTP_SERVER_BASE_URI"));

                        thLog.debug("Setting the messages in the container " + messagesForRequest.getUudi()
                                + " to the 'transmitted' state...");

                        String messagesFromResponseInXml = this.transmitMessagesReceiveResponse(
                                messagesForRequestInXml, UUID.fromString(messagesForRequest.getUudi()), destinationURL);
                        thLog.debug("HTTP Response BODY: " + messagesFromResponseInXml);

                        if (messagesFromResponseInXml != null) {

                            this.processResponseMessagesContainer(messagesForRequest.getDestinationHost(),
                                    messagesFromResponseInXml);
                        } else {
                            thLog.debug("No messages received from the HTTP response...");
                        }
                    }
                }

            } catch (HttpException e) {
                thLog.error(e.getMessage(), e);
            } catch (IOException e) {
                thLog.error("It seems that there's no HTTP server running at " + destinationURL);
                thLog.error("Verification 1: Is the destination IP address correct?");
                thLog.error("Verification 2: Is the destination IP address reachable? Can you ping it from this "
                        + "machine?");
                thLog.error("Verification 3: Is there a DSPWireTransportServer up and running at the destination?");
                thLog.error("All messages in the outbound queues hasn't been transmitted due to this failure... "
                        + "a new attempt will be made after " + getDelayForTransportSender() + " seconds ");

            } catch (DSPException e) {
                thLog.error(e.getMessage(), e);
            } catch (Exception e) {
                thLog.error(e.getMessage(), e);
            }
        }

        /**
         * Process the response from containing the Messages Container.
         * 
         * @param messagesQueue is the current MessagesQueues singleton instance.
         * @param messagesForRequest is the messages used for the request
         * @param messagesFromResponseInXml is the response
         * @throws DSPException if the data broker from the DSP Context is missing
         */
        private void processResponseMessagesContainer(String destIp, String messagesFromResponseInXml)
                throws DSPException {

            MessagesContainer messagesFromResponse = deserializeMessagesContainer(messagesFromResponseInXml);
            for (AbstractMessage abstrMessage : messagesFromResponse.getMessage()) {
                thLog.trace("Delivering message ID " + abstrMessage.getMessageID() + " type "
                        + abstrMessage.getContentType() + " to the DSP broker...");

                // Always check if there is a broker available
                MessageBrokerAccessor messageBroker = dspContext.getDataBroker();
                if (messageBroker != null) {
                    messageBroker.send((Message) abstrMessage);
                } else {
                    log.debug("Message broker not available");
                }
            }
            // Acknowledge all messages received from the server-side, based on the highest message ID. This
            // value MUST be returned by the server-side.
            int acknowledgeUntil = messagesFromResponse.getAcknowledgeUntil();
            MessagesQueues.INSTANCE.setMessagesToAcknowledged(destIp, acknowledgeUntil);
        }

        /**
         * Sends the messagesContainer in xml format using the given UUID, to the given URL
         * 
         * @param messagesContainerXml is the marshalled version of the MessagesContainer
         * @param messagesContainerId is the UUID of the messages container used to change the state of the messages on
         *        the DSP Messages Queues.
         * @param dest is the URL for the destination
         * @return the MessagesContainer instance sent from the server as a response.
         * @throws HttpException if any http problem occurs
         * @throws IOException if any problem with the socket connection occurs (the server is not available), etc.
         */
        private String transmitMessagesReceiveResponse(String messagesContainerXml, UUID messagesContainerId, URL dest)
                throws HttpException, IOException {

            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod(dest.toString());

            NameValuePair[] formValues = new NameValuePair[1];
            formValues[0] = new NameValuePair(System.getProperty("HTTP_SERVER_REQUEST_VARIABLE"), messagesContainerXml);
            postMethod.setRequestBody(formValues);
            // execute method and handle any error responses.

            thLog.debug("Trying send the container " + messagesContainerId
                    + " to the DSP Wire Transport Server located" + "at " + dest.toString());
            int statusCode = client.executeMethod(postMethod);

            // // Change the messages to the Transmitted state
            // this.messagesDir.setMessagesToTransmitted(url, messagesContainerId);

            String responseXmlMessagesContainter = null;
            thLog.debug("The HTTP server replied with the status code " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                responseXmlMessagesContainter = postMethod.getResponseBodyAsString();
                thLog.debug("The DSP Transport Server responded with messages " + dest.toString());
                thLog.debug(responseXmlMessagesContainter);
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                responseXmlMessagesContainter = null;

                thLog.error("It seems that there is an HTTP server running at " + dest.toString()
                        + ", but the expected " + "service at '" + System.getProperty("HTTP_SERVER_BASE_URI")
                        + "' can't be reached...");
                thLog.error("Verification 1: Is there a DSPWireTransportServer up and running at the destination?");
                thLog.error("Verification 2: Is the HTTP server PORT ("
                        + System.getProperty("WIRE_TRANSPORT_SERVER_PORT")
                        + ") the one where the HTTP server receives the service '"
                        + System.getProperty("HTTP_SERVER_BASE_URI") + "'?");
                thLog.error("All messages in the outbound queue for this destination haven't been transmitted due "
                        + "to this failure... A new attempt will be made after " + getDelayForTransportSender()
                        + " seconds ...");
            }
            postMethod.releaseConnection();
            return responseXmlMessagesContainter;
        }
    }

    /**
     * Shuts down all the threads started by the scheduler.
     */
    private void shutdownTransportWorkers() {
        if (!this.scheduler.isShutdown()) {
            log.debug("Shutting down all transport workers...");
            this.scheduler.shutdown();
        }
    }

    /**
     * Updates the internal DSP Wire Transport with properties such as destination IP address and PORT for remote
     * communication.
     * 
     * @param updateMessage is an update message to configure the component.
     * @throws DSPException if any problem happens during the update.
     */
    private void updateComponentProperties(UpdateMessage updateMessage) throws DSPException {

        MessageContent propertiesNode = updateMessage.getBody().getAny();

        DSProperties properties = (DSProperties) propertiesNode;
        boolean hasDelayChanged = false;
        for (DSProperty property : properties.getProperty()) {
            System.setProperty(property.getName(), property.getValue());
            if (property.getName().equals("WIRE_TRANSPORT_TRANSFER_DELAY") && !hasDelayChanged
                    && !System.getProperty(property.getName()).equals(property.getValue())) {
                hasDelayChanged = true;
            }
            log.debug("Update Property: " + property.getName() + "=" + property.getValue());
        }

        long delay = this.getDelayForTransportSender();
        if (this.scheduler.isShutdown()) {
            log.info("Starting scheduling the transport senders to wake up at every " + delay + " seconds...");
            this.scheduler.scheduleWithFixedDelay(new DspTransportSender(), delay, delay, TimeUnit.SECONDS);
        } else if (hasDelayChanged) {
            this.shutdownTransportWorkers();
            log.info("Rescheduling the transport senders to wake up at every " + delay + " seconds...");
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            this.scheduler.scheduleWithFixedDelay(new DspTransportSender(), delay, delay, TimeUnit.SECONDS);
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
    private long getDelayForTransportSender() {
        long delay = 60;
        try {
            delay = Long.valueOf(System.getProperty("WIRE_TRANSPORT_TRANSFER_DELAY")).longValue();
        } catch (NumberFormatException nfe) {
            log.error(nfe.getMessage(), nfe);
            log.error("The property WIRE_TRANSPORT_TRANSFER_DELAY must be set with an int value.");
            log.error("Using default value of 60 seconds for WIRE_TRANSPORT_TRANSFER_DELAY");
        }
        return delay;
    }

    /**
     * @param message a DSP message instance.
     * @return if the message is for local delivery
     */
    public boolean isMessageForLocalDelivery(String nodeAddress) {
        return nodeAddress.equals(NetworkUtil.getCurrentEnvironmentNetworkIp())
                || nodeAddress.equalsIgnoreCase("LOCAL") || nodeAddress.equalsIgnoreCase("LOCALHOST")
                || nodeAddress.equals("127.0.0.1");
    }

    public void deliver(Message message) throws DSPException {
        log.debug("Delivering message, ID " + message.getMessageID());
        String correlation = message.getHeader().getCorrelationID() + "";
        log.debug("Message " + correlation == null ? "has correlation ID " + correlation : "without correlation ID");
        String nodeAddress = message.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue();
        log.debug("Message address is " + nodeAddress + ", current host ip is "
                + NetworkUtil.getCurrentEnvironmentNetworkIp());

        final boolean messageIsLocal = this.isMessageForLocalDelivery(nodeAddress);
        log.debug("is Message for local delivery? " + messageIsLocal);

        // Processing start-up or configuration messages
        if (message instanceof UpdateMessage && messageIsLocal) {

            log.debug("Update messages delivered: configuring DSP Wire Transport Server with Properties");
            this.updateComponentProperties((UpdateMessage) message);

        } else if (message instanceof QueryMessage && messageIsLocal) {

            log.debug("Query message delivered to this component... must produce a response...");
            this.queryComponentProperties((QueryMessage) message);

        } else {

            log.debug("Adding the DSP message to the Messages Outbound Queues...");
            // add the message to the queues
            MessagesQueues.INSTANCE.addMessageToOutboundQueue(message);
        }
    }

    private void queryComponentProperties(QueryMessage queryMessage) throws DSPException {
        MessageContent content = queryMessage.getBody().getAny();
        log.debug("Content class " + content.getClass().getName());

        if (content instanceof DSProperties) {
            log.debug("Got query configuration");

            DSProperties props = new DSProperties();

            DSProperty prp = new DSProperty();
            prp.setName("HTTP_SERVER_REQUEST_VARIABLE");
            prp.setValue(System.getProperty("HTTP_SERVER_REQUEST_VARIABLE"));
            props.getProperty().add(prp);

            DSProperty prp2 = new DSProperty();
            prp2.setName("HTTP_SERVER_BASE_URI");
            prp2.setValue(System.getProperty("HTTP_SERVER_BASE_URI"));
            props.getProperty().add(prp2);

            DSProperty prp3 = new DSProperty();
            prp3.setName("WIRE_TRANSPORT_SERVER_PORT");
            prp3.setValue(System.getProperty("WIRE_TRANSPORT_SERVER_PORT"));
            props.getProperty().add(prp3);

            DSProperty prp4 = new DSProperty();
            prp4.setName("WIRE_TRANSPORT_TRANSFER_DELAY");
            prp4.setValue(System.getProperty("WIRE_TRANSPORT_TRANSFER_DELAY"));
            props.getProperty().add(prp4);

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

        String destIpAddress = null, hostName = null;
        try {
            destIpAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            log.debug("Could not find the name of this host for the network service... Using 'localhost' instead...");
            hostName = "localhost";
        }
        log.info("Initializing client with hostname '" + hostName + "' / IP address '" + destIpAddress + "'");
    }

    public void stopComponent() throws DSPException {
        log.info("Stopping component");
        this.shutdownTransportWorkers();
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