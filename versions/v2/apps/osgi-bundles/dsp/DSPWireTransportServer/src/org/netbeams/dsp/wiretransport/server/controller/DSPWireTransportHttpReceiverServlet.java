package org.netbeams.dsp.wiretransport.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.content.AckType;
import org.netbeams.dsp.message.content.AcksContainer;
import org.netbeams.dsp.wiretransport.client.controller.DSPWireTransportHttpClient;
import org.netbeams.dsp.wiretransport.client.model.MessagesQueues;

/**
 * The DSPWireTransportHttpReceiverServlet is the server-side implementation of the DSP Wire transport that is
 * responsible for receiving DSP messages through the wire. It collects messages sent via HTTP POST as XML messages and
 * and forward them to the DSP broker.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class DSPWireTransportHttpReceiverServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DSPWireTransportHttpReceiverServlet.class);

    private static final long serialVersionUID = 1L;
    /**
     * The DSPContext instance to access the DSP broker.
     */
    private DSPContext dspContext;

    /**
     * Creates a new instance of the servlet with the given reference to the messages queue service.
     * 
     * @param dspContext is the reference of the DSP context.
     */
    public DSPWireTransportHttpReceiverServlet(DSPContext dspContext) {
        this.dspContext = dspContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Get the value of a request parameter; the name is case-sensitive
        String messagesContainerXml = req.getParameterValues(System.getProperty("HTTP_SERVER_REQUEST_VARIABLE"))[0];
        MessagesContainer responseMessagesContainer;

        if (messagesContainerXml != null && !"".equals(messagesContainerXml)) {
            try {
                responseMessagesContainer = this.deliverMessagesToLocalDSP(messagesContainerXml);
            } catch (JAXBException e) {
                log.error(e.getMessage(), e);
                throw new IllegalArgumentException(e.getMessage());
            } catch (DSPException e) {
                log.error(e.getMessage(), e);
                throw new IllegalArgumentException(e.getMessage());
            }

        } else {
            log.error("There's no HTTP parameter called " + System.getProperty("HTTP_SERVER_REQUEST_VARIABLE")
                    + " with the Messages Container in xml");
            throw new IllegalArgumentException("The request paramemter "
                    + System.getProperty("HTTP_SERVER_REQUEST_VARIABLE") + "is missing");
        }

        // The following generates a page showing all the request parameters
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/xml");

        try {
            out.println(DSPWireTransportHttpClient.serializeMessagesContainer(responseMessagesContainer));
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
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

        // Deliver all the messages to the DSP Broker component
        MessagesContainer requestMessagesContainer = DSPWireTransportHttpClient
                .deserializeMessagesContainer(messagesContainerRequestXML);
        log.debug("Retrieved messages container from the HTTP request");

        // Send all the messages to the local DSP broker.
        this.send(requestMessagesContainer.getMessage());

        String destIpAddress = requestMessagesContainer.getHost();
        MessagesContainer responseMessagesContainer = MessagesQueues.INSTANCE
                .retrieveQueuedMessagesForTransmission(destIpAddress);
        log.trace("Retrieved the messages container for the given client IP: " + destIpAddress);
        // Adding the acknowledgment message to be sent back to the client
        try {
            Message acknowledgeMessage = null;
            acknowledgeMessage = this.generateAcknowledgmentMessageForRequest(responseMessagesContainer, destIpAddress);
            responseMessagesContainer.getMessage().add(acknowledgeMessage);
            log.debug("Adding an acknowledgement message for the client's request");
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return responseMessagesContainer;
    }

    /**
     * Sends the DSP messages collected from the Servlet to the local DSP broker
     * 
     * @param dspMessages is a list of Messages wrapped into a list of AbstractMessages
     * @throws DSPException if any problem with the DSP occurs
     */
    private void send(List<AbstractMessage> dspMessages) throws DSPException {

        // Always check if there is a broker available
        MessageBrokerAccessor messageBroker = this.dspContext.getDataBroker();
        if (messageBroker != null) {

            log.debug("Sending " + dspMessages.size() + " messages to the broker...");
            for (AbstractMessage msg : dspMessages) {
                messageBroker.send((Message) msg);
            }

        } else {
            log.error("Message broker is not available from the DSP context");
        }
    }

    /**
     * Generates the acknowledgment message for the client with the ID as the correlationId of the header of an instance
     * of Event Message.
     * 
     * @param requestMessagesContainer it's the request messages container from the client. It's necessary to retrieve
     *        the messages container UUID necessary to be sent back as the correlation Id.
     * @param requestUrl it's the request URL, or the identification of the client.
     * @return an instance of the AcknowledgementMessage with the values for the
     * @throws JAXBException if problems with marshalling occurs
     * @throws ParserConfigurationException is any problem with marshalling occurs
     */
    private AcknowledgementMessage generateAcknowledgmentMessageForRequest(MessagesContainer requestMessagesContainer,
            String destIpAddress) throws JAXBException, ParserConfigurationException {

        DSPMessagesFactory bl = DSPMessagesFactory.INSTANCE;

        ComponentIdentifier producer = bl.makeDSPComponentIdentifier(this.getClass().toString(), System
                .getenv("WIRE_TRANSPORT_SERVER_IP"), "org.netbeams.dsp.wiretransport");
        ComponentIdentifier consumer = bl.makeDSPComponentIdentifier(this.getClass().toString(), destIpAddress,
                "org.netbeams.dsp.wiretransport");

        String correlationId = requestMessagesContainer.getUudi();

        Header header = bl.makeDSPMessageHeader(correlationId, producer, consumer);

        AcksContainer acks = new AcksContainer();
        AckType ackt = new AckType();
        ackt.setUuid(requestMessagesContainer.getUudi());
        acks.getAck().add(ackt);

        return bl.makeDSPAcknowledgementMessage(header, acks, org.netbeams.dsp.message.content.ObjectFactory.class);
    }
}