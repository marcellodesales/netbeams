package org.netbeams.dsp.test.rand;

import org.apache.log4j.Logger;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
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
		
        ServiceReference sr = bc.getServiceReference(DSPMessagesDirectory.class.getName());
        DSPMessagesDirectory messagesQueue = (DSPMessagesDirectory) bc.getService(sr);
        if (messagesQueue == null) {
            log.error("The Messages Queue serice could not be retrieved from the reference of the OSGi platform!!");
            throw new IllegalStateException(
                    "The Messages Queue serice could not be retrieved from the reference of the OSGi platform!!");
        }

		producer = new RandomProducer(messagesQueue);
		//producer.start();
		serviceRegistration = ActivatorHelper.registerOSGIService(bc, producer);
	}

	public void stop(BundleContext bc) throws Exception {
		log.info("Stopping...");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		//producer.stopComponent();
		//producer.stopThread();
		//producer.join();
		bundleContext = null;
		
	}

}
