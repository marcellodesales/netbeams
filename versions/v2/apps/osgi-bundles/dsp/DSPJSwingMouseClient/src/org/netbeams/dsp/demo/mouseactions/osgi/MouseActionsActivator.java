package org.netbeams.dsp.demo.mouseactions.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Activator for the Mouse Actions
 * 
 * @author marcello de sales <marcello.sales@gmail.com>
 */
public class MouseActionsActivator implements BundleActivator {
    
    private static final Logger log = Logger.getLogger(MouseActionsActivator.class);
    /**
     * Regular OSGi BundleContext instance
     */
    private BundleContext bundleContext;
    /**
     * Service registration for the mouse application
     */
    private ServiceRegistration serviceRegistration;
    /**
     * The DSP component that produces Mouse Actions through a JFrame
     */
    private MouseActionDSPComponent producer;

    // @Override
    public void start(BundleContext bc) throws Exception {
        log.info("MouseActions.Activate.start()");
        this.bundleContext = bc;

        log.debug("Retrieving the Messages Directory service for the Mouse Actions Client...");
        ServiceReference sr = bc.getServiceReference(DSPMessagesDirectory.class.getName());
        DSPMessagesDirectory messagesQueue = (DSPMessagesDirectory) bc.getService(sr);
        
        if (messagesQueue == null) {
            log.error("The Messages queue service is not available for testing...");
        }
        
        log.debug("Instantiating the mouse actions component...");
        this.producer = new MouseActionDSPComponent(messagesQueue);
        this.producer.startComponent();
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bc, this.producer);

    }

    // @Override
    public void stop(BundleContext bc) throws Exception {
        log.info("MouseActions.Activator.stop()");
        this.producer.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}
