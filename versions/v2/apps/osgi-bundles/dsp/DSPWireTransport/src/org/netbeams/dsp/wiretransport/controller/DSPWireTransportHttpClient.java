package org.netbeams.dsp.wiretransport.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
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
    
    /**
     * The DSPContext defines the external context
     */
    private static DSPContext bc;
    /**
     * The base component is responsible for this producer.
     */
    private static BaseComponent baseComponent;

    
    public DSPWireTransportHttpClient(DSPContext dspBc, BaseComponent component) throws JAXBException {
        bc = dspBc;
        baseComponent = component;
        
        scheduler.scheduleWithFixedDelay(new DspTransportSender(null), 0, 1, TimeUnit.MINUTES);
    }
        
    public DSPWireTransportHttpClient(MessagesContainer messages) throws JAXBException {        
        scheduler.scheduleWithFixedDelay(new DspTransportSender(messages), 0, 10, TimeUnit.SECONDS);
    }
    
    /**
     * @param messages is the messages container instance with the set of messages located at the outbound queue at
     *        the messages directory.
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
//            this.reference = ((org.osgi.framework.BundleContext) bc).getServiceReference(DSPMessagesDirectory.class
//                    .getName());
            //this.messagesDir = (DSPMessagesDirectory) ((org.osgi.framework.BundleContext) bc).getService(reference);
        }

        /**
         * Sends the messagesContainer in xml format using the given UUID, to the given URL
         * @param messagesContainerXml is the marshalled version of the MessagesContainer
         * @param messagesContainerId is the UUID of the messages container used to change the state of the messages
         * on the DSP Messages Queues.
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
            new NameValuePair(TRANSPORT_HTTP_VARIABLE, messagesContainerXml);
            postMethod.setRequestBody(formValues);
             // execute method and handle any error responses.
             
            int statusCode = client.executeMethod(postMethod);

//            // Change the messages to the Transmitted state
//            this.messagesDir.setMessagesToTransmitted(url, messagesContainerId);

            String responseXmlMessagesContainter = null;
            System.out.println(statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                responseXmlMessagesContainter = postMethod.getResponseBodyAsString();
            }
            postMethod.releaseConnection();
            return responseXmlMessagesContainter;
        }

        @Override
        public void run() {

            try {
//                URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL);
                URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL + "/transportDspMessages");
//                MessagesContainer messagesForRequest = messagesDir.retrieveQueuedMessagesForTransmission(url);
                MessagesContainer messagesForRequest = messages;
                String messagesForRequestInXml = serializeMessagesContainer(messagesForRequest);
                System.out.println("HTTP Request BODY: " + messagesForRequestInXml);

                String messagesFromResponseInXml = this.transmitMessagesReceiveResponse(messagesForRequestInXml, UUID
                        .fromString(messagesForRequest.getUudi()), url);
                System.out.println("HTTP Response BODY: " + messagesFromResponseInXml);

                MessagesContainer messagesFromResponse = deserializeMessagesContainer(messagesFromResponseInXml);

                for (AbstractMessage message : messagesFromResponse.getMessage()) {
                    if (message instanceof EventMessage) {
                        this.messagesDir.setMessagesToTransmitted(url, UUID.fromString(message.getHeader()
                                .getCorrelationID().toString()));
                    }
                    // At this point, deliver the messages for the broker through the proxy with
                    // the delivery method of the DSP component.
                    deliver((Message) message);
                }
            } catch (ConnectException e) {
                System.out.println("The DSP server is not available at " + DSPMessagesDirectory.COMPONENTS_SERVER_URL);
            } catch (HttpException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            } catch (JAXBException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            } catch (DSPException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void deliver(Message message) throws DSPException {
        // Always check if there is a broker available
        MessageBrokerAccessor messageBroker = bc.getDataBroker();
        if (messageBroker != null) {
            messageBroker.send(message);
        } else {
            log.error("WireTransport.push: Message Broker not available");
        }
    }

    @Override
    public Message deliverWithReply(Message message) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ComponentDescriptor getComponentDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startComponent() throws DSPException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopComponent() throws DSPException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getComponentNodeId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getComponentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        // TODO Auto-generated method stub

    }
        
    public static void main(String[] args) throws JAXBException {
        System.out.println(UUID.randomUUID());
        JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        MessagesContainer messages = (MessagesContainer)unmarshaller.unmarshal(new File("/tmp/messagesExample.xml"));
        
        DSPWireTransportHttpClient wire = new DSPWireTransportHttpClient(messages);
        
        
    }
}
