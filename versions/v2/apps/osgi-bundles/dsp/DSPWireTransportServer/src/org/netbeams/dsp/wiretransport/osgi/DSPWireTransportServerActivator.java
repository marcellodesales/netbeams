/*
 * Created on Mon Feb 02 12:33:01 GMT-03:00
 */
package org.netbeams.dsp.wiretransport.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpReceiverServlet;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportServerConsumer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

/**
 * Activator class for the WireTransport DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportServerActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(DSPWireTransportServerActivator.class);

    /**
     * Http Service for the DSP Wire Transport HTTP Server
     */
    private HttpService httpService;
    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;
    /**
     * The service reference to the DSP consumer 
     */
    private DSPWireTransportServerConsumer consumer;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        log.info("WireTransportServer.Activate.start()");
        
        this.bundleContext = bc;

        this.consumer = new DSPWireTransportServerConsumer(this.bundleContext);
        this.consumer.startComponent();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        log.info("WireTransport.Activator.stop()");
        this.httpService.unregister(DSPWireTransportHttpReceiverServlet.BASE_URI);
        this.consumer.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}