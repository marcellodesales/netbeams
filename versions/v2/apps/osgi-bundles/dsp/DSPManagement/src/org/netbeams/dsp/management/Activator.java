package org.netbeams.dsp.management;

import javax.servlet.ServletException;
import javax.servlet.Servlet;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

	private static final Logger log = Logger.getLogger(Activator.class);

	private BundleContext bundleContext;
	private ServiceRegistration serviceRegistration;

	private DSPManager dspManager;
	private ManagementServlet mgrServlet;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		log.info("start invoked.");

		this.bundleContext = bundleContext;
		dspManager = new DSPManager();
		registerServlet();
		serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, dspManager);

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		log.info("stop invoked.");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
		unregisterServlet();
		dspManager.stopComponent();
		dspManager = null;
		mgrServlet = null;
	}

	private void registerServlet() throws PlatformException{
		ServiceReference srvRef = null;
		try{
			srvRef = bundleContext.getServiceReference(HttpService.class.getName());
			if(srvRef != null){
				try {
					HttpService httpService = (HttpService)bundleContext.getService(srvRef);
					// Create Servlet
					mgrServlet = new ManagementServlet(dspManager);
					httpService.registerServlet(ManagementServlet.BASE_URI, mgrServlet, null, null);
				} catch (Exception e) {
					log.warn("could not register management servlet", e);
					throw new PlatformException("could not register management servlet", e);
				}
			}
		}finally{
			if(srvRef != null){
				bundleContext.ungetService(srvRef);
			}
		}
	}
	
	private void unregisterServlet() throws PlatformException{
		ServiceReference srvRef = null;
		try{
			srvRef = bundleContext.getServiceReference(HttpService.class.getName());
			if(srvRef != null){
				try {
					HttpService httpService = (HttpService)bundleContext.getService(srvRef);
					httpService.unregister(ManagementServlet.BASE_URI);
				} catch (Exception e) {
					log.warn("could not unregister management servlet", e);
					throw new PlatformException("could not unregister management servlet", e);
				}
			}
		}finally{
			if(srvRef != null){
				bundleContext.ungetService(srvRef);
			}
		}
	}
	

}