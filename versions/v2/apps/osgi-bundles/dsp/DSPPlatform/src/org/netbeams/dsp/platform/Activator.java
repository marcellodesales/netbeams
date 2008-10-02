/*
 * Created on Wed Jun 25 19:19:36 PDT 2008
 */
package org.netbeams.dsp.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.netbeams.dsp.platform.osgi.DMPBundleController;
import org.netbeams.dsp.platform.osgi.PlatformBundlesController;
import org.netbeams.dsp.util.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class Activator implements BundleActivator {
	
	private BundleContext bundleContext;
	
	// Plarform
	private Properties configuration;
	private Platform platform;
	private DMPBundleController dmpBundlesController;
	private PlatformBundlesController platformBundlesController;
	

	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Log.log("Platform.Activator.start()");
		
		this.bundleContext = bundleContext;
		platform = new Platform();
		platform.init();
		platform.start();
		
		String homeDir = obtainDMPHomePath();
		Log.log("Platform.Activator: DMP_HOME = " + homeDir);

		configuration = loadPlatformConfigurations(homeDir);
		
		platformBundlesController = new PlatformBundlesController(homeDir, platform, bundleContext, configuration);
		platformBundlesController.init();
		
		dmpBundlesController = new DMPBundleController(homeDir, platform, bundleContext);
		dmpBundlesController.init();
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		platform.stop();
		if(configuration.getProperty("platform.bundle.uninstallOnStop", "false").equals("true")){
			platformBundlesController.uninstallPlatformBundles();
		}
	}


	/**
	 * Obtain the DMP component implementaion which is specified in the manifest head:
	 * 
	 * <code>DMP-Component</code>
	 * 
	 * @param bundle Bundle of the DMP component.
	 * @return
	 */
	private String obtainDMPComponentName(Bundle bundle) {
		Dictionary headers = bundle.getHeaders();
		return (String)(headers.get("DMP-Component"));
	}
	
	private void startDMPComponents() throws BundleException {
		Log.log("Activator.startDMPComponents()");
		dmpBundlesController.startDMPBundles();
	}
	
	private Properties loadPlatformConfigurations(String dir) {
		String configFilePath = dir + File.separator + "config.properties";
		Properties configProperties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(configFilePath));
			configProperties.load(fis);
		} catch (FileNotFoundException e) {
			Log.log(e);
		} catch (IOException e) {
			Log.log(e);
		}finally{
			try {
				if(fis != null){
					fis.close();
				}
			} catch (IOException e) {}
		}
		return configProperties;
	}


	private Bundle installBundle(String homeDir, String bundleFileName) {
       String bundlePath = homeDir + File.separator + bundleFileName;
       FileInputStream fis = null;
       
       try {
    	   fis = new FileInputStream(new File(bundlePath));
     	   return bundleContext.installBundle(bundlePath, fis);
       } catch (BundleException e) {
    	   Log.log(e);
       } catch (FileNotFoundException e) {
    	   Log.log(e);
       }finally{
    	   if(fis != null){
    		   try {
				fis.close();
			} catch (IOException e) {}
    	   }
       }
       return null;
	}

	private String obtainDMPHomePath(){
		// For now, get it from environment variables
		return System.getenv("DMP_HOME");

	}
	

}