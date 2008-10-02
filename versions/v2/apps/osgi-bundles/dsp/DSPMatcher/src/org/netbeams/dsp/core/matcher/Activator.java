/*
 * Created on Sun Jun 29 20:50:48 PDT 2008
 */
package org.netbeams.dsp.core.matcher;

import org.netbeams.dsp.match.Matcher;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private BundleContext context;
	private ServiceRegistration serviceRegistration;

	private Matcher matcher;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Log.log("Matcher.Activator.start()");
		this.context = context;
		registerSevice();

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
		matcher.stopComponent();
	}

	private void registerSevice() {
		matcher = new MatcherImpl();
		serviceRegistration = context.registerService(Matcher.class.getName(), matcher, null);		
	}

}