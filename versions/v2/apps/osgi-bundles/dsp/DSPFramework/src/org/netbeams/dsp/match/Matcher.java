package org.netbeams.dsp.match;

import java.util.Collection;

import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.DMPPlatformComponent;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.registry.ComponentRegistry;

public interface Matcher extends DMPPlatformComponent{

	/**
	 * Dependency injection. Invoke by the platform implementation.
	 * 
	 * @param matcher registry
	 */
	public void setComponentRegistry(ComponentRegistry registry);
	public ComponentRegistry getComponentRegistry();
	
	/**
	 * Invoked by the platform when it initializes. This method is the first to be called.
	 * 
	 * @param register ComponentRegister in the platform
	 */
	public void initComponent() throws DMPException;
	
	/**
	 * Invoked by the platform after all the platform components are initialized and it ready to start.
	 */
	public void startComponent() throws DMPException;
	
	public void stopComponent() throws DMPException;
	
	/**
	 * Invoked by the DataBroker to find all the match componens to which match the message
	 * 
	 * @param message
	 * @return Not null list of components.
	 */
	public Collection<ComponentLocator> match(Message message);
}
