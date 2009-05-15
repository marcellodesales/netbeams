package org.netbeams.dsp.management;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.management.test.TestManagement;
import org.netbeams.dsp.management.ui.GetDataServlet;
import org.netbeams.dsp.management.ui.PropertyUIServlet;
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
	private PropertyUIServlet propServlet;
	private GetDataServlet dataServler;
	private TestManagement tester;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		log.info("start invoked.");

		this.bundleContext = bundleContext; 
		dspManager = new DSPManager();
		registerServlet();
		serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, dspManager);
//		tester = new TestManagement(dspManager);
//		tester.start();

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		log.info("stop invoked.");
		ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
    	unregisterServlet();
		if(tester != null){
			tester.shouldStop();
			tester.interrupt();
		}
		dspManager.stopComponent();
		dspManager = null;
		mgrServlet = null;
	}

	private void registerServlet() throws DSPException {
		ServiceReference srvRef = null;
		try{
			srvRef = bundleContext.getServiceReference(HttpService.class.getName());
			if(srvRef != null){
				try {
					HttpService httpService = (HttpService)bundleContext.getService(srvRef);
					// Create and Bind ManagementServlet
					mgrServlet = new ManagementServlet(dspManager);
					httpService.registerServlet(ManagementServlet.BASE_URI, mgrServlet, null, null);
					
					// Create and Bind PropertyUIServlet
					propServlet = new PropertyUIServlet(dspManager);
					httpService.registerServlet(PropertyUIServlet.BASE_URI, propServlet, null, null);

					// Create and Bind PropertyUIServlet
					dataServler = new GetDataServlet(dspManager.getBuffer());
					httpService.registerServlet(GetDataServlet.BASE_URI, dataServler, null, null);

					log.info("Servlets registered.");
					
				} catch (Exception e) {
					log.warn("could not register management servlet", e);
					throw new DSPException("could not register management servlet: " + 
					                                                               e.getMessage());
				}
			}else{
	            log.error("The Http Service reference could not be retrieved from the OSGi platform!!!");
	            throw new IllegalStateException("This DSP Component needs to have the HTTP service running!!!");
			}
		}finally{
			if(srvRef != null){
//				bundleContext.ungetService(srvRef);
			}
		}
	}
	
	private void unregisterServlet() throws DSPException {
		ServiceReference srvRef = null;
		try{
			srvRef = bundleContext.getServiceReference(HttpService.class.getName());
			if(srvRef != null){
				try {
					HttpService httpService = (HttpService)bundleContext.getService(srvRef);
					httpService.unregister(ManagementServlet.BASE_URI);
					httpService.unregister(PropertyUIServlet.BASE_URI);
					httpService.unregister(GetDataServlet.BASE_URI);
				} catch (Exception e) {
					log.warn("could not unregister management servlet", e);
					throw new DSPException("could not unregister management servlet: "
					                                                            + e.getMessage());
				}
			}
		}finally{
			if(srvRef != null){
				bundleContext.ungetService(srvRef);
			}
		}
	}
	

}