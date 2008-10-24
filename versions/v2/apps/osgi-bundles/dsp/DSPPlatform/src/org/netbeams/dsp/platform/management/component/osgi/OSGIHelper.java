package org.netbeams.dsp.platform.management.component.osgi;

import org.netbeams.dsp.util.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class OSGIHelper {
	
	public static void startBundle(Bundle bundle) throws BundleException {
		Log.log("Actrivator: Starting bundle " + bundle.getLocation());
		try {
			bundle.start();
		} catch (BundleException e) {
			Log.log(e);
			throw e;
		}
	}
	
	public static Object obtainBundleServiceReference(BundleContext context, String clazz){
		ServiceReference reference = context.getServiceReference(clazz);
		if(reference == null){
			Log.log("No bundle service reference for " + clazz);
			return null;
		}
		return context.getService(reference);
	}

}
