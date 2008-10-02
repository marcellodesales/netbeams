package org.netbeams.dsp.registry;

/**
 * Register information about a component
 * 
 * @author Kleber Sales
 */
import java.util.Date;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;

public class ComponentRegister {
	
	private Date when;
	private ComponentLocator locator;
	private ComponentDescriptor descriptor;
	
	public ComponentRegister(ComponentLocator locator, ComponentDescriptor descriptor) {
		this.when = new Date();
		this.locator = locator;
		this.descriptor = descriptor;
	}

	public ComponentLocator getLocator() {
		return locator;
	}
	public void setLocator(ComponentLocator locator) {
		this.locator = locator;
	}
	public ComponentDescriptor getDescriptor() {
		return descriptor;
	}
	public void setDescriptor(ComponentDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public Date getWhen() {
		return when;
	}
	

}
