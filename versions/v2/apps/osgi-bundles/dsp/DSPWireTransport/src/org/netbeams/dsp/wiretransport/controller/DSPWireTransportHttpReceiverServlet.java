package org.netbeams.dsp.wiretransport.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.EventMessage;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DSPWireTransportHttpReceiverServlet extends HttpServlet {

    private BundleContext bc;

    public DSPWireTransportHttpReceiverServlet(BundleContext bc) {
        this.bc = bc;
    }

    private static final long serialVersionUID = 1L;

    // This method handles both GET and POST requests.
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        // Get the value of a request parameter; the name is case-sensitive
        String messagesContainerXml = req.getParameterValues(DSPWireTransportHttpClient.TRANSPORT_HTTP_VARIABLE)[0];
        if (messagesContainerXml != null && !"".equals(messagesContainerXml)) {
            try {
                this.deliverMessagesToLocalDSP(messagesContainerXml);
            } catch (JAXBException e) {
                throw new IllegalArgumentException(e.getMessage());
            } catch (DSPException e) {
                throw new IllegalArgumentException(e.getMessage());
            }

        } else {
            throw new IllegalArgumentException("The request paramemter " + DSPWireTransportHttpClient.TRANSPORT_HTTP_VARIABLE
                    + "is missing");
        }

        // The following generates a page showing all the request parameters
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/xml");
        MessagesContainer responseMessagesContainer;
        try {
            responseMessagesContainer = this.deliverMessagesToLocalDSP(messagesContainerXml);
            out.println(DSPWireTransportHttpClient.serializeMessagesContainer(responseMessagesContainer));
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (DSPException e) {
            e.printStackTrace();
        }        
        out.close();
    }

    /**
     * This method is responsible for achieving 2 important steps: <BR>
     * 1. The messages received in the XML format is unmarshalled and all the messages on the container must be
     * delivered to the DSP Broker via the DSP Wire Transport itself. <BR>
     * 2. Generate the Messages Container of outbound messages from the server.
     * 
     * @param messagesContainerRequestXML is the MessagesContainer instance in XML sent by another DSP component.
     * @return the MessagesContainer instance from the outbound messages registered in the local DSP Messages Queue.
     *         Also, the messages returned can include EventMessages that are acknowledgment of the reception of the
     *         messages.
     * @throws JAXBException if any unmarshalling error occurs.
     * @throws DSPException if any problem occur with the delivery of messages to the DSP Broker via the Broker.
     */
    private MessagesContainer deliverMessagesToLocalDSP(String messagesContainerRequestXML) throws JAXBException,
            DSPException {

        //Deliver all the messages to the DSP Broker component
        MessagesContainer requestMessagesContainer = DSPWireTransportHttpClient.deserializeMessagesContainer(
                                                                                  messagesContainerRequestXML);
        ServiceReference transportService = bc.getServiceReference(DSPWireTransportHttpClient.class.getName());
        DSPWireTransportHttpClient dspWireTransport = (DSPWireTransportHttpClient) bc.getService(transportService);
        for (AbstractMessage msg : requestMessagesContainer.getMessage()) {
            dspWireTransport.deliver((Message) msg);
        }
        
        //Retrieve the outbound messages from the Messages Queues for the client DSP component
        ServiceReference messagesQueueService = bc.getServiceReference(DSPMessagesDirectory.class.getName());
        DSPMessagesDirectory messagesQueue = (DSPMessagesDirectory) bc.getService(messagesQueueService);
        MessagesContainer responseMessagesContainer = null;
        URL requestUrl = null;
        try {
            requestUrl = new URL(requestMessagesContainer.getHost());
            responseMessagesContainer = messagesQueue.retrieveQueuedMessagesForTransmission(requestUrl);
        } catch (MalformedURLException e) {
            responseMessagesContainer = messagesQueue.retrieveQueuedMessagesForTransmission(null);
        }

        // Adding the acknowledgment message to be sent back to the client
        try {
            Message acknowledgeMessage = null;
            acknowledgeMessage = this.generateAcknowledgmentMessageForRequest(requestMessagesContainer, requestUrl);
            responseMessagesContainer.getMessage().add(acknowledgeMessage);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return responseMessagesContainer;
    }

    /**
     * Generates the acknowledgment message for the client with the ID as the correlationId of the header of
     * an instance of Event Message.
     * @param requestMessagesContainer it's the request messages container from the client. It's necessary to retrieve
     * the messages container UUID necessary to be sent back as the correlation Id.
     * @param requestUrl it's the request URL, or the identification of the client.
     * @return 
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    private EventMessage generateAcknowledgmentMessageForRequest(MessagesContainer requestMessagesContainer, URL requestUrl)
            throws JAXBException, ParserConfigurationException {
        String compLocation = "";
        try {
            compLocation = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            compLocation = "dsp-server";
        }
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(this.getClass()
                .toString(), compLocation, "org.netbeams.dsp.wiretransport");
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(this.getClass()
                .toString(), requestUrl.toString(), "org.netbeams.dsp.wiretransport");
        String correlationId = requestMessagesContainer.getUudi();
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(correlationId, System.currentTimeMillis(),
                producer, consumer);
        return DSPMessagesFactory.INSTANCE.makeDSPEventMessage(UUID.randomUUID().toString(),
                "org.netbeams.dsp.message", header, new Body());
    }
}