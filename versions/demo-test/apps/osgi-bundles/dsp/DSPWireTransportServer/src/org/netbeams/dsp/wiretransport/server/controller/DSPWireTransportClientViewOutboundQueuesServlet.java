package org.netbeams.dsp.wiretransport.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
     * 
     * @param out
     * @param outboundQueues
     * @throws TransformerException if there's any error with the XML transformation for the output
     */
    private void printBody(PrintWriter out, Map<String, Queue<QueueMessageData>> outboundQueues)
            throws TransformerException {
        Set<String> destinationIps = outboundQueues.keySet();
        if (destinationIps != null) {
            for (String destinationIp : destinationIps) {
                out.println("<table border=1><th colspan='4'><td>Dest: " + destinationIp + "</td></th>");
                for (QueueMessageData messageData : outboundQueues.get(destinationIp)) {
                    out.println("<tr><td>ID: " + messageData.getMessage().getMessageID() + "</td></tr>");
                    out.println("<tr><td>Creation Time: " + messageData.getMessage().getHeader().getCreationTime()
                            + "</td></tr>");
                    out.println("<tr><td>Content Type: " + messageData.getMessage().getContentType() + "</td>");
                    out.println("<tr><td>Producer: "
                            + messageData.getMessage().getHeader().getProducer().getComponentType() + "</td>");
                    out.println("<tr><td>Consumer: "
                            + messageData.getMessage().getHeader().getConsumer().getComponentType() + "</td>");
                    out.println("<tr><td><b>State: " + messageData.getState() + "</b></td></tr>");
                    out.println("<tr><td>");
                    formatXml(messageData.getMessage().getBody().getAny().toXml(), out);
                    out.println("</td></tr>");
                }
                out.println("</table><BR>");
            }
        }
    }

    public static void formatXml(String xmlDocument, Writer writer) throws TransformerException {

        Reader xmlReader = new StringReader(xmlDocument);
        Reader xslReader = new StringReader(
                "<?xml version=\"1.0\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns=\"http://www.w3.org/1999/xhtml\"><xsl:output method=\"html\" encoding=\"Windows-1252\"/><xsl:template match=\"/\"><style type=\"text/css\">*|*:root {background: url(viewsource-bg.jpg) top center no-repeat white;}#viewsource {font-family: -moz-fixed;font-weight: normal;color: white;white-space: pre;}#viewsource.wrap {white-space: pre-wrap;}pre {font: inherit;color: inherit;white-space: inherit;margin: 0;}.start-tag { color: #ff5353; font-weight: bold;}.end-tag { color: #ff5353; font-weight: bold;}.comment { color: #86d98f; font-style: italic;}.cdata { color: #CC0066;}.doctype { color: steelblue; font-style: italic;}.pi { color: orchid; font-style: italic;}.entity { color:#FF4500; font-weight: normal;}.text {font-weight: normal;}.attribute-name { color: blue; font-weight: bold;}.attribute-value { color: black; font-weight: normal;}.summary { display: block; background-color: #FFFFCC; width: 90%; border: solid; border-width: 1pt; font-family: sans-serif;}.popup {font-weight: normal;}.markup { color: blue; font-style: italic;}.error, .error > .start-tag, .error > .end-tag,.error > .comment, .error > .cdata, .error > .doctype,.error > .pi, .error > .entity, .error > .attribute-name,.error > .attribute-value {color: red;font-weight: bold;}/***/#header {background-color: #ccc;border-bottom: 3px solid black;padding: 0.5em;margin-bottom: 1em;}table {border-spacing: 0;margin: 0;}td {padding: 0;}.indent {margin-left: 1em;}.spacer {width: 1em;}.expander {cursor: pointer;-moz-user-select: none;vertical-align: top;text-align: center;}.expander-closed > * > .expander-content {display: none;}.comment {font-family: monospace;white-space: pre;}</style><xsl:apply-templates/></xsl:template><xsl:template match=\"*\"><div class=\"indent\"><span class=\"markup\">&lt;</span><span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span><xsl:apply-templates select=\"@*\"/><span class=\"markup\">/&gt;</span></div></xsl:template><xsl:template match=\"*[*]\"><table><tr> <xsl:call-template name=\"expander\"/>  <td>    <span class=\"markup\">&lt;</span>      <span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span>     <xsl:apply-templates select=\"@*\"/>    <span class=\"markup\">&gt;</span>      <div class=\"expander-content\"><xsl:apply-templates/></div>    <span class=\"markup\">&lt;/</span>     <span class=\"end-tag\"><xsl:value-of select=\"name(.)\"/></span>       <span class=\"markup\">&gt;</span>      </td></tr></table></xsl:template><xsl:template match=\"*[processing-instruction()]\"><table><tr>        <xsl:call-template name=\"expander\"/>  <td>    <span class=\"markup\">&lt;</span>      <span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span>     <xsl:apply-templates select=\"@*\"/>    <span class=\"markup\">&gt;</span>      <div class=\"expander-content\"><xsl:apply-templates/></div>    <span class=\"markup\">&lt;/</span>     <span class=\"end-tag\"><xsl:value-of select=\"name(.)\"/></span>       <span class=\"markup\">&gt;</span>      </td></tr></table></xsl:template><xsl:template match=\"*[node()]\">     <xsl:call-template name=\"expander\"/>          <span class=\"markup\">&lt;</span>      <span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span>     <xsl:apply-templates select=\"@*\"/>    <span class=\"markup\">&gt;</span>      <div class=\"expander-content\"><xsl:apply-templates/></div>    <span class=\"markup\">&lt;/</span>     <span class=\"end-tag\"><xsl:value-of select=\"name(.)\"/></span>       <span class=\"markup\">&gt;</span></xsl:template><xsl:template match=\"*[string-length(.) &gt; 50]\">   <xsl:call-template name=\"expander\"/>  <span class=\"markup\">&lt;</span>      <span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span>     <xsl:apply-templates select=\"@*\"/>    <span class=\"markup\">&gt;</span>      <div class=\"expander-content\"><xsl:apply-templates/></div>    <span class=\"markup\">&lt;/</span>     <span class=\"end-tag\"><xsl:value-of select=\"name(.)\"/></span>       <span class=\"markup\">&gt;</span></xsl:template><xsl:template match=\"*[comment()]\"><table><tr>       <xsl:call-template name=\"expander\"/>  <td>    <span class=\"markup\">&lt;</span>      <span class=\"start-tag\"><xsl:value-of select=\"name(.)\"/></span>     <xsl:apply-templates select=\"@*\"/>    <span class=\"markup\">&gt;</span>      <div class=\"expander-content\"><xsl:apply-templates/></div>    <span class=\"markup\">&lt;/</span>     <span class=\"end-tag\"><xsl:value-of select=\"name(.)\"/></span>       <span class=\"markup\">&gt;</span>      </td></tr></table></xsl:template><xsl:template match=\"@*\"><xsl:text> </xsl:text><span class=\"attribute-name\"><xsl:value-of select=\"name(.)\"/></span><span class=\"markup\">=</span><span class=\"attribute-value\">\"<xsl:value-of select=\".\"/>\"</span></xsl:template><xsl:template match=\"text()\"><xsl:if test=\"normalize-space(.)\"><div class=\"indent text\"><xsl:value-of select=\".\"/></div></xsl:if></xsl:template><xsl:template match=\"processing-instruction()\"><div class=\"indent pi\"><xsl:text>&lt;?</xsl:text><xsl:value-of select=\"name(.)\"/><xsl:text> </xsl:text><xsl:value-of select=\".\"/><xsl:text>?&gt;</xsl:text></div></xsl:template><xsl:template match=\"processing-instruction()[string-length(.) &gt; 50]\"><table><tr>    <xsl:call-template name=\"expander\"/>  <td class=\"pi\">       &lt;?<xsl:value-of select=\"name(.)\"/> <div class=\"indent expander-content\"><xsl:value-of select=\".\"/></div>       <xsl:text>?&gt;</xsl:text>      </td></tr></table></xsl:template><xsl:template match=\"comment()\"><div class=\"comment indent\"><xsl:text>&lt;!--</xsl:text><xsl:value-of select=\".\"/><xsl:text>--&gt;</xsl:text></div></xsl:template><xsl:template match=\"comment()[string-length(.) &gt; 50]\"><table><tr>        <xsl:call-template name=\"expander\"/>  <td class=\"comment\">  <xsl:text>&lt;!--</xsl:text>    <div class=\"indent expander-content\"><xsl:value-of select=\".\"/></div>       <xsl:text>--&gt;</xsl:text>     </td></tr></table></xsl:template><xsl:template name=\"expander\"><div class=\"expander-display-open\" onclick=\"x(this);\"><div class=\"spacer\"></div></div></xsl:template></xsl:stylesheet>");

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(xmlReader);
        Source xsltSource = new StreamSource(xslReader);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans;
        trans = transFact.newTransformer(xsltSource);
        trans.transform(xmlSource, new StreamResult(writer));
    }
}