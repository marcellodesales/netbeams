package org.netbeams.dsp.management.component;

import java.util.UUID;

import org.netbeams.dsp.DMPComponent;

public class ComponentEntry {

	private UUID _uuid;
	private ComponentState state;
	private DMPComponent component;
	
	public ComponentEntry(UUID uuid, ComponentState state, DMPComponent component){
		_uuid = uuid;
		this.state = state;
		this.component = component;
	
	}
	
	public DMPComponent getComponent() {
		return component;
	}
	public void setComponent(DMPComponent component) {
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
