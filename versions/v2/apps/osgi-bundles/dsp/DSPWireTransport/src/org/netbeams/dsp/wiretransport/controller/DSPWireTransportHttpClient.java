package org.netbeams.dsp.wiretransport.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URL;
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
import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.EventMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DSPWireTransportHttpClient implements DSPComponent {

    private static final Logger log = Logger.getLogger(DSPWireTransportHttpClient.class);

    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * HTTP POST request variable used to submit the MessagesContainer.
     */
    public static final String TRANSPORT_HTTP_VARIABLE = "dspMessagesContainer";

    private static final String COMPONENT_TYPE = "org.netbeams.dsp.wiretransport";

    private static final ComponentDescriptor componentDescriptor = null;

    /**
     * The DSPContext defines the external context
     */
    private BundleContext bc;
    private DSPContext dspContext;
    /**
     * The DSP Node ID
     */
    private String componentNodeId;
    /**
     * The base component is responsible for this producer.
     */
    private static BaseComponent baseComponent;

    public DSPWireTransportHttpClient(BundleContext bc) throws JAXBException {
        this.bc = bc;
        log.info("Starting the DSP Wire Transport locally...");
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
        JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
        Marshaller m = context.createMarshaller();
        StringWriter output = new StringWriter();
        m.marshal(messages, output);
        return output.toString();
    }

    /**
     * @param responseStream the marshalled version of the Messages Container
     * @return the Messages Container instance frmo the the xml version of the message
     * @throws JAXBException if any problem with the unmarshall operation occurs.
     */
    public static MessagesContainer deserializeMessagesContainer(String responseStream) throws JAXBException {
        InputStream input = new ByteArrayInputStream(responseStream.getBytes());
        JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
        Unmarshaller um = context.createUnmarshaller();
        return (MessagesContainer) um.unmarshal(input);
    }

    /**
     * The DSPMouseWorker implements a pattern of a given worker thread that is used at different types by the Executor.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspTransportSender extends Thread {

        /**
         * Reference to the service
         */
        private ServiceReference reference;
        /**
         * Reference to the DSP Messages Directory service
         */
        private DSPMessagesDirectory messagesDir;

        private MessagesContainer messages;

        /**
         * Initializes the transport sender with service reference to the Messages Directory.
         */
        public DspTransportSender(MessagesContainer messages) {
            this.messages = messages;
            this.reference =  bc.getServiceReference(DSPMessagesDirectory.class.getName());
            this.messagesDir = (DSPMessagesDirectory) bc.getService(reference);
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
            formValues[0] = new NameValuePair(TRANSPORT_HTTP_VARIABLE, messagesContainerXml);
            postMethod.setRequestBody(formValues);
            // execute method and handle any error responses.

            log.trace("Trying send the container " + messagesContainerId + " to the DSP Wire Transport Server located"
                    + "at " + dest.toString());
            int statusCode = client.executeMethod(postMethod);

            // // Change the messages to the Transmitted state
            // this.messagesDir.setMessagesToTransmitted(url, messagesContainerId);

            String responseXmlMessagesContainter = null;
            System.out.println(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                responseXmlMessagesContainter = postMethod.getResponseBodyAsString();
                log.trace("The DSP Transport Server responded with messages " + dest.toString());
                log.trace(responseXmlMessagesContainter);
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                responseXmlMessagesContainter = null;
                log.error("The server is running, but there's no service at " + dest.toString());
            }
            postMethod.releaseConnection();
            return responseXmlMessagesContainter;
        }

        @Override
        public void run() {

            try {
                // URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL);
                URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL + "/transportDspMessages");

                MessagesContainer messagesForRequest = this.messages != null ? this.messages : messagesDir
                        .retrieveQueuedMessagesForTransmission(url);
                if (messagesForRequest.getMessage().size() > 0) {
                    String messagesForRequestInXml = serializeMessagesContainer(messagesForRequest);
                    log.trace("HTTP Request BODY: " + messagesForRequestInXml);

                    String messagesFromResponseInXml = this.transmitMessagesReceiveResponse(messagesForRequestInXml,
                            UUID.fromString(messagesForRequest.getUudi()), url);
                    log.trace("HTTP Response BODY: " + messagesFromResponseInXml);

                    if (messagesFromResponseInXml != null) {
                        MessagesContainer messagesFromResponse = deserializeMessagesContainer(messagesFromResponseInXml);
                        for (AbstractMessage message : messagesFromResponse.getMessage()) {
                            if (message instanceof EventMessage) {
                                log.trace("Received Acknowledge message with correlation ID = "
                                        + message.getHeader().getCorrelationID());
                                this.messagesDir.setMessagesToTransmitted(url, UUID.fromString(message.getHeader()
                                        .getCorrelationID().toString()));
                            } else {
                                // At this point, deliver the messages for the broker through the proxy with
                                // the delivery method of the DSP component.
                                deliver((Message) message);
                                log.trace("Delivering message ID " + message.getMessageID() + " type "
                                        + message.getContentType() + " to the DSP broker...");
                            }
                        }
                    } else {
                        log.trace("No messages received from the HTTP response...");
                    }
                }

            } catch (ConnectException e) {
                log.error("There's no HTTP server available at " + DSPMessagesDirectory.COMPONENTS_SERVER_URL);
                // if (++notAvailable == 5) {
                // System.out.println("Server is not responding... terminating the service...");
                // shutdownTransportWorker();
                // }

            } catch (HttpException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (JAXBException e) {
                log.error(e.getMessage(), e);
            } catch (DSPException e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Shuts down all the threads started by the scheduler.
     */
    private void shutdownTransportWorkers() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("shutting down all transport workers...");
        }
    }

    public void deliver(Message message) throws DSPException {
        // Always check if there is a broker available
        MessageBrokerAccessor messageBroker = this.dspContext.getDataBroker();
        if (messageBroker != null) {
            messageBroker.send(message);
        } else {
            log.error("WireTransport.push: Message Broker not available");
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
        return componentDescriptor;
    }

    public void startComponent() throws DSPException {
        log.info("Starting component");
        baseComponent.getComponentNodeId();
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

//    public static void main(String[] args) throws JAXBException {
//        System.out.println(UUID.randomUUID());
//        JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message");
//        Unmarshaller unmarshaller = jc.createUnmarshaller();
//        MessagesContainer messages = (MessagesContainer) unmarshaller.unmarshal(new File("/tmp/messagesExample.xml"));
//
//        DSPWireTransportHttpClient wire = new DSPWireTransportHttpClient(messages);
//
//    }
}
