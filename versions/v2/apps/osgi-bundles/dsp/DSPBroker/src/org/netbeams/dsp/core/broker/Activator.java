/*
 * Created on Wed Jun 25 19:24:56 PDT 2008
 */
package org.netbeams.dsp.core.broker;

import org.netbeams.dsp.broker.MessageBroker;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
  
	private BundleContext context;
	private ServiceRegistration serviceRegistration;

	private MessageBroker broker;
	
	
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
	  Log.log("MessageBroker.Activator.start()");
	  this.context = context;
	  registerSevice();
  }

/* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
	  serviceRegistration.unregister();
	  broker.stopComponent();
  }
  
  
  private void registerSevice() {
		broker = new MessageBrokerImpl();
		serviceRegistration = context.registerService(MessageBroker.class.getName(), broker, null);		
	}

}