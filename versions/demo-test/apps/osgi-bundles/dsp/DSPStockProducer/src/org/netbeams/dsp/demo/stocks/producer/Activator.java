package org.netbeams.dsp.demo.stocks.producer;


import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	
	private static final Logger log = Logger.getLogger(Activator.class);
	
	private BundleContext bundleContext;
	private ServiceRegistration serviceRegistration;
	
	private StockProducer producer;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
	  log.info("Starting...");
	  
	  this.bundleContext = bundleContext;
	  producer = new StockProducer();
	  serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, producer);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
	  log.info("stopping...");
	  ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
	  producer.stopComponent();
  }
}