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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.platform.management.component.ComponentManager;
import org.netbeams.dsp.platform.management.component.DeploymentController;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class DSPBundleController implements DeploymentController, BundleListener, ServiceListener{
	
	private static final Logger log = Logger.getLogger(DSPBundleController.class);
	
	private static final String DSP_DEPLOYMENT_DIRECTORY = "deployment";
	private static final String CONFIG_FILE = "config.xml";

	private String HOME_PATH;
	
	private ComponentManager componentManager;
	
	private BundleContext bundleContext;
	
	// Information about OSGi bundle containing DSPComponents
	// <bundle name> ==> <bundle_config>
	private Map<String, BundleConfig> nameTobundleConfigsMap; 
	private Map<String, Bundle> nameToBundleMap;  
	private Map<String, List<DSPComponent>> nameToComponentMap;
	private Map<Long, String> bundleIdToNameMap;
	
	private Map<ServiceReference, Long> seviceReferences;
		
	private SortedSet<BundleConfig> sortedByPriorityConfigs;
	
	Object lock;

	public DSPBundleController(String homePath, BundleContext bundleContext){
		nameTobundleConfigsMap = new HashMap<String, BundleConfig>();
		nameToBundleMap = new HashMap<String, Bundle>();
		
		nameToComponentMap = new HashMap<String, List<DSPComponent>>();
		bundleIdToNameMap = new HashMap<Long, String>();
		seviceReferences = new HashMap<ServiceReference, Long>();
		
		sortedByPriorityConfigs = new TreeSet<BundleConfig>(new BundleConfigPriorityComparator());
		
		lock = new Object();
		HOME_PATH = homePath;
		this.bundleContext = bundleContext;
	}
	
	/**
	 * @Override
	 */
	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}
	
	/**
	 * Init component.
	 * 
	 * @throws PlatformException
	 */
	public void init() throws PlatformException{
		log.info("init()");
	}
	
	public void start()throws PlatformException{
		log.info("start()");
		registerServiceListner();
		findPreregisteredDSPServices();
		// The OSGi specification states the bundle installation procedures should not re-install the 
		// bundle if the "location" is the same as a already installed bundle. Therefore, we can run 
		// this method during start()
		
		try {
			installDSPComponents();
		} catch (Exception e) {
			// TODO: Improve this code
			log.error("Problems installing dsp components.", e);
			throw new PlatformException("CProblems installing dsp components.", e);
		}
		
		try {
			startDSPBundles();
		} catch (BundleException e) {
			// TODO: Improve this code
			log.error("Cannot start components.", e);
			throw new PlatformException("Cannot start components.", e);
		}		
	}
	public void stop() throws PlatformException{
		log.info("stop()");
	}
	
	public Collection<Bundle> getBundles(){
		return nameToBundleMap.values();
	}
	
	/**
	 * Invoked by OSGi Event framework when the bundle status is modified.
	 * 
	 * @param event
	 * @Override
	 */
	public void bundleChanged(BundleEvent event) {
		// TODO: Implement
	}
	
	/**
	 * Invoked by OSGi Event framework when the bundle status is modified.
	 * 
	 * @param event
	 * @Override
	 */
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

