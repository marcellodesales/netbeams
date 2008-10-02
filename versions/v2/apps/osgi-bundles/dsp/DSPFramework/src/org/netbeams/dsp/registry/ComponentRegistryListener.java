package org.netbeams.dsp.registry;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;

public interface ComponentRegistryListener {
	
	public void registered(ComponentLocator locator, ComponentDescriptor descriptor);
	
	public void unregistered(ComponentLocator locator);

}
