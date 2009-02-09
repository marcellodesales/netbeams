package org.netbeams.dsp.test.rand;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class Activator implements BundleActivator {
	
	private static final Logger log = Logger.getLogger(Activator.class);
	
	private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;
    
    private RandomProducer producer;

	public void start(BundleContext bc) throws Exception {		
		log.info("Starting...");
		
		bundleContext = bc;
		producer = new RandomProducer();
		serviceRegistration = ActivatorHelper.registerOSGIService(bc, producer);
	}

	public void stop(BundleContext bc) throws Exception {
		log.info("Stopping...");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		producer.stopComponent();
		//producer.stopThread();
		//producer.join();
		bundleContext = null;
		
	}

}
