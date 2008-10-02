package org.netbeams.dsp.management.component;

import java.util.UUID;

import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPContextFactory;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.registry.ComponentRegistry;

public interface ComponentManager {
	
	/**
	 * Invoke by the Platform before any other method is invoked.
	 * 
	 * @param contextFactory
	 * @throws DMPException
	 */
	public void init(DMPContextFactory contextFactory) throws DMPException;

	/**
	 * Invoked by the Platform.
	 * 
	 * @throws DMPException
	 */
	public void start() throws DMPException;
	
	/**
	 * Invoked by the Platform.
	 * 
	 * @throws DMPException
	 */
	public void stop() throws DMPException;
	
	/**
	 * Dependency injection by the Platform. Due its dynamic nature it is possible that the Platform
	 * invokes this method mutiple times during the life-cycle of the Component Manager depending on 
	 * Component Manager Availability. Implementataions MUST handle this fact accordingly.
	 * 
	 * @param componentRegistry
	 */
	public void setComponentRegistry(ComponentRegistry componentRegistry);
	
	public ComponentRegistry getComponentRegistry();
		
	/**
	 * Attach a component. The implementaion MUST create one UUID and a SNPContext
	 * through the SNPContextFactory and initialize the SNPComponent.
	 * 
	 * The created UUID MUST be returned.
	 * 
	 * TODO: Currently I am proposing that a component can have 5 states:
	 * 		 UNINSTALLED,
	 *		 INSTALLED,
	 *		 RESOLVED,
	 *		 STARTING,
	 *		 ACTIVE,
	 *		 STOPPING
	 * 
	 * 	  	 I need to make it consistent with OSGi. 
	 * 
	 * 
	 * @param component
	 * @param priority TODO
	 * @return
	 * @throws DMPException
	 */
	public UUID attach(long bindID, DMPComponent component, int priority) throws DMPException;
	
	public void detach(long bindID, DMPComponent component) throws DMPException;
		
	public void makeAvailable(long bindID, DMPComponent component) throws DMPException;
	
	public void makeUnavailable(long bindID, DMPComponent component) throws DMPException;
	
	public DMPComponent obtainComponent(UUID uuid);
	
	public void stopDMPComponents();


}
