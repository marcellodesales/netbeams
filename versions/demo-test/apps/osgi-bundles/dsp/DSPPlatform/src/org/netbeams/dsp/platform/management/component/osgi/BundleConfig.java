package org.netbeams.dsp.platform.management.component.osgi;

/**
 * Information about OSGi Bundles kept by the Platform
 * 
 * #10/08/2008 - Kleber Sales - Creation
 *
 **/

public class BundleConfig {
	
	private String name;
	private String fileName;
	private int priority;

	public BundleConfig() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
