package org.netbeams.dsp.wiretransport.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
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

public class DSPWireTransport implements DSPComponent {
    
    private static final Logger log = Logger.getLogger(DSPWireTransport.class);

    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * The DSPContext defines the external context
     */
    private static DSPContext bc;
    /**
     * The base component is responsible for this producer.
     */
    private static BaseComponent baseComponent;

    public DSPWireTransport(DSPContext dspBc, BaseComponent component) {
        bc = dspBc;
        baseComponent = component;
        scheduler.scheduleWithFixedDelay(new DspTransportSender(), 0, 1, TimeUnit.MINUTES);
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

        /**
         * Initializes the transport sender with service reference to the Messages Directory.
         */
        public DspTransportSender() {
            this.reference = ((org.osgi.framework.BundleContext) bc).getServiceReference(DSPMessagesDirectory.class
                    .getName());
            this.messagesDir = (DSPMessagesDirectory) ((org.osgi.framework.BundleContext) bc).getService(reference);
        }

        /**
         * @param messages is the messages container instance with the set of messages located at the outbound queue at
         *        the messages directory.
         * @return The xml version of the DSP Messages of the given container.
         * @throws JAXBException if any problem with regards to the marshall operation happens.
         */
        private String serializeMessagesContainer(MessagesContainer messages) throws JAXBException {
            JAXBContext context = JAXBContext.newInstance(MessagesContainer.class);
            Marshaller m = context.createMarshaller();
            StringWriter output = new StringWriter();
            m.marshal(messages, output);
            return output.toString();
        }

        /**
         * @param responseStream the input stream of the
         * @return
         * @throws JAXBException
         */
        private MessagesContainer deserializeMessagesContainer(String responseStream) throws JAXBException {
            ByteArrayInputStream input = new ByteArrayInputStream(responseStream.getBytes());
            JAXBContext context = JAXBContext.newInstance(MessagesContainer.class);
            Unmarshaller um = context.createUnmarshaller();
            return (MessagesContainer) um.unmarshal(input);
        }

        private String transmitMessagesReceiveResponse(String messagesContainerXml, UUID messagesContainerId)
                throws HttpException, IOException, JAXBException, DSPException {
            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod(DSPMessagesDirectory.COMPONENTS_SERVER_URL + "/receiveDspMessages");

            client.setConnectionTimeout(8000);
            postMethod.setRequestBody(messagesContainerXml);
            postMethod.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");
            int statusCode = client.executeMethod(postMethod);

            // Change the messages to the Transmitted state
            URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL);
            this.messagesDir.setMessagesToTransmitted(url, messagesContainerId);

            String messages = null;

            if (statusCode == HttpStatus.SC_NOT_IMPLEMENTED) {

            } else if (statusCode == HttpStatus.SC_OK) {

                messages = postMethod.getResponseBodyAsString();
            }
            postMethod.releaseConnection();
            return messages;
        }

        @Override
        public void run() {

            try {
                URL url = new URL(DSPMessagesDirectory.COMPONENTS_SERVER_URL);
                MessagesContainer messagesForRequest = messagesDir.retrieveQueuedMessagesForTransmission(url);

                String messagesForRequestInXml = this.serializeMessagesContainer(messagesForRequest);
                System.out.println("HTTP Request BODY: " + messagesForRequestInXml);

                String messagesFromResponseInXml = this.transmitMessagesReceiveResponse(messagesForRequestInXml, UUID
                        .fromString(messagesForRequest.getUudi()));
                System.out.println("HTTP Response BODY: " + messagesFromResponseInXml);

                MessagesContainer messagesFromResponse = this.deserializeMessagesContainer(messagesFromResponseInXml);

                for (AbstractMessage message : messagesFromResponse.getMessage()) {
                    if (message instanceof EventMessage) {
                        this.messagesDir.setMessagesToTransmitted(url, UUID.fromString(message.getHeader()
                                .getCorrelationID().toString()));
                    }
                    // At this point, deliver the messages for the broker through the proxy with
                    // the delivery method of the DSP component.
                    deliver((Message) message);
                }

            } catch (HttpException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (JAXBException e) {
                log.error(e.getMessage(), e);
            } catch (DSPException e) {
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
}
