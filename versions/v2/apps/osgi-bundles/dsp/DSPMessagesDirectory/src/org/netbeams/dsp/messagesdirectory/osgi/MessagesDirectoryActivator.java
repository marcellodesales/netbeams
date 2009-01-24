/*
 * Created on Sat Jan 02 12:33:01 GMT-03:00
 */

package org.netbeams.dsp.messagesdirectory.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.wiretransport.osgi.DSPWireTransportActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator class for the MessagesDirectory DSP component
 * 
 * @author Marcello de Sales
 */
public class MessagesDirectoryActivator implements BundleActivator {
    
    private static final Logger log = Logger.getLogger(DSPWireTransportActivator.class);

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
        log.info("MessagesDirectory.Activate.start()");

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
        log.info("MessagesDirectory.Activator.stop()");
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
        this.messagesDirectory.stopComponent();
    }
}