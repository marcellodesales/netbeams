/*
 * Created on Wed Jun 25 20:37:46 PDT 2008
 */
package org.netbeams.dsp.core.management.component;

import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
  	
	private BundleContext context;
	private ServiceRegistration serviceRegistration;
	
	private ComponentManager componentManagement;
		
	 /* (non-Javadoc)
	  * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	  */
	public void start(BundleContext context) throws Exception {
		Log.log("ComponentManager.Activator.start()");
		this.context = context;
		componentManagement = new ComponentManagerImpl();
		registerSevice();
	 }

	 /* (non-Javadoc)
	  * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	  */
	 public void stop(BundleContext context) throws Exception {
	 }
	
	 private void registerSevice() { 
		 serviceRegistration = context.registerService(ComponentManager.class.getName(), componentManagement, null);
	 }
	 
  
}