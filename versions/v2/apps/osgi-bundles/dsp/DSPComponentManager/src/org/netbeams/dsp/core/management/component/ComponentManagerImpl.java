package org.netbeams.dsp.core.management.component;

/**
 * Manages SNPComponents and Platform Components. 
 * 
 * This component....
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPContextFactory;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.NodeAddress;
import org.netbeams.dsp.management.component.ComponentEntry;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.management.component.ComponentState;
import org.netbeams.dsp.registry.ComponentRegistry;
import org.netbeams.dsp.util.Log;

public class ComponentManagerImpl implements ComponentManager {

	private DMPContextFactory contextFactory;
	private Map<Long, UUID> binds;
	private Map<UUID, ComponentEntry> entries;
	
	private ComponentRegistry componentRegistry;
	
	private Object lock;
	
	public ComponentManagerImpl(){
		lock = new Object();
		binds = new HashMap<Long,UUID>();	
		entries = new HashMap<UUID, ComponentEntry>();	
		
	}
	
	@Override
	public void init(DMPContextFactory contextFactory) throws DMPException {
		this.contextFactory = contextFactory;
	}

	@Override
	public void setComponentRegistry(ComponentRegistry componentRegistry) {
		synchronized (lock) {
			this.componentRegistry = componentRegistry;
		}	
	}
	
	@Override
	public ComponentRegistry getComponentRegistry() {
		synchronized (lock) {
			return componentRegistry;
		}
	}

	/**
	 * TODO: Implement better life-cycle
	 */
	@Override
	public void start() throws DMPException {
		for(ComponentEntry entry: entries.values()){
			DMPComponent component = entry.getComponent();
			component.startComponent();
		}
	}

	/**
	 *  Attach a DMP component and invoke the appropriate life-cycle.
	 *  
	 *  TODO: Currently the implementation generates a new UUID everytime the component is attached. 
	 *  	  The corrrect implementation should persist the UUID so the same component 
	 *        (based on component type) always get the same UUID.
	 *        
	 *        This implementation does almost no sanity check
	 */
	
	@Override
	public UUID attach(long bindID, DMPComponent component, int priority) throws DMPException {
		Log.log("ComponentManagerImpl.attach(): " + component.getType());
		
		// Init component
		UUID uuid = generateUUID();
		// Create the relationship between the bindID and the UUID
		binds.put(new Long(bindID), uuid);
		//TODO: Handle DNS better
		NodeAddress address = new NodeAddress("localhost");
		ComponentLocator locator = new ComponentLocator(uuid, address);
		DMPContext context = contextFactory.createContext();
		component.initComponent(locator, context);
		// TODO: Handle the component state properly
		ComponentEntry entry = new ComponentEntry(component.getLocator().getUUID(), ComponentState.ACTIVE, component);
		entries.put(component.getLocator().getUUID(), entry);
		
		start(component, locator, context);
		
		// TODO: Improve this mechanism
		// Register the component
		componentRegistry.register(component.getLocator(), component.getDescriptor());
		
		return uuid;
	}
	
	@Override
	public void detach(long bindID, DMPComponent component) throws DMPException {
	}
	
	@Override
	public void makeUnavailable(long bindID, DMPComponent component) throws DMPException{	
	}

	
	/**
	 * Start component.
	 * 
	 * TODO: This should be asychronous
	 * 
	 * @param component
	 * @param context 
	 * @param locator 
	 * @throws DMPException 
	 */
	private void start(DMPComponent component, ComponentLocator locator, DMPContext context) throws DMPException {
		component.initComponent(locator, context);
		component.startComponent();
	}

	@Override
	public DMPComponent obtainComponent(UUID uuid) {
		ComponentEntry entry = entries.get(uuid);
		return (entry != null)? entry.getComponent() : null;
	}
	
	private UUID generateUUID() {
		return UUID.randomUUID();
	}

	@Override
	public void stopDMPComponents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void makeAvailable(long bindID, DMPComponent component)
			throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws DMPException {
		// TODO Auto-generated method stub
		
	}

}
