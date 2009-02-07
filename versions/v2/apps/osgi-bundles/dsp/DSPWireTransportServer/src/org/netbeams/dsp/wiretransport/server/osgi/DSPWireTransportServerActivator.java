/*
 * Created on Mon Feb 02 12:33:01 GMT-03:00
 */
package org.netbeams.dsp.wiretransport.server.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.wiretransport.server.controller.DSPWireTransportServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator class for the WireTransport DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportServerActivator implements BundleActivator {

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPWireTransportServerActivator.class);
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
    private DSPWireTransportServer consumer;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        log.info("WireTransportServer.Activate.start()");
        
        this.bundleContext = bc;

        this.consumer = new DSPWireTransportServer(this.bundleContext);
        this.consumer.startComponent();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        log.info("WireTransport.Activator.stop()");
        this.consumer.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}