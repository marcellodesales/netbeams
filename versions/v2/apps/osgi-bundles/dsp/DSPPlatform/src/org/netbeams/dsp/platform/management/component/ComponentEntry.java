package org.netbeams.dsp.platform.management.component;

import org.netbeams.dsp.DSPComponent;

public class ComponentEntry {

	private String componentNodeId;
	private ComponentState state;
	private DSPComponent component;
	
	public ComponentEntry(String componentNodeId, ComponentState state, DSPComponent component){
		this.componentNodeId = componentNodeId;
		this.state = state;
		this.component = component;
	
	}
	
	public DSPComponent getComponent() {
		return component;
	}
	public void setComponent(DSPComponent component) {
		this.component = component;
	}
	public String getComponentNodeId() {
		return componentNodeId;
	}
	public void setComponentID(String componentNodeId) {
		this.componentNodeId = componentNodeId;
	}
	public ComponentState getState() {
		return state;
	}
	public void setState(ComponentState state) {
		this.state = state;
	}
	
}
