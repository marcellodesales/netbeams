/*
 * Created on Wed Jun 25 20:53:51 PDT 2008
 */
package org.netbeams.dsp.registry;


import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private BundleContext context;
	private ServiceRegistration serviceRegistration;

	private ComponentRegistry registry;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Log.log("Registry.Activator.start()");
		this.context = context;
		registerSevice();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
		registry.stopComponent();
	}


	private void registerSevice() {
		registry = new ComponentRegistryImpl();
		serviceRegistration = context.registerService(ComponentRegistry.class.getName(), registry, null);		
	}
}