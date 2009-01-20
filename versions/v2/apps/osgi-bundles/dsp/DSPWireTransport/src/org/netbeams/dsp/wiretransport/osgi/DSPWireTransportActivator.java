/*
 * Created on Sat Jan 02 12:33:01 GMT-03:00
 */

package org.netbeams.dsp.wiretransport.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;

import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpReceiverServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Activator class for the MessagesDirectory DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportActivator implements BundleActivator {

    private static HttpService httpService;

    private ServiceReference httpSR;

    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        Log.log("MessagesDirectory.Activate.start()");
        this.bundleContext = bc;

        // ServiceReference reference = bc.getServiceReference(HttpService.class.getName());
        // HttpService service = (HttpService) bc.getService(reference);

        this.httpSR = bc.getServiceReference(HttpService.class.getName());
        httpService = (HttpService) bc.getService(this.httpSR);

        System.out.println("HttpService.class.getName(): " + HttpService.class.getName());

        // Just print the service properties
        this.printServiceProperties();
        // Register the servlets for the DSP Wire Transport
        this.registerServlets();

        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            host = "localhost";
        }
        System.out.println("DSP Wire Transport Service Available:");
        System.out.println("Transport Messages Container XML: http://" + host + ":8080/transportDspMessages");
    }

    private void printServiceProperties() {
        Object port = this.httpSR.getProperty("port");
        if (port != null) {
            port = port.toString();
        } else {
            System.out.println("Ooops - failed to find the port property!!!");
        }
        // Dump the properties as known by the http service
        System.out.println("--- Propery keys ---");
        for (String key : httpSR.getPropertyKeys()) {
            System.out.println(key + ": --> " + httpSR.getProperty(key));
        }
    }

    private void registerServlets() {
        String aliasTransport = "/transportDspMessages";
        // / Since the HTTP Service is available from
        // "http://localhost:8080"
        try {
            httpService.registerServlet(aliasTransport, new DSPWireTransportHttpReceiverServlet(bundleContext), null,
                    null);

        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        Log.log("MessagesDirectory.Activator.stop()");
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}