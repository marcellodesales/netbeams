package org.netbeams.dsp.platform.management.component;

/**
 * Component Manager is reposible for managing the local DSP components. It takes the components through their life-cycle ...
 */

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPContextFactory;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.DSPPlatformComponent;
import org.netbeams.dsp.NodeAddress;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.platform.broker.MessageBroker;
import org.netbeams.dsp.util.DSPUtils;

public class ComponentManager implements BaseComponent{

	private static final String TYPE = "org.dsp.platform.componentmanager";

	private UUID uuid;
	
	private DSPContextFactory contextFactory;
	private Map<String, UUID> binds;
	private Map<UUID, ComponentEntry> entries;
	
	private DSPPlatformComponent directoryService;
	private MessageBroker broker;
	// Control the deployment units where the DSPComponents are deployed.
	private DeploymentController deploymentController;
	
	private Object lock;	
	
	public ComponentManager(){
		lock = new Object();
		binds = new HashMap<String,UUID>();	
		entries = new HashMap<UUID, ComponentEntry>();	
		directoryService = null; // The code might not have a Directory Service
	}
	
	@Override
	public String getComponentType() {
		// TODO Auto-generated method stub
		return TYPE;
	}

	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return uuid;
	}

	@Override
	public void initComponent(UUID uuid, DSPContext context)
			throws DSPException {
		// TODO Auto-generated method stub
		
	}

	public void init(DSPContextFactory contextFactory) throws PlatformException {
		this.contextFactory = contextFactory;
	}

	/**
	 * Invoked by the platform. Dependency Injection
	 * @param broker
	 */
	public void setMessageBroker(MessageBroker broker){
		this.broker = broker;
	}
	
	public void setDeploymentController(DeploymentController deploymentController){
		this.deploymentController = deploymentController;
		this.deploymentController.setComponentManager(this);
	}
	
	public void start() throws PlatformException{
		startDSPBundleController();
		obtainDirectoryService();
	}
				
	public void stop() throws PlatformException{
		// TODO: Clean up code...
	}


	public void attach(String externalId, DSPComponent component) throws DSPException{
		UUID uuid = UUID.randomUUID();
		ComponentEntry entry = new ComponentEntry(uuid, ComponentState.ACTIVE, component);
		binds.put(externalId, uuid);
		entries.put(uuid, entry);
		
		component.initComponent(uuid, contextFactory.createContext());
		ComponentIdentifier indentifier = 
			DSPUtils.obtainIdentifier(component.getComponentType(), uuid, NodeAddress.LOCAL_ADDRESS);
		
		notifyDirectoryService(indentifier, component.getComponentDescriptor());		
		notifyMessageBroker(component);	
		component.startComponent();
	}
	
	public void dettach(String externalId) throws DSPException{
		// TODO: Implement
	}

	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	private void startDSPBundleController() throws PlatformException {
		deploymentController.start();
	}


	private void notifyDirectoryService(ComponentIdentifier indentifier, ComponentDescriptor descriptor) {
		// Notify the DirectotyService
		createNotificationService();
		
	}

	private void createNotificationService() {
		// TODO Implement
		
	}

	private void notifyMessageBroker(DSPComponent component) throws DSPException {
		broker.attach(component);
	}
	
	private void obtainDirectoryService() {
		// TODO Implement		
	}

	
}
