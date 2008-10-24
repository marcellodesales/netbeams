package org.netbeams.dsp;

public class ComponentIdentifier {

	private String type;
	private ComponentLocator locator;
	
	public ComponentIdentifier(){}
	
	public ComponentIdentifier(String type, ComponentLocator locator) {
		this.type = type;
		this.locator = locator;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public ComponentLocator getLocator() {
		return locator;
	}
	public void setLocator(ComponentLocator locator) {
		this.locator = locator;
	}
}
