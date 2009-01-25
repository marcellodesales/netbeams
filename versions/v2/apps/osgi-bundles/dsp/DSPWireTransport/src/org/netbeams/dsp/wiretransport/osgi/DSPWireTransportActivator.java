/*
 * Created on Sat Jan 02 12:33:01 GMT-03:00
 */

package org.netbeams.dsp.wiretransport.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpClient;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpReceiverServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Activator class for the WireTransport DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(DSPWireTransportActivator.class);

    /**
     * Http Service for the DSP Wire Transport HTTP Server
     */
    private HttpService httpService;

    private ServiceReference httpSR;

    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;

    private DSPWireTransportHttpClient client;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        log.info("WireTransport.Activate.start()");
        this.bundleContext = bc;

        this.httpSR = bc.getServiceReference(HttpService.class.getName());
        if (this.httpSR == null) {
            log.error("The Http Service reference could not be retrieved from the OSGi platform!!!");
            throw new IllegalStateException("This DSP Component needs to have the HTTP service running!!!");
        }
        
        httpService = (HttpService) bc.getService(this.httpSR);
        if (this.httpService == null) {
            log.error("The Http Service could not be retrieved from the reference of the OSGi platform!!!");
            throw new IllegalStateException("The Http Service could not be retrieved from the reference of the OSGi platform!!!");
        }
        log.debug("HttpService.class.getName(): " + HttpService.class.getName());

        // Just print the service properties
        this.printServiceProperties();
        // Register the servlets for the DSP Wire Transport
        this.registerServlets();

        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            log.debug("Could not find the name of this host for the network service... Using 'localhost'...");
            host = "localhost";
        }
        log.info("DSP Wire Transport Service Available...");
        log.info("HTTP POST requests as XML to http://" + host + ":8080/transportDspMessages");

        ServiceReference sr = bc.getServiceReference(DSPMessagesDirectory.class.getName());
        DSPMessagesDirectory messagesQueue = (DSPMessagesDirectory) bc.getService(sr);
        this.client = new DSPWireTransportHttpClient(messagesQueue);
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, this.client);
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
        log.debug("--- DSP Wire Transport Http Server Propery keys ---");
        for (String key : httpSR.getPropertyKeys()) {
            log.debug(key + ": --> " + httpSR.getProperty(key));
        }
    }

    /**
     * Registers the HTTP Servlet for the DSP Wire Transport Server-side. The server will be available at the URL:
     * http://SERVER-NAME/transportDspMessages and will accept HTTP POST requests.
     * 
     * @see DSPWireTransportHttpReceiverServlet.
     */
    private void registerServlets() {
        String aliasTransport = "/transportDspMessages";
        // / Since the HTTP Service is available from
        // "http://localhost:8080"
        try {
            log.info("Registering the DSP Wire Transport Messages Receiver Servlet as /transportDspMessages");
            httpService.registerServlet(aliasTransport, new DSPWireTransportHttpReceiverServlet(this.bundleContext),
                    null, null);
        } catch (ServletException e) {
            log.error(e.getMessage(), e);
        } catch (NamespaceException e) {
            log.error(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        log.info("WireTransport.Activator.stop()");
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
        this.client.stopComponent();
    }
}