/*
 * Created on Sat Oct 18 21:26:01 PDT 2008
 */

package org.netbeams.dsp.persistence;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * First Skeleton from Prof. Puder Data persistene Activator
 * 
 * @author marcello
 */
public class DataPersistenceActivator implements BundleActivator {

	public static BundleContext bc = null;

	public void start(BundleContext bc) throws Exception {
		System.out.println(bc.getBundle().getHeaders().get(
				Constants.BUNDLE_NAME)
				+ " starting...");
		DataPersistenceActivator.bc = bc;
		DSPInMemoryDataPersistence service = DSPInMemoryDataPersistence.INSTANCE;
		ServiceRegistration registration = bc.registerService(
				DSPInMemoryDataPersistence.class.getName(), service,
				new Properties());
		System.out.println("Service registered: "
				+ registration.getReference().toString());
	}

	public void stop(BundleContext bc) throws Exception {
		System.out.println(bc.getBundle().getHeaders().get(
				Constants.BUNDLE_NAME)
				+ " stopping...");
		DataPersistenceActivator.bc = null;
	}
}