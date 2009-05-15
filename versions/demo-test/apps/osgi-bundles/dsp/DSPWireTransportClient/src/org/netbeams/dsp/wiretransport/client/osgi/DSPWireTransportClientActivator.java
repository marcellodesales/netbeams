/*
 * Created on Sat Jan 02 12:33:01 GMT-03:00
 */

package org.netbeams.dsp.wiretransport.client.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.wiretransport.client.controller.DSPWireTransportHttpClient;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator class for the WireTransport DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportClientActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(DSPWireTransportClientActivator.class);

    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;
    /**
     * The reference to the Wire Transport client
     */
    private DSPWireTransportHttpClient client;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        log.info("WireTransportClient.Activate.start()");
        this.bundleContext = bc;
        this.client = new DSPWireTransportHttpClient();
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, this.client);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        log.info("WireTransportClient.Activator.stop()");
        this.client.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}