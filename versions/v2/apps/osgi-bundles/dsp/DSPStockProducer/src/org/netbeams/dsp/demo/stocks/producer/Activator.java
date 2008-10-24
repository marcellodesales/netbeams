package org.netbeams.dsp.demo.stocks.producer;


import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	
	private BundleContext bundleContext;
	private ServiceRegistration serviceRegistration;
	
	private StockProducer producer;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
	  Log.log("StockProducer.Activate.start()");
	  
	  this.bundleContext = bundleContext;
	  producer = new StockProducer();
	  serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, producer);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
	  Log.log("StockProducer.Activator.stop()");
	  ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
	  producer.stopComponent();
  }
}