//	public Bundle obtainDSPBundle(String bundleFileName){
//		return nameToBundleMap.get(bundleFileName);
//	}
	
	
	public List<BundleConfig> sortedBundleByPriority() {
		Collection<BundleConfig> coll = nameTobundleConfigsMap.values();
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
		log.info("findPreregisteredDSPServices()");
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
		String configFilePath = obtainDeploymentPath();
		
		log.info("Install dsp components. Deployment directory: " + configFilePath);

		Document config = readConfiguration(configFilePath);
		installBundles(config);
	}

	private void installBundles(Document document) throws JDOMException, IOException {
		log.info("Install bundles.");
		
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
			
			log.info("Read config for bundle name:" + name);
			
			// Keep information about the bundle.
			BundleConfig bundleConfig = new BundleConfig();
			bundleConfig.setName(name);
			bundleConfig.setFileName(fileName);
			bundleConfig.setPriority(Integer.parseInt(pritority));
			nameTobundleConfigsMap.put(name, bundleConfig);
			
			Bundle bundle = installBundle(bundleContext, fileName);
			if(bundle != null){
				nameToBundleMap.put(name, bundle);
				bundleIdToNameMap.put(bundle.getBundleId(), name);
				sortedByPriorityConfigs.add(bundleConfig);
			}
		}
	}

	/**
	 * Starts bundles based on the priority defined in the configuration.
	 * 
	 * @throws BundleException
	 */
	public void startDSPBundles() throws BundleException{
		
		log.info("start bundles");
				
		for(BundleConfig bc: sortedByPriorityConfigs){		
			try {
				log.info("Starting " + bc.getName());
		
				Bundle b = nameToBundleMap.get(bc.getName());
				b.start();
			} catch (BundleException e) {
				log.warn("Could not start bundle.", e);
			}
		}
	}
	
	public void uninstallBundles() throws BundleException{
		log.info("Uninstalling bundles...");
		for(Map.Entry<String,Bundle> entry: nameToBundleMap.entrySet()){
			log.info("Uninstalling: " + entry.getKey());		
		    entry.getValue().uninstall();
		}
	}
	
	/**
	 * TODO: Add filter
	 */
	private void registerServiceListner() {
		log.info("Registering OSGi service listner()");
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
			log.info("Registering dsp component: " + service.getClass());
			DSPComponent component = (DSPComponent)service;

			Long bundleId = sr.getBundle().getBundleId();
			String componentName = bundleIdToNameMap.get(bundleId);
			if(componentName != null){
				log.info("Bundle mapped to component name " + componentName);
			}else{
				log.warn("Bundle could not be mapped to any component name. It will not be registered.");
				return;
			}
			
			addComponent(componentName, component);
			seviceReferences.put(sr, bundleId);
			
			try {
				componentManager.attach(componentName, component);
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
			Long bundleID = sr.getBundle().getBundleId();
			
			log.info("Unregistering component " + bundleIdToNameMap.get(bundleID) + 
					" class " + service.getClass());

			DSPComponent component = (DSPComponent)service;
			
			Long bundleId = sr.getBundle().getBundleId();
			String componentName = bundleIdToNameMap.get(bundleId);
			if(componentName != null){
				log.info("Bundle mapped to component name " + componentName);
			}else{
				log.warn("Bundle could not be mapped to any component name. It will not be unregistered.");
				return;
			}
			
			
			removeComponent(componentName, component);
			seviceReferences.remove(sr);
			
			try {
				componentManager.dettach(componentName);
			} catch (DSPException e) {
				log.warn("Could not dettach ");
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
	public void addComponent(String name, DSPComponent component){
		List<DSPComponent> componentsInBundle = nameToComponentMap.get(name);
		if(componentsInBundle == null){
			componentsInBundle = new ArrayList<DSPComponent>();
			nameToComponentMap.put(name, componentsInBundle);
		}
		componentsInBundle.add(component);		
	}

	/**
	 * 
	 * @param bundleID
	 * @param component
	 */
	public void removeComponent(String name, DSPComponent component){
		log.info("Remove component " + name);
		
		List<DSPComponent> componentsInBundle = nameToComponentMap.get(name);
		if(componentsInBundle != null){
			for( int x = 0; x < componentsInBundle.size(); ++x){
				if(component == componentsInBundle.get(x)){
					componentsInBundle.remove(x);
				}
			}
		}
	}

	private Bundle installBundle(BundleContext bundleContext, String bundleFileName) {
		log.info("Installing bundle file name " + bundleFileName);
		
		String bundlePath = obtainDeploymentPath() + File.separator + bundleFileName;
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(new File(bundlePath));
			return bundleContext.installBundle(bundlePath, fis);
		} catch (BundleException e) {
			log.error("Could not install bundle", e);
		} catch (FileNotFoundException e) {
			log.error("Could not find bundle file", e);
		} finally {
			if (fis != null) {
				try { fis.close();} catch (IOException e) {}
			}
		}
		return null;
	}

	private Document readConfiguration(String path) throws JDOMException, IOException {
		String configFilePath = path + File.separator + CONFIG_FILE;
		File configFile = new File(configFilePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			log.error("Could not find configuration file", e);
			throw e;
		}
		SAXBuilder parser = new SAXBuilder();
		Document config = null;
		try {
			config = parser.build(fis);
		} catch (JDOMException e) {
			log.error("Could not parse configuration file", e);
			throw e;
		} catch (IOException e) {
			log.error("Could not parse configuration file", e);
			throw e;
		}finally{
			if(fis != null){
				try{fis.close();}catch(IOException e){};
			}
		}
		return config;
	}

	private String obtainDeploymentPath(){
		String path;
		
		String deploymentDir = System.getProperty("dsp.deploymentdir");
		if(deploymentDir == null){
			deploymentDir = DSP_DEPLOYMENT_DIRECTORY;
		}
		
		return HOME_PATH + File.separator + deploymentDir;
	}

}
