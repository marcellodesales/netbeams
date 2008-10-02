/*
 * Created on Sun Jun 29 20:24:58 PDT 2008
 */
package org.netbeams.dsp.core.stockconsumer;

import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
  
	private BundleContext bundleContext;
	private ServiceRegistration serviceRegistration;
	
	private StockConsumer consumer;
	
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext bundleContext) throws Exception {
	  Log.log("StockConsumer.Activate.start()");
	  
	  this.bundleContext = bundleContext;
	  consumer = new StockConsumer();
	  serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, consumer);

  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
	  Log.log("StockConsumer.Activator.stop()");
	  ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
	  consumer.stopComponent();
  }
}