package org.netbeams.dsp.platform.management.component;

import java.util.UUID;

import org.netbeams.dsp.DSPComponent;

public class ComponentEntry {

	private UUID _uuid;
	private ComponentState state;
	private DSPComponent component;
	
	public ComponentEntry(UUID uuid, ComponentState state, DSPComponent component){
		_uuid = uuid;
		this.state = state;
		this.component = component;
	
	}
	
	public DSPComponent getComponent() {
		return component;
	}
	public void setComponent(DSPComponent component) {
		this.component = component;
	}
	public UUID getUUID() {
		return _uuid;
	}
	public void setComponentID(UUID uuid) {
		_uuid = uuid;
	}
	public ComponentState getState() {
		return state;
	}
	public void setState(ComponentState state) {
		this.state = state;
	}
	
}
