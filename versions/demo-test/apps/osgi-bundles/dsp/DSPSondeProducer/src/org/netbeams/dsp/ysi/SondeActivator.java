package org.netbeams.dsp.ysi;

import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.apache.log4j.Logger;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeActivator implements BundleActivator {
	
	private static final Logger log = Logger.getLogger(SondeActivator.class);
	
	private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    private SondeDSPComponent dspComponent;
    
	public void start(BundleContext bundleContext) throws Exception {		
		log.info("Starting...");
		this.bundleContext = bundleContext;
		dspComponent = new SondeDSPComponent();
		serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, dspComponent);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		log.info("Stopping...");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		dspComponent.stopComponent();
		bundleContext = null;
	}

}
