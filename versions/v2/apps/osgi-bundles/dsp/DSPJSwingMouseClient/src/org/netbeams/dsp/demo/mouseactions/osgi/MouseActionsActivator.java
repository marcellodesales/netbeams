package org.netbeams.dsp.demo.mouseactions.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Activator for the Mouse Actions
 * 
 * @author Marcello de Sales <marcello.sales@gmail.com>
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

    public void start(BundleContext bc) throws Exception {
        log.info("MouseActions.Activate.start()");
        this.bundleContext = bc;
                
        log.debug("Instantiating the mouse actions component...");
        this.producer = new MouseActionDSPComponent();
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bc, this.producer);
    }

    public void stop(BundleContext bc) throws Exception {
        log.info("MouseActions.Activator.stop()");
        this.producer.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}
