package org.netbeams.dsp.test.randomnumber;

import org.netbeams.dsp.test.randomnumber.RandomNumberDSPComponent;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.apache.log4j.Logger;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class RandomNumberActivator implements BundleActivator {
	
	private static final Logger log = Logger.getLogger(RandomNumberActivator.class);
	
	private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;
    
    private RandomNumberDSPComponent producer;

	public void start(BundleContext bc) throws Exception {		
		log.info("Starting...");
		bundleContext = bc;
		producer = new RandomNumberDSPComponent();
		serviceRegistration = ActivatorHelper.registerOSGIService(bc, producer);
	}

	public void stop(BundleContext bc) throws Exception {
		log.info("Stopping...");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		producer.stopComponent();
		bundleContext = null;
		
	}

}
