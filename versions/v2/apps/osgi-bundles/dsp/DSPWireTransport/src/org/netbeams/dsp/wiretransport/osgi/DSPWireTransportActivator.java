/*
 * Created on Sat Jan 02 12:33:01 GMT-03:00
 */

package org.netbeams.dsp.wiretransport.osgi;

import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator class for the MessagesDirectory DSP component
 * 
 * @author Marcello de Sales
 */
public class DSPWireTransportActivator implements BundleActivator {

    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;
    /**
     * The Messages Directory instance
     */
    private DSPMessagesDirectory messagesDirectory;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        Log.log("MessagesDirectory.Activate.start()");

        this.bundleContext = bc;
        this.messagesDirectory = DSPMessagesDirectory.INSTANCE;
        this.serviceRegistration = ActivatorHelper.registerOSGIService(this.bundleContext, this.messagesDirectory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        Log.log("MessagesDirectory.Activator.stop()");
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
        this.messagesDirectory.stopComponent();
    }
}