package org.netbeams.test.randomnumber;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	private RandomNumber thread = null;
	
	public void start(BundleContext bc) throws Exception {
		
		System.out.println("Random Number Generator starting...");
		this.bc = bc;
		this.thread = new RandomNumber();
		this.thread.start();
	}

	public void stop(BundleContext bc) throws Exception {
		
		System.out.println("Random Number Generator stopping...");
		this.thread.stopThread();
		this.thread.join();
		this.bc = null;
		

	}

}
