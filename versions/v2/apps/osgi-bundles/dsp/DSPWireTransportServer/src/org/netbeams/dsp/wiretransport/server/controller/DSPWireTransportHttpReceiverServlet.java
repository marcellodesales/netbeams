package org.netbeams.dsp.wiretransport.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
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
                log.debug("Messages Container for the response is ready to be serialized..."
                        + responseMessagesContainer);
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

        try {
            // The following generates a page showing all the request parameters
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/xml");
            out.println(DSPWireTransportHttpClient.serializeMessagesContainer(responseMessagesContainer));
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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

        log.debug("Retrieved messages container from the HTTP request... Deserializing the XML from the client...");
        // Deliver all the messages to the DSP Broker component
        MessagesContainer requestMessagesContainer = DSPWireTransportHttpClient
                .deserializeMessagesContainer(messagesContainerRequestXML);

        log.debug("Delivering messages to the DSP Broker...");
        // Send all the messages to the local DSP broker.
        this.send(requestMessagesContainer.getMessage());

        String destIpAddress = requestMessagesContainer.getDestinationHost();
        log.debug("Retrieving the messages container for the given client IP: " + destIpAddress);
        MessagesContainer responseMessagesContainer = MessagesQueues.INSTANCE
                .retrieveQueuedMessagesForTransmission(destIpAddress);

        log.debug("Acknowledment will be sent to the destination IP " + destIpAddress);
        Integer ackUntil = Integer.valueOf(requestMessagesContainer.getMessage().get(
                requestMessagesContainer.getMessage().size() - 1).getMessageID());
        log.debug("Last message ID received is " + ackUntil);
        responseMessagesContainer.setAcknowledgeUntil(ackUntil);
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
}