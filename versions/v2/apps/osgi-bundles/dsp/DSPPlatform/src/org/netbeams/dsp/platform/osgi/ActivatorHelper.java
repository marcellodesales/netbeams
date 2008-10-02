package org.netbeams.dsp.platform.osgi;

import org.netbeams.dsp.DMPComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ActivatorHelper {

	public static ServiceRegistration registerOSGIService(BundleContext bundleContext, DMPComponent component){
		return bundleContext.registerService(component.getClass().getName(), component, null);		
	}
	
	public static void unregisterOSGIService(BundleContext bundleContext, ServiceRegistration serviceRegistration){
		serviceRegistration.unregister();
	}
}
