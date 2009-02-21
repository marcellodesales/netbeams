package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchTarget {

	private String componentType;
	private ComponentLocator locator;
	
	public MatchTarget(String componentType, ComponentLocator locator) {
		super();
		this.componentType = componentType;
		this.locator = locator;
	}
	
	public boolean equals(Object obj) {
	    if (!(obj instanceof MatchTarget)) {
	        return false;
	    }
	    return this.componentType.equals(((MatchTarget)obj).componentType) &&
	      this.locator.getNodeAddress().getValue().equals(((MatchTarget)obj).locator.getNodeAddress().getValue());
	}
	
	public int hashCode() {
	    return 10 * this.componentType.hashCode() + 20 * this.locator.hashCode();
	}
	
	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}


	public ComponentLocator getLocator() {
		return locator;
	}


	public void setLocator(ComponentLocator locator) {
		this.locator = locator;
	}

}
