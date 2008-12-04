package org.netbeams.dsp.platform.management.component;

/**
 * Component Manager is reposible for managing the local DSP components. It takes the components through their life-cycle ...
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPContextFactory;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.DSPPlatformComponent;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.PlatformException;
import org.netbeams.dsp.platform.broker.MessageBroker;
import org.netbeams.dsp.util.DSPUtils;
import org.netbeams.dsp.util.Log;

public class ComponentManager implements BaseComponent{

	private static final String TYPE = ComponentManager.class.getName();
	
	private String DSP_HOME;

	private String componentNodeId;
	
	private DSPContextFactory contextFactory;
	private Map<String, String> binds; // <?, Component Node ID>
	private Map<String, ComponentEntry> entries; // <Component Node ID, ComponentEntry>
	
	private DSPPlatformComponent directoryService;
	private MessageBroker broker;
	// Control the deployment units where the DSPComponents are deployed.
	private DeploymentController deploymentController;
	// Boostrap configuration
	private BootstrapConfigurator boostrapConfigurator;
	
	private Object lock;	
	
	public ComponentManager(){
		lock = new Object();
		binds = new HashMap<String,String>();	
		entries = new HashMap<String, ComponentEntry>();	
		directoryService = null; // The code might not have a Directory Service
	}
	
	@Override
	public String getComponentType() {
		// TODO Auto-generated method stub
		return TYPE;
	}

	@Override
	public String getComponentNodeId() {
		return componentNodeId;
	}

	@Override
	public void initComponent(String componentNodeId, DSPContext context)
			throws DSPException {
	}

	public void init(DSPContextFactory contextFactory, String dspHome) throws PlatformException {
		this.contextFactory = contextFactory;
		DSP_HOME = dspHome;
		boostrapConfigurator = new BootstrapConfigurator(DSP_HOME);
		boostrapConfigurator.init();
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
		String componentNodeId = UUID.randomUUID().toString();
		ComponentEntry entry = new ComponentEntry(componentNodeId, ComponentState.ACTIVE, component);
		binds.put(externalId, componentNodeId);
		entries.put(componentNodeId, entry);
		
		component.initComponent(componentNodeId, contextFactory.createContext());
		ComponentIdentifier indentifier = 
			DSPUtils.obtainIdentifier(component.getComponentType(), componentNodeId, NodeAddressHelper.LOCAL_NODEADDRESS);
		
		notifyDirectoryService(indentifier, component.getComponentDescriptor());		
		notifyMessageBroker(component);	
		component.startComponent();
		// Bootstrap messages
		sendBootstrapMessages(component);
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
	
	private void sendBootstrapMessages(DSPComponent component) throws DSPException {
		Log.log("sendBootstrapMessages to " + component.getComponentType());
		
		List<Message> messages = boostrapConfigurator.createMessages(this, component.getComponentType());
		for(Message message: messages){
			broker.getMessageBrokerAccessor().send(message);
		}		
	}


	
}
