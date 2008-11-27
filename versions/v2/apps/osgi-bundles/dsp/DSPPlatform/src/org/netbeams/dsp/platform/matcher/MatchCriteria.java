package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchCriteria {
	
	private String componentType;
	private ComponentLocator locator;
	
	public MatchCriteria(String componentType, ComponentLocator locator) {
		super();
		this.componentType = componentType;
		this.locator = locator;
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
