package org.netbeams.dsp.platform.osgi;

import org.netbeams.dsp.DSPComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ActivatorHelper {

	public static ServiceRegistration registerOSGIService(BundleContext bundleContext, DSPComponent component){
		return bundleContext.registerService(component.getClass().getName(), component, null);		
	}
	
	public static void unregisterOSGIService(BundleContext bundleContext, ServiceRegistration serviceRegistration){
		serviceRegistration.unregister();
	}
}
