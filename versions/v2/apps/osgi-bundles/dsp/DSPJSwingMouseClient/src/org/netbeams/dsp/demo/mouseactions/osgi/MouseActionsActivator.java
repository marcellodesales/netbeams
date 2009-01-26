package org.netbeams.dsp.demo.mouseactions.osgi;

import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Activator for the Mouse Actions
 * 
 * @author marcello de sales <marcello.sales@gmail.com>
 */
public class MouseActionsActivator implements BundleActivator{
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
	
    //@Override
    public void start(BundleContext bc) throws Exception {
        Log.log("MouseActions.Activate.start()");
        this.bundleContext = bc;
        
        ServiceReference sr = bc.getServiceReference(DSPMessagesDirectory.class.getName());
        DSPMessagesDirectory messagesQueue = (DSPMessagesDirectory)bc.getService(sr);
        this.producer = new MouseActionDSPComponent(messagesQueue);
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bc, this.producer);
        
    }

    //@Override
    public void stop(BundleContext bc) throws Exception {
        Log.log("MouseActions.Activator.stop()");
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
        producer.stopComponent();
    }
}
