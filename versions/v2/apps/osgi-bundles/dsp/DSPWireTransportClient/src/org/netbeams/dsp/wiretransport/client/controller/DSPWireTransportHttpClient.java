package org.netbeams.dsp.wiretransport.client.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.ObjectFactory;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.NetworkUtil;
import org.netbeams.dsp.wiretransport.client.model.MessagesQueues;
import org.w3c.dom.Node;

public class DSPWireTransportHttpClient implements DSPComponent {

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPWireTransportHttpClient.class);
    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
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
    }

    /**
     * @param messages is the messages container instance with the set of messages located at the outbound queue at the
     *        messages directory.
     * @return The xml version of the DSP Messages of the given container.
     * @throws JAXBException if any problem with regards to the marshall operation happens.
     */
    public static String serializeMessagesContainer(MessagesContainer messages) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Marshaller m = context.createMarshaller();
        StringWriter output = new StringWriter();
        m.marshal(messages, output);
        return output.toString();
    }

    /**
     * @param responseStream the marshalled version of the Messages Container
     * @return the Messages Container instance from the the xml version of the message
     * @throws JAXBException if any problem with the unmarshall operation occurs.
     */
    public static MessagesContainer deserializeMessagesContainer(String responseStream) throws JAXBException {
        InputStream input = new ByteArrayInputStream(responseStream.getBytes());
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = context.createUnmarshaller();
        return (MessagesContainer) um.unmarshal(input);
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
                MessagesContainer[] msgContainers = messagesQueue.retrieveAllQueuedMessagesForTransmission();
                thLog.debug("Retrieving all messages from the Messages Queue for transmission...");
                thLog.debug("Messages Container size is " + msgContainers.length + ".");

                if (msgContainers.length > 0) {
                    thLog.debug("Sending each Messages Container to their own destinition...");
                } else {
                    thLog.debug("No messages registered in the Messages Queue...");
                    thLog.debug("Stopping the Wire Transport for 60 seconds...");
                }

                for (MessagesContainer messagesForRequest : msgContainers) {

                    thLog.debug("Messages Container UUID: " + messagesForRequest.getUudi());
                    thLog.debug("Messages Container destiniation IP is " + messagesForRequest.getHost());
                    thLog.debug("Messages Container # of messages " + messagesForRequest.getMessage().size());

                    if (messagesForRequest.getMessage().size() > 0) {
                        String messagesForRequestInXml = serializeMessagesContainer(messagesForRequest);
                        thLog.trace("HTTP Request BODY: " + messagesForRequestInXml);

                        destinationURL = new URL("http://" + messagesForRequest.getHost() + ":"
                                + System.getProperty("WIRE_TRANSPORT_SERVER_PORT") + System.getProperty("BASE_URI"));

                        thLog.trace("Setting the messages in the container " + messagesForRequest.getUudi()
                                + " to the 'transmitted' state...");

                        // Set the messages to transmitted before sending them. This avoid messages
                        // sent to the local DSP to be in an inaccurate state.
                        messagesQueue.setMessagesToTransmitted(messagesForRequest.getHost(), UUID
                                .fromString(messagesForRequest.getUudi()));

                        String messagesFromResponseInXml = this.transmitMessagesReceiveResponse(
                                messagesForRequestInXml, UUID.fromString(messagesForRequest.getUudi()), destinationURL);
                        thLog.trace("HTTP Response BODY: " + messagesFromResponseInXml);

                        if (messagesFromResponseInXml != null) {

                            this.processResponseMessagesContainer(messagesQueue, messagesForRequest,
                                    messagesFromResponseInXml);

                        } else {
                            thLog.trace("No messages received from the HTTP response...");
                        }
                    }
                }

            } catch (ConnectException e) {
                thLog.error("There's no HTTP server running at " + destinationURL
                        + ". Please verify: \n1. There's a DSPWireTransport component running at the destination..."
                        + "\n2. if there's HTTP connectivity");
            } catch (HttpException e) {
                thLog.error(e.getMessage(), e);
            } catch (IOException e) {
                thLog.error(e.getMessage(), e);
            } catch (JAXBException e) {
                thLog.error(e.getMessage(), e);
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
         * @throws JAXBException if any problem occurs with the JAXB desirialization process.
         * @throws DSPException if the data broker from the DSP Context is missing
         */
        private void processResponseMessagesContainer(MessagesQueues messagesQueue,
                MessagesContainer messagesForRequest, String messagesFromResponseInXml) throws JAXBException,
                DSPException {
            MessagesContainer messagesFromResponse = deserializeMessagesContainer(messagesFromResponseInXml);
            for (AbstractMessage abstrMessage : messagesFromResponse.getMessage()) {
                if (abstrMessage instanceof AcknowledgementMessage) {
                    thLog.trace("Received Acknowledge message with correlation ID = "
                            + abstrMessage.getHeader().getCorrelationID());
                    messagesQueue.setMessagesToTransmitted(messagesForRequest.getHost(), UUID.fromString(abstrMessage
                            .getHeader().getCorrelationID().toString()));
                } else {

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
            }
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

            thLog.trace("Trying send the container " + messagesContainerId
                    + " to the DSP Wire Transport Server located" + "at " + dest.toString());
            int statusCode = client.executeMethod(postMethod);

            // // Change the messages to the Transmitted state
            // this.messagesDir.setMessagesToTransmitted(url, messagesContainerId);

            String responseXmlMessagesContainter = null;
            thLog.debug("The HTTP server replied with the status code " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                responseXmlMessagesContainter = postMethod.getResponseBodyAsString();
                thLog.debug("The DSP Transport Server responded with messages " + dest.toString());
                thLog.trace(responseXmlMessagesContainter);
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                responseXmlMessagesContainter = null;
                thLog.error("The server is running, but there's no service at " + dest.toString());
            }
            postMethod.releaseConnection();
            return responseXmlMessagesContainter;
        }
    }

    /**
     * Shuts down all the threads started by the scheduler.
     */
    private void shutdownTransportWorkers() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            log.trace("Shutting down all transport workers...");
        }
    }

    /**
     * Updates the internal DSP Wire Transport with properties such as destination IP address and PORT for remote
     * communication.
     * 
     * @param updateMessage is an update message to configure the component.
     */
    private void updateComponentProperties(UpdateMessage updateMessage) {

        Object propertiesNode = updateMessage.getBody().getAny();

        try {
            JAXBContext jc = JAXBContext.newInstance(org.netbeams.dsp.data.property.ObjectFactory.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            DSProperties properties = (DSProperties) unmarshaller.unmarshal((Node) propertiesNode);
            for (DSProperty property : properties.getProperty()) {
                if (property.getName().equals("WIRE_TRANSPORT_SERVER_IP")) {
                    System.setProperty("WIRE_TRANSPORT_SERVER_IP", property.getValue());
                    log.debug("Update Property: WIRE_TRANSPORT_SERVER_IP=" + property.getValue());
                    
                } else if (property.getName().equals("WIRE_TRANSPORT_SERVER_PORT")) {
                    log.debug("Update Property: WIRE_TRANSPORT_SERVER_PORT=" + property.getValue());
                    System.setProperty("WIRE_TRANSPORT_SERVER_PORT", property.getValue());

                } else if (property.getName().equals("HTTP_SERVER_BASE_URI")) {
                    log.debug("Update Property: HTTP_SERVER_BASE_URI=" + property.getValue());
                    System.setProperty("HTTP_SERVER_BASE_URI", property.getValue());
                    
                } else if (property.getName().equals("HTTP_SERVER_REQUEST_VARIABLE")) {
                    log.debug("Update Property: HTTP_SERVER_REQUEST_VARIABLE=" + property.getValue());
                    System.setProperty("HTTP_SERVER_REQUEST_VARIABLE", property.getValue());
                }
            }

        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deliver(Message message) throws DSPException {

        log.debug("Deliverying DSP message to this component");

        // Processing start-up or configuration messages
        if (message instanceof UpdateMessage && message.getContentType().equals("org.netbeams.dsp.data.property")) {

            log.debug("Update messages delivered: configuring DSP Wire Transport Server with Properties");
            this.updateComponentProperties((UpdateMessage) message);

        } else {
            // add the message to the queues
            MessagesQueues.INSTANCE.addMessageToOutboundQueue(message);
        }
    }

    public Message deliverWithReply(Message message) throws DSPException {
        log.debug("Delivering message with reply not implemented.");
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        log.debug("Delivering message with reply with delay not implemented.");
        return null;
    }

    public ComponentDescriptor getComponentDescriptor() {
        return COMPONENT_DESCRIPTOR;
    }

    public void startComponent() throws DSPException {
        log.info("Starting component");

        log.info("Scheduling the transport senders to wake up at every 60 seconds...");
        scheduler.scheduleWithFixedDelay(new DspTransportSender(), 0, 60, TimeUnit.SECONDS);
        
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
        log.info("Initializing component: " + componentNodeId + " with context " + this.dspContext.toString());
    }
}