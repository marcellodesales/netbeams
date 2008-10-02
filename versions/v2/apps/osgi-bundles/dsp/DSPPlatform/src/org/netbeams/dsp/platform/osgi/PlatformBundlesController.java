package org.netbeams.dsp.platform.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.broker.MessageBroker;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.match.Matcher;
import org.netbeams.dsp.platform.Platform;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.registry.ComponentRegistry;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class PlatformBundlesController implements ServiceListener, BundleListener {
		
	public static final String DMP_PLATFORM_COMPONENT = "DMP-platform-component";
	
	private final String[] knowPlatformComponents= {
			"org.dmp.basic.broker.MessageBroker",
			"org.dmp.basic.registry.ComponentRegistry",
			"org.dmp.basic.matcher.Matcher"
	};
	
	
	private String DMP_HOME;
	
	private Platform platform;
	Properties configuration;
	
	private BundleContext bundleContext;
	
	// Plarform Components
	private Bundle brokerBundle;
	private MessageBroker broker;
	private ServiceReference brokerSR;

	private Bundle registryBundle;
	private ComponentRegistry registry;
	private ServiceReference registrySR;

	private Bundle matcherBundle;
	private Matcher matcher;
	private ServiceReference matcherSR;

	private ComponentManager componentManager;
	private ServiceReference comonentManagerSR;
	
	private List<Bundle> startedPlatformBundles;
	
	private Object lock;
	
	
	public PlatformBundlesController(String dmpHomePath, Platform platform, BundleContext bundleContext, Properties configuration){
		lock = new Object();
		startedPlatformBundles = new ArrayList<Bundle>();
		DMP_HOME = dmpHomePath;
		this.platform = platform;
		this.bundleContext = bundleContext;
		this.configuration = configuration;
	}
	
	public void init() throws PlatformException, BundleException{
		registerServiceListner();
		registerBundleListener();
		findPreregisteredPlatformServices();
		// The OSGi specification states the bundle installation procedures should not re-install the 
		// bundle if the "location" is the same as a already installed bundle. Therefore, we can run 
		// this method during start()
		installPlatformBundles();
		startPlatformBundles();
	}
		
	public void uninstallPlatformBundles() throws BundleException{
		brokerBundle.uninstall();
		registryBundle.uninstall();
		matcherBundle.uninstall();
	}

	/**
	 * Invoked by OSGi Event framework when the service status is modified.
	 *
	 * @param event
	 * @throws DMPException 
	 * @throws PlatformException 
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
		if(event.getType() == ServiceEvent.REGISTERED){
			handleRegistered(event);
		}else if(event.getType() == ServiceEvent.UNREGISTERING){
			try {
				handleUnegistered(event);
			} catch (PlatformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void bundleChanged(BundleEvent event) {
//		if(event.getType() == BundleEvent.STARTED){
//			Bundle bundle = event.getBundle();
//			if(bundle.getHeaders("") != null){
//				startedPlatformBundles.add(bundle);
//			}
//		}
		
	}


	
	///////////////////////
	/// Private Section ///
	///////////////////////

	
	private void registerBundleListener() {
		bundleContext.addBundleListener(this);
		
	}

	private void registerServiceListner() {
		bundleContext.addServiceListener(this);
	}

	/**
	 * For now this method assumes only one service/bundle is installed in OSGI instance.
	 * 
	 * @param ServiceEvent OSGi service framework event
	 * @throws DMPException 
	 */
	private void handleRegistered(ServiceEvent event) {
		

		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof MessageBroker){
			Log.log("PlatformBundlesController.handleRegistered: " + service.getClass());
			MessageBroker mb = (MessageBroker)service; 
			synchronized (lock) {
				if(broker == null){
					try {
						platform.setMessageBroker(mb);
						broker = mb;
						brokerSR = sr;
					} catch (PlatformException e) {
						Log.log("Could not set the Message Broker in the Platform");
						Log.log(e);
					}
				}
			}
			return;
		}
		
		if(service instanceof ComponentRegistry){
			Log.log("PlatformBundlesController.handleRegistered: " + service.getClass());

			ComponentRegistry reg = (ComponentRegistry)service;
			synchronized (lock) {
				if(registry == null){
					try {
						platform.setComponentRegistry(reg);
						registry = reg;
						registrySR = sr;
					} catch (PlatformException e) {
						Log.log("Could not set the Registry in the Platform");
						Log.log(e);
					}
				}
			}
			return;
		}
		
		if(service instanceof Matcher){
			Log.log("PlatformBundlesController.handleRegistered: " + service.getClass());

			Matcher m = (Matcher)service;
			synchronized (lock) {
				if(matcher == null){
					try {
						platform.setMatcher(m);
						matcher = m;
						matcherSR = sr;
					} catch (Exception e) {
						Log.log("Could not set the Matcher in the Platform");
						Log.log(e);
					} 
				}
			}
			return;
		}		
	}	
	
	/**
	 * For now this method assumes only one service/bundle is installed in OSGI instance.
	 * 
	 * @param ServiceEvent OSGi service framework event
	 */
	private void handleUnegistered(ServiceEvent event) throws PlatformException {
		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof MessageBroker){
			synchronized (lock) {
				if(brokerSR == sr){
					platform.setMessageBroker(null);
					broker = null;
					brokerSR = null;
				}else{
					Log.log("PlatformBundlesController.handleUnegistering: Service reference does not match");
				}
			}
			return;
		}
		
		if(service instanceof ComponentRegistry){
			synchronized (lock) {
				if(registrySR == sr){
					platform.setComponentRegistry(null);
					registry = null;
					registrySR = null;
				}
			}
			return;
		}
		
		if(service instanceof Matcher){
			synchronized (lock) {
				if(registrySR == sr){
					try {
						platform.setMatcher(null);
					} catch (DMPException e) {
						Log.log("Problem unsetting the Matcher in the platform");
						Log.log(e);
					}
					matcher = null;
					matcherSR = null;
				}
			}
			return;
		}			
	}


	private void installPlatformBundles() {
		Log.log("PlatformBundlesController.obtainPlatformComponents()");
								
		// TODO: Refactore this code. It is dull...
		Log.log("PlatformBundlesController.obtainPlatformComponents: installing Broker");
		String brokerFileName = configuration.getProperty("bundle.dmp.broker");
		brokerBundle = installBundle(brokerFileName);
		
		Log.log("PlatformBundlesController.obtainPlatformComponents: installing Matcher");
		String matcherFileName = configuration.getProperty("bundle.dmp.matcher");
		matcherBundle = installBundle(matcherFileName);
		
		Log.log("PlatformBundlesController.obtainPlatformComponents: installing Registry");
		String registryFileName = configuration.getProperty("bundle.dmp.registry");
		registryBundle = installBundle(registryFileName);
				
		Log.log("PlatformBundlesController.obtainPlatformComponents(): end");	
	}
	
		
	/**
	 * Start all the plarform bundles.
	 * 
	 * @throws BundleException 
	 */
	
	private void startPlatformBundles() throws BundleException {
		OSGIHelper.startBundle(registryBundle);
		OSGIHelper.startBundle(matcherBundle);		
		OSGIHelper.startBundle(brokerBundle);		
	}
	
	
	/**
	 * Obtain the services which implement the platform components. Platform components can
	 * be provided by an registered event or we manually try to find it.
	 * @throws PlatformException 
	 */
	private void findPreregisteredPlatformServices() throws PlatformException {
		Log.log("PlatformBundlesController.findPreregisteredPlatformServices()");
		
		// TODO: As this class is also a ServiceListner, I need to investigate how what is the flow of events when 
		// we install the bundle. For now, I aquire the monitor few time to give an opportunity for the event thread.

		synchronized (lock) {
			if(registry == null){
				Log.log("Activator.findPreregisteredPlatformServices: Finding " + ComponentRegistry.class.getName());
				registrySR = bundleContext.getServiceReference(ComponentRegistry.class.getName());
				if(registrySR != null){
					Log.log("Activator.findPreregisteredPlatformServices: Found");
					registryBundle = registrySR.getBundle();
					registry = (ComponentRegistry) bundleContext.getService(registrySR);
					platform.setComponentRegistry(registry);
				}
			}
		}
		synchronized (lock) {
			if(matcher == null){
				Log.log("Activator.findPreregisteredPlatformServices: Finding " + Matcher.class.getName());
				matcherSR = bundleContext.getServiceReference(Matcher.class.getName());
				if(matcherSR != null){
					Log.log("Activator.findPreregisteredPlatformServices: Found");
					try {
						platform.setMatcher(matcher);
						matcherBundle = matcherSR.getBundle();
						matcher = (Matcher) bundleContext.getService(matcherSR);
					} catch (DMPException e) {
						Log.log("Could not set the Matcher in the Platform");
						Log.log(e);
					}
				}
			}
		}
		synchronized (lock) {
			if(broker == null){
				Log.log("Activator.findPreregisteredPlatformServices: Finding " + MessageBroker.class.getName());
				brokerSR = bundleContext.getServiceReference(MessageBroker.class.getName());
				if(brokerSR != null){
					Log.log("Activator.findPreregisteredPlatformServices: Found");
					brokerBundle = brokerSR.getBundle();
					broker = (MessageBroker) bundleContext.getService(brokerSR);
					platform.setMessageBroker(broker);
				}
			}
		}
	}

	
	private Bundle installBundle(String bundleFileName) {
		String bundlePath = DMP_HOME + File.separator + bundleFileName;
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(new File(bundlePath));
			return bundleContext.installBundle(bundlePath, fis);
		} catch (BundleException e) {
			Log.log(e);
		} catch (FileNotFoundException e) {
			Log.log(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

}
