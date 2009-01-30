package org.netbeams.dsp.wiretransport.controller;

import static org.netbeams.dsp.wiretransport.osgi.DSPWireTransportActivator.DESTINATION_PORT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
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
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.ObjectFactory;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;

public class DSPWireTransportHttpClient implements DSPComponent {

    private static final Logger log = Logger.getLogger(DSPWireTransportHttpClient.class);

    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final String COMPONENT_TYPE = "org.netbeams.dsp.wiretransport";

    private static final ComponentDescriptor COMPONENT_DESCRIPTOR = null;

    /**
     * The DSPContext defines the external context
     */
    private DSPMessagesDirectory messagesQueue;
    private DSPContext dspContext;
    /**
     * The DSP Node ID
     */
    private String componentNodeId;

    public DSPWireTransportHttpClient(DSPMessagesDirectory dspQueues) throws JAXBException {
        this.messagesQueue = dspQueues;
        log.info("Starting DSP Transport Client...");
        log.info("Scheduling the transport senders to wake up at every 60 seconds...");
        scheduler.scheduleWithFixedDelay(new DspTransportSender(null), 0, 60, TimeUnit.SECONDS);
    }

    public DSPWireTransportHttpClient(MessagesContainer messages) throws JAXBException {
        scheduler.scheduleWithFixedDelay(new DspTransportSender(messages), 0, 10, TimeUnit.SECONDS);
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
     * @return the current environment's IP address, taking into account the Internet connection to any of the available
     *         machine's Network interfaces. Examples of the outputs can be in octatos or in IPV6 format.
     * 
     *         fec0:0:0:9:213:e8ff:fef1:b717%4 siteLocal: true isLoopback: false isIPV6: true
     *         ============================================ 130.212.150.216 <<<<<<<<<<<------------- This is the one we
     *         want to grab so that we can. siteLocal: false address the DSP on the network. isLoopback: false isIPV6:
     *         false ==> lo ============================================ 0:0:0:0:0:0:0:1%1 siteLocal: false isLoopback:
     *         true isIPV6: true ============================================ 127.0.0.1 siteLocal: false isLoopback:
     *         true isIPV6: false
     */
    public static String getCurrentEnvironmentNetworkIp() {
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Somehow we have a socket error...");
        }

        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress addr = address.nextElement();
                if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()
                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                    return addr.getHostAddress();
                }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * The DSPMouseWorker implements a pattern of a given worker thread that is used at different types by the Executor.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspTransportSender extends Thread {

        private MessagesContainer messages;

        private final Logger thLog = Logger.getLogger(DspTransportSender.class);

        /**
         * Initializes the transport sender with service reference to the Messages Directory.
         */
        public DspTransportSender(MessagesContainer messages) {
            this.messages = messages;
            thLog.info("Starting the DSP Wire Transport Sender...");
        }

        /**
         * Sends the messagesContainer in xml format using the given UUID, to the given URL
         * 
         * @param messagesContainerXml is the marshalled version of the MessagesContainer
         * @param messagesContainerId is the UUID of the messages container used to change the state of the messages on
         *        the DSP Messages Queues.
         * @param dest is the URL for the destinition
         * @return the MessagesContainer instance sent from the server as a response.
         * @throws HttpException if any http problem occurs
         * @throws IOException if any problem with the socket connection occurs (the server is not available), etc.
         */
        private String transmitMessagesReceiveResponse(String messagesContainerXml, UUID messagesContainerId, URL dest)
                throws HttpException, IOException {

            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod(dest.toString());

            NameValuePair[] formValues = new NameValuePair[1];
            formValues[0] = new NameValuePair(DSPWireTransportHttpReceiverServlet.TRANSPORT_HTTP_VARIABLE,
                    messagesContainerXml);
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

        public void run() {

            URL destinationURL = null;
            try {

                MessagesContainer[] msgContainers = messagesQueue.retrieveAllQueuedMessagesForTransmission();
                thLog.debug("Retrieving all messages from the Messages Queue for transmission...");
                // Just for the matter of testing purposes, the MessagesContainer can be passed as a parameter.
                msgContainers = (this.messages != null) ? new MessagesContainer[] { this.messages } : msgContainers;
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

                        destinationURL = new URL("http://" + messagesForRequest.getHost() + ":" + DESTINATION_PORT
                                + DSPWireTransportHttpReceiverServlet.BASE_URI);

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
                            MessagesContainer messagesFromResponse = deserializeMessagesContainer(messagesFromResponseInXml);
                            for (AbstractMessage message : messagesFromResponse.getMessage()) {
                                if (message instanceof AcknowledgementMessage) {
                                    thLog.trace("Received Acknowledge message with correlation ID = "
                                            + message.getHeader().getCorrelationID());
                                    messagesQueue.setMessagesToTransmitted(messagesForRequest.getHost(), UUID
                                            .fromString(message.getHeader().getCorrelationID().toString()));
                                } else {
                                    // At this point, deliver the messages for the broker through the proxy with
                                    // the delivery method of the DSP component.
                                    deliver((Message) message);
                                    thLog.trace("Delivering message ID " + message.getMessageID() + " type "
                                            + message.getContentType() + " to the DSP broker...");
                                }
                            }
                        } else {
                            thLog.trace("No messages received from the HTTP response...");
                        }
                    }
                }

            } catch (ConnectException e) {
                thLog.error("There's no HTTP server running at " + destinationURL
                        + ". Please verify if there's a DSPWireTransport component running...");
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

    public void deliver(Message message) throws DSPException {
        // Always check if there is a broker available
        MessageBrokerAccessor messageBroker = this.dspContext.getDataBroker();
        log.debug("Delivering message");
        if (messageBroker != null) {
            messageBroker.send(message);
        } else {
            log.error("Message Broker not available while delivering message");
        }
    }

    public Message deliverWithReply(Message message) throws DSPException {
        log.debug("Delivering message with reply.");
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        log.debug("Delivering message with reply w/wait.");
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
        this.shutdownTransportWorkers();
    }

    public String getComponentNodeId() {
        return this.componentNodeId;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        log.info("Initializing component: " + componentNodeId);
        this.dspContext = context;
        this.componentNodeId = componentNodeId;
    }
}
