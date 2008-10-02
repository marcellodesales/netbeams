package org.netbeams.dsp.platform.manamgement.component;

/**
 * Manages SNPComponents and Platform Components. 
 * 
 * This component....
 */
import java.util.UUID;

import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPContextFactory;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.registry.ComponentRegistry;

public abstract class ComponentManagerImpl implements ComponentManager {

	
	@Override
	public void init(DMPContextFactory contextFactory) throws DMPException {
	}

	/**
	 * TODO: Implement better life-cycle
	 */
	@Override
	public void start() throws DMPException {
	}

	@Override
	public UUID attach(long bindID, DMPComponent component, int priority) throws DMPException {
		return null;
	}

	@Override
	public DMPComponent obtainComponent(UUID uuid) {
		return null;
	}
	
	private UUID generateUUID() {
		return null;
	}

	@Override
	public void setComponentRegistry(ComponentRegistry componentRegistry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ComponentRegistry getComponentRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void detach(long bindID, DMPComponent component) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void makeUnavailable(long bindID, DMPComponent component) throws DMPException{	
	}


}
