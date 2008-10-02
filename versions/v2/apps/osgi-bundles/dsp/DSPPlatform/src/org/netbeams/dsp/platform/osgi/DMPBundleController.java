package org.netbeams.dsp.platform.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.PlatformComponent;
import org.netbeams.dsp.platform.Platform;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class DMPBundleController implements BundleListener, ServiceListener{
	
	private static final String DMP_COMONENT_DIRECTORY = "components";
	
	private String DMP_HOME;
	
	private Platform platform;
	
	private BundleContext bundleContext;
	
	// <bundle_file_name> ==> <bundle_config>
	private Map<String, BundleConfig> bundleConfigs;
	private Map<String, Bundle> bundles;
	
	private Map<Long, List<DMPComponent>> bundleComponentMap;
	private Map<ServiceReference, Long> seviceReferences;
	
	Object lock;

	public DMPBundleController(String dmpHomePath, Platform platform, BundleContext bundleContext){
		lock = new Object();
		DMP_HOME = dmpHomePath;
		this.platform = platform;
		this.bundleContext = bundleContext;
		bundleConfigs = new HashMap<String, BundleConfig>();
		bundles = new HashMap<String, Bundle>();
		
		bundleComponentMap = new HashMap<Long, List<DMPComponent>>();
		seviceReferences = new HashMap<ServiceReference, Long>();
	}
	
	public void init() throws JDOMException, IOException, BundleException{
		registerServiceListner();
		findPreregisteredDMPServices();
		// The OSGi specification states the bundle installation procedures should not re-install the 
		// bundle if the "location" is the same as a already installed bundle. Therefore, we can run 
		// this method during start()
		installDMPComponents();
		startDMPBundles();

	}
	
	public Collection<Bundle> getBundles(){
		return bundles.values();
	}
	
	/**
	 * Invoked by OSGi Event framework when the bundle status is modified.
	 * 
	 * @param event
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		
	}
	
	/**
	 * Invoked by OSGi Event framework when the bundle status is modified.
	 * 
	 * @param event
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
		if(event.getType() == ServiceEvent.REGISTERED){
			try {
				handleRegistered(event);
			} catch (PlatformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(event.getType() == ServiceEvent.UNREGISTERING){
			try {
				handleUnegistering(event);
			} catch (PlatformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public Bundle obtainDMPBundle(String bundleFileName){
		return bundles.get(bundleFileName);
	}
	
	
	public List<BundleConfig> sortedBundleByPriority() {
		Collection<BundleConfig> coll = bundleConfigs.values();
		List<BundleConfig> list = new ArrayList<BundleConfig>();
		list.addAll(coll);
		Collections.sort(list, new BundleConfigPriorityComparator());
		return list;
	}

	///////////////////////
	/// Private Section ///
	///////////////////////
	
	private void findPreregisteredDMPServices() {
		// TODO Auto-generated method stub
		
	}

	
	private void installDMPComponents() throws JDOMException, IOException {
		
		Log.log("DMPBundleController.installDMPComponents()");
		
		Document eConfig = readConfiguration();
		Element eComponents = eConfig.getRootElement();
		
		Log.log(eConfig);
		
		List components = eComponents.getChildren("component");
		for (Object o : components) {
			Element eComponent = (Element) o;
			Element eBundle = eComponent.getChild("bundle");
			String bundleFileName = eBundle.getTextTrim();
			Element ePriority = eComponent.getChild("priority");
			String pritority = ePriority.getTextTrim();	
			
			Log.log("DMPBundleController: bundle file name[ " + bundleFileName + " ] priority[ " + pritority + " ]");
			
			BundleConfig bundleConfig = new BundleConfig();
			bundleConfig.setBundleFileName(bundleFileName);
			bundleConfig.setPriority(Integer.parseInt(pritority));
			bundleConfigs.put(bundleFileName, bundleConfig);
			
			Bundle bundle = installBundle(bundleContext, bundleFileName);
			bundles.put(bundleFileName, bundle);
		}
	}

	/**
	 * Starts bundles based on the priority defined in the configuration.
	 * 
	 * @throws BundleException
	 */
	public void startDMPBundles() throws BundleException{
		
		Log.log("DMPBundleController.startDMPBundles()");
		
		List<BundleConfig> bundleConfigsByPriority = sortedBundleByPriority();
		
		for(BundleConfig bc: bundleConfigsByPriority){		
			try {
				Log.log("DMPBundleController: starting " + bc.getBundleFileName());
				
				Bundle b = bundles.get(bc.getBundleFileName());
				// TODO: Handle the blundle state properly
				b.start();
			} catch (BundleException e) {
				Log.log(e);
				throw e;
			}
		}
	}

	
	/**
	 * TODO: Add filter
	 */
	private void registerServiceListner() {
		bundleContext.addServiceListener(this);
	}

	
	private void handleRegistered(ServiceEvent event) throws PlatformException {
		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof DMPComponent &&
				!(service instanceof PlatformComponent)) // Hack for ComponentRegisgtry. TODO: Fix this!!!
		{
			Log.log("DMPBundleController.handleRegistered: " + service.getClass());
			Long bundleID = sr.getBundle().getBundleId();
			DMPComponent component = (DMPComponent)service;
			
			addComponent(bundleID, component);
			seviceReferences.put(sr, bundleID);
			
			try {
				platform.getComponentManager().attach(bundleID, component, 10);
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void handleUnegistering(ServiceEvent event) throws PlatformException {
		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof DMPComponent &&
				!(service instanceof PlatformComponent)) // Hack for ComponentRegisgtry. TODO: Fix this!!!)
		{	
			Log.log("DMPBundleController.handleUnegistering: " + service.getClass());
			Long bundleID = sr.getBundle().getBundleId();
			DMPComponent component = (DMPComponent)service;
			
			removeComponent(bundleID, component);
			seviceReferences.remove(sr);
			
			try {
				platform.getComponentManager().detach(bundleID, component);
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Add component to the map (bundleComponentMap) which stores references to 
	 * avaible CMP components. 
	 * 
	 * @param bundleID
	 * @param component
	 */
	public void addComponent(long bundleID, DMPComponent component){
		List<DMPComponent> componentsInBundle = bundleComponentMap.get(bundleID);
		if(componentsInBundle == null){
			componentsInBundle = new ArrayList<DMPComponent>();
			bundleComponentMap.put(bundleID, componentsInBundle);
		}
		componentsInBundle.add(component);		
	}

	/**
	 * 
	 * @param bundleID
	 * @param component
	 */
	public void removeComponent(long bundleID, DMPComponent component){
		List<DMPComponent> componentsInBundle = bundleComponentMap.get(bundleID);
		if(componentsInBundle != null){
			for( int x = 0; x < componentsInBundle.size(); ++x){
				if(component == componentsInBundle.get(x)){
					componentsInBundle.remove(x);
				}
			}
		}
	}

	private Bundle installBundle(BundleContext bundleContext, String bundleFileName) {
		String bundlePath = DMP_HOME + File.separator + DMP_COMONENT_DIRECTORY + File.separator + bundleFileName;
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

	
	private Document readConfiguration() throws JDOMException, IOException {
		String configFilePath = DMP_HOME + File.separator + DMP_COMONENT_DIRECTORY + File.separator + "config.xml";
		File configFile = new File(configFilePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			Log.log(e);
			throw e;
		}
		SAXBuilder parser = new SAXBuilder();
		Document config = null;
		try {
			config = parser.build(fis);
		} catch (JDOMException e) {
			Log.log(e);
			throw e;
		} catch (IOException e) {
			Log.log(e);
			throw e;
		}finally{
			if(fis != null){
				try{fis.close();}catch(IOException e){};
			}
		}
		return config;
	}

}
