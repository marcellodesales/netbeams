package org.netbeams.test.simplebundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author teresa | tlj@sfsu.edu
 *
 */
public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	private HelloWorldThread thread = null;
	
	public void start(BundleContext bc) throws Exception {
		System.out.println("SimpleBundle starting...");
		Activator.bc = bc;
		this.thread = new HelloWorldThread();
		this.thread.start();
	}
	
	public void stop(BundleContext bc) throws Exception {
		System.out.println("SimpleBundle stopping...");
		this.thread.stopThread();
		this.thread.join();
		Activator.bc = null;		
	}
	
	
}
