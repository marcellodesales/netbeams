package org.netbeams.dsp.wiretransport.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.wiretransport.client.model.MessagesQueues;
import org.netbeams.dsp.wiretransport.client.model.QueueMessageData;

/**
 * The DSPWireTransportClientViewOutboundQueuesServlet is used to visualize the Outbound queues registered and their
 * state for debugging and management purposes.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class DSPWireTransportClientViewOutboundQueuesServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DSPWireTransportClientViewOutboundQueuesServlet.class);

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
    public DSPWireTransportClientViewOutboundQueuesServlet(DSPContext dspContext) {
        this.dspContext = dspContext;
        log.debug("DSPContext " + this.dspContext.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // The following generates a page showing all the request parameters
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            
            Map<String, Queue<QueueMessageData>> outboundQueues = MessagesQueues.INSTANCE.getOutboundQueues();
            
            out.println("<html><title>DSP Wire Transport Client Outbound Queues Monitor</title>");
            out.println("<head><meta http-equiv='refresh' content='5'></head><body>");
            out.println("DSP Wire Transport Client Outbound Queues Monitor<BR>");
            this.printBody(out, outboundQueues);
            out.println("</body></html>");
            
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Prints the tables per IP destination with the DSP messages and current state.
     * @param out
     * @param outboundQueues
     */
    private void printBody(PrintWriter out, Map<String, Queue<QueueMessageData>> outboundQueues) {
        Set<String> destinationIps = outboundQueues.keySet();
        if (destinationIps != null) {
            for (String destinationIp : destinationIps) {
                out.println("<table border=1><th colspan='4'><td>Dest: " + destinationIp + "</td></th>");
                for (QueueMessageData messageData : outboundQueues.get(destinationIp)) {
                    out.println("<tr><td>ID: " + messageData.getMessage().getMessageID() + "</td></tr>");
                    out.println("<tr><td>Creation Time: " + messageData.getMessage().getHeader().getCreationTime() + "</td></tr>");
                    out.println("<tr><td>Content Type: " + messageData.getMessage().getContentType() + "</td>");
                    out.println("<tr><td>Producer: " + messageData.getMessage().getHeader().getProducer().getComponentType() + "</td>");
                    out.println("<tr><td>Consumer: " + messageData.getMessage().getHeader().getConsumer().getComponentType() + "</td>");
                    out.println("<tr><td><b>State: " + messageData.getState() + "</b></td></tr>");
                    out.println("<tr><td><pre>"+ messageData.getMessage().getBody().getAny().toXml() +"</pre></td></tr>");
                }
                out.println("</table><BR>");                
            }
        }
    }
}