package org.netbeams.test.serial;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class Activator implements BundleActivator {

	public static BundleContext bc = null;
	private SerialBundle thread;
	
	public void start(BundleContext bc) throws Exception {
		System.out.println("Serial bundle starting...");
		this.bc = bc;
		this.thread = new SerialBundle();
		this.thread.start();

	}

	public void stop(BundleContext arg0) throws Exception {
		System.out.println("Serial bundle stopping...");
		//this.thread.stopThread();
		this.thread.join();
		this.bc = null;

	}

}
