package org.netbeams.dsp.wiretransport.server.controller;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.Message;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * DSP component responsible for receiving the DSP messages from a client. It publishes Servlets
 * that are able to receive DSP Messages serialized in XML format.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class DSPWireTransportServer implements DSPComponent {

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPWireTransportServer.class);
    /**
     * The DSP component type used to identify the component in match rules 
     */
    private static final String COMPONENT_TYPE = "org.netbeams.dsp.wiretransport.server";
    /**
     * The component descriptor
     */
    private static final ComponentDescriptor COMPONENT_DESCRIPTOR = null;
    /**
     * The main DSP context used by the DSP Platform
     */
    private DSPContext dspContext;
    /**
     * The DSP Node ID
     */
    private String componentNodeId;
    /**
     * Http Service for the registration of Servlets
     */
    private HttpService httpService;
    /**
     * The service reference to the HTTP service
     */
    private ServiceReference httpSR;
    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    
    /**
     * Creates a new DSP Wire Transport Server consumer with the reference to the HTTP service
     * @param httpServiceReference is the HTTP service reference.
     */
    public DSPWireTransportServer(BundleContext bundleContext) {
        this.bundleContext  = bundleContext;
    }
    
    public void deliver(Message message) throws DSPException {
        log.debug("Delivering Messages to the component");        
    }

    public Message deliverWithReply(Message message) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    public ComponentDescriptor getComponentDescriptor() {
        return COMPONENT_DESCRIPTOR;
    }

    public void startComponent() throws DSPException {
        
        this.httpSR = this.bundleContext.getServiceReference(HttpService.class.getName());
        if (this.httpSR == null) {
            log.error("The Http Service reference could not be retrieved from the OSGi platform!!!");
            throw new IllegalStateException("This DSP Component needs to have the HTTP service running!!!");
        }

        httpService = (HttpService) this.bundleContext.getService(this.httpSR);
        if (this.httpService == null) {
            log.error("The Http Service could not be retrieved from the reference of the OSGi platform!!!");
            throw new IllegalStateException(
                    "The Http Service could not be retrieved from the reference of the OSGi platform!!!");
        }
        
        log.debug("HttpService.class.getName(): " + httpService.getClass().getName());

        // Just print the http service properties
        this.printServiceProperties();
        // Register the servlets for the DSP Wire Transport
        this.registerServlets();
    }
    
    /**
     * Prints the Http server service's properties
     */
    private void printServiceProperties() {
        Object port = this.httpSR.getProperty("port");
        if (port != null) {
            port = port.toString();
            log.debug(port);
        } else {
            log.debug("Ooops - failed to find the port property!!!");
        }
        // Dump the properties as known by the http service
        log.debug("--- DSP Wire Transport HTTP Server Propery keys ---");
        for (String key : httpSR.getPropertyKeys()) {
            log.debug(key + ": --> " + httpSR.getProperty(key));
        }
    }

    /**
     * Registers the HTTP Servlet for the DSP Wire Transport Server-side. The server will be available at the URL:
     * http://SERVER-NAME/transportDspMessages and will accept HTTP POST requests.
     */
    private void registerServlets() {
        try {
            
            log.info("Registering the DSP Wire Transport Messages Receiver Servlet as "
                    + System.getProperty("HTTP_SERVER_BASE_URI"));
            
            this.httpService.registerServlet(System.getProperty("HTTP_SERVER_BASE_URI"),
                    new DSPWireTransportHttpReceiverServlet(this.dspContext), null, null);
            
            log.info("Registering the DSP Wire Transport View Queues Servlet as /viewqueues");
            
            this.httpService.registerServlet("/viewqueues",
                    new DSPWireTransportClientViewOutboundQueuesServlet(this.dspContext), null, null);
            
        } catch (ServletException e) {
            log.error(e.getMessage(), e);
        } catch (NamespaceException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void stopComponent() throws DSPException {
        log.info("DSPWireTransportServerConsumer.stopComponent()");
        this.httpService.unregister(System.getProperty("HTTP_SERVER_BASE_URI"));
    }

    public String getComponentNodeId() {
        return this.componentNodeId;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        this.componentNodeId = componentNodeId;
        this.dspContext = context;   
    }
}