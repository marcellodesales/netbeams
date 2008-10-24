package org.netbeams.dsp.platform.management.component.osgi;

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
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.platform.management.component.ComponentManager;
import org.netbeams.dsp.platform.management.component.DeploymentController;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class DSPBundleController implements DeploymentController, BundleListener, ServiceListener{
	
	private static final String DSP_DEPLOYMENT_DIRECTORY = "deployment";

	private String HOME_PATH;
	
	private ComponentManager componentManager;
	
	private BundleContext bundleContext;
	
	// Information about OSGi bundle containing DSPComponents
	// <bundle name> ==> <bundle_config>
	private Map<String, BundleConfig> bundleConfigs;
	private Map<String, Bundle> bundles;
	
	private Map<Long, List<DSPComponent>> bundleComponentMap;
	private Map<ServiceReference, Long> seviceReferences;
		
	private SortedSet<BundleConfig> configs;
	
	
	Object lock;

	public DSPBundleController(String homePath, BundleContext bundleContext){
		lock = new Object();
		HOME_PATH = homePath;
		this.bundleContext = bundleContext;
		bundleConfigs = new HashMap<String, BundleConfig>();
		bundles = new HashMap<String, Bundle>();
		
		bundleComponentMap = new HashMap<Long, List<DSPComponent>>();
		seviceReferences = new HashMap<ServiceReference, Long>();
		
		configs = new TreeSet<BundleConfig>(new BundleConfigPriorityComparator());
	}
	
	@Override
	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	
	/**
	 * 
	 * @throws PlatformException
	 */
	public void init() throws PlatformException{
	}
	

	
	public void start()throws PlatformException{
		registerServiceListner();
		findPreregisteredDSPServices();
		// The OSGi specification states the bundle installation procedures should not re-install the 
		// bundle if the "location" is the same as a already installed bundle. Therefore, we can run 
		// this method during start()
		
		try {
			installDSPComponents();
		} catch (Exception e) {
			// TODO: Improve this code
			e.printStackTrace();
			throw new PlatformException("Cannot install components", e);
		}
		
		try {
			startDSPBundles();
		} catch (BundleException e) {
			// TODO: Improve this code
			e.printStackTrace();
			throw new PlatformException("Cannot start components", e);
		}		
	}
	public void stop() throws PlatformException{
		// TODO: Implement it
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
		// TODO: Implement
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

	public Bundle obtainDSPBundle(String bundleFileName){
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
	
	/**
	 * It might be possible for services representing DSPComponents to be already registered before this
	 * instance registered to the OSGi Service Framework. Therefore, we need to search for previously
	 * registered service.
	 * 
	 * TODO: We need to better control how bundles containing DSP components life-cycle. Check the OSGi 
	 * Start Level Service
	 */
	private void findPreregisteredDSPServices() {
		
	}
	
	/**
	 * Install Platform DSPComponents.
	 * 
	 * TODO: Not sure if we need a different method for platform or non-platform.
	 * 
	 * @throws JDOMException
	 * @throws IOException
	 */
	private void installDSPComponents() throws JDOMException, IOException {

		Log.log("DSPBundleController.installDSPComponents()");
	
		String configFilePath = HOME_PATH + File.separator + DSP_DEPLOYMENT_DIRECTORY;
		Document config = readConfiguration(configFilePath);
		installBundles(config);
	}

	private void installBundles(Document document) throws JDOMException, IOException {
		Element eComponents = document.getRootElement();
				
		// Get all components
		List components = eComponents.getChildren("component");
		for (Object o : components) {
			Element eComponent = (Element) o;
			// Bundle Name
			Element eName = eComponent.getChild("name");
			String name = eName.getTextTrim();
			Element eBundle = eComponent.getChild("bundle");
			String fileName = eBundle.getTextTrim();
			Element ePriority = eComponent.getChild("priority");
			String pritority = ePriority.getTextTrim();	
			
			Log.log("DSPBundleController: bundle file name[ " + fileName + " ] priority[ " + pritority + " ]");
			
			// Keep information about the bundle.
			BundleConfig bundleConfig = new BundleConfig();
			bundleConfig.setName(name);
			bundleConfig.setFileName(fileName);
			bundleConfig.setPriority(Integer.parseInt(pritority));
			bundleConfigs.put(name, bundleConfig);
			
			Bundle bundle = installBundle(bundleContext, fileName);
			bundles.put(name, bundle);
			
			configs.add(bundleConfig);
		}
	}

	/**
	 * Starts bundles based on the priority defined in the configuration.
	 * 
	 * @throws BundleException
	 */
	public void startDSPBundles() throws BundleException{
		
		Log.log("DSPBundleController.startDSPBundles()");
				
		for(BundleConfig bc: configs){		
			try {
				Log.log("DSPBundleController: starting " + bc.getName());
		
				Bundle b = bundles.get(bc.getName());
				// TODO: Handle the blundle state properly
				b.start();
			} catch (BundleException e) {
				Log.log(e);
				throw e;
			}
		}
	}
	
	
	public void uninstallBundles() throws BundleException{
		for(Map.Entry<String,Bundle> entry: bundles.entrySet()){
			Log.log("Uninstalling: " + entry.getKey());
			entry.getValue().uninstall();
		}
	}
	
	/**
	 * TODO: Add filter
	 */
	private void registerServiceListner() {
		bundleContext.addServiceListener(this);
	}

	/**
	 * Checks it the new service is a DSPComponent. If so, store informarion about the service
	 * object and the bundle, and attach the DSPComponent to ComponentManager.
	 * 
	 * @param event OSGi Service Event
	 * @throws PlatformException
	 */
	private void handleRegistered(ServiceEvent event) throws PlatformException {
		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof DSPComponent){
			Log.log("DSPBundleController.handleRegistered: " + service.getClass());
			Long bundleID = sr.getBundle().getBundleId();
			DSPComponent component = (DSPComponent)service;
			
			addComponent(bundleID, component);
			seviceReferences.put(sr, bundleID);
			
			try {
				componentManager.attach(bundleID.toString(), component);
			} catch (DSPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new PlatformException("Cannot attach bundle to ComponentManager", e);
			}
		}
	}
	

	/**
	 * Checks it the ungeristered service is a DSPComponent. If so, remove informarion about the service
	 * object and the bundle, and deattach the DSPComponent to ComponentManager.
	 * 
	 * @param event OSGi Service Event
	 * @throws PlatformException
	 */

	private void handleUnegistering(ServiceEvent event) throws PlatformException {
		ServiceReference sr = event.getServiceReference();
		Object service = bundleContext.getService(sr);
		
		if(service instanceof DSPComponent)	{	
			Log.log("DSPBundleController.handleUnegistering: " + service.getClass());
			Long bundleID = sr.getBundle().getBundleId();
			DSPComponent component = (DSPComponent)service;
			
			removeComponent(bundleID, component);
			seviceReferences.remove(sr);
			
			try {
				componentManager.dettach(bundleID.toString());
			} catch (DSPException e) {
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
	public void addComponent(long bundleID, DSPComponent component){
		List<DSPComponent> componentsInBundle = bundleComponentMap.get(bundleID);
		if(componentsInBundle == null){
			componentsInBundle = new ArrayList<DSPComponent>();
			bundleComponentMap.put(bundleID, componentsInBundle);
		}
		componentsInBundle.add(component);		
	}

	/**
	 * 
	 * @param bundleID
	 * @param component
	 */
	public void removeComponent(long bundleID, DSPComponent component){
		List<DSPComponent> componentsInBundle = bundleComponentMap.get(bundleID);
		if(componentsInBundle != null){
			for( int x = 0; x < componentsInBundle.size(); ++x){
				if(component == componentsInBundle.get(x)){
					componentsInBundle.remove(x);
				}
			}
		}
	}

	private Bundle installBundle(BundleContext bundleContext, String bundleFileName) {
		String bundlePath = HOME_PATH + File.separator + DSP_DEPLOYMENT_DIRECTORY + File.separator + bundleFileName;
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

	
	private Document readConfiguration(String path) throws JDOMException, IOException {
		String configFilePath = path + File.separator + "config.xml";
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
