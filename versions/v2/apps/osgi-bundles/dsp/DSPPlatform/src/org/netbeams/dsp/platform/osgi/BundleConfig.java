package org.netbeams.dsp.platform.osgi;

public class BundleConfig {
	
	private String bundleFileName;
	private int priority;

	public BundleConfig() {
	}

	public String getBundleFileName() {
		return bundleFileName;
	}

	public void setBundleFileName(String bundleFileName) {
		this.bundleFileName = bundleFileName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	

}
