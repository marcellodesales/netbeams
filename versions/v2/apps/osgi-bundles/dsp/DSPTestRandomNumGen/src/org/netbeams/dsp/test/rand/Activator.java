package org.netbeams.dsp.test.rand;

import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class Activator implements BundleActivator {
	
	private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;
    private RandomNumberGenerator producer;

	public void start(BundleContext bc) throws Exception {		
		Log.log("RandomNumber.Activate.start()");
		producer = new RandomNumberGenerator();
		bundleContext = bc;
		producer = new RandomNumberGenerator();
		producer.start();
		serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, producer);
	}

	public void stop(BundleContext bc) throws Exception {
		Log.log("RandomNumber.Activator.stop()");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		producer.stopThread();
		producer.join();
		bundleContext = null;
		
	}

}
