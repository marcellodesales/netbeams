package org.netbeams.dsp.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jdom.Element;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageType;
import org.netbeams.dsp.MessageTypes;
import org.netbeams.dsp.util.ErrorCode;
import org.netbeams.dsp.util.Log;

public class ComponentRegistryImpl implements ComponentRegistry {

	public static final String COMPONENT_TYPE = "org.dmp.components.registry";
	public static final String REGISTER_MSG_TYPE = "org.dmp.components.registry.register";
	
	// DMPComponent information. 
	// TODO: I still need tread it as DMP component.
	private DMPContext context;
	private ComponentLocator locator;
	private ComponentDescriptor descritor;

	// Registered component information
	private Map<UUID, ComponentRegister> components;
	// TODO: Design a good data structure
//	private Map<String, List<ComponentLocator>> consumedMsgTypeIndex;
//	private Map<String, List<ComponentLocator>> producedMsgTypeIndex;
	
	/**
	 *  When the component was initialized
	 */
	private Date whenInitialized;
	
	private Object lock;
	
	public ComponentRegistryImpl(){
		lock = new Object();
		createDesctriptor();
	}

	
	@Override
	public void initComponent() {
		components = new HashMap<UUID, ComponentRegister>();
		whenInitialized = new Date();
	}
	
	@Override
	public Date whenInitialized(){
		return whenInitialized;
	}

	@Override
	public ComponentLocator getLocator() {
		return locator;
	}
	
	@Override
	public String getType() {
		return COMPONENT_TYPE;
	}

	@Override
	public ComponentDescriptor getDescriptor() {
		return descritor;
	}

	@Override
	public void initComponent(ComponentLocator locator, DMPContext context) throws DMPException {
		this.locator = locator;
		this.context = context;
	}

	@Override
	public Message messageDelivered(Message request) throws DMPException {
		
	}

	@Override
	public void startComponent() {
		// DO nothing for now ...
	}
	
	@Override
	public void stopComponent() {
	}

	@Override
	public void push(Message msg) throws DMPException {
		if(Message.Category.ACTION != msg.getType().getCategory()){
			throw new DMPException(ErrorCode.ERROR_INVALID_DATA_CATEGORY,"Invalid data category {0}. Expected {1}.",
					new Object[]{msg.getType().getCategory(), Message.Category.ACTION});
		}		
		processMessage(msg);
	}


	/**
	 * This method should only be invoked by platform components. It is a access point to make the platform itself 
	 * simpler as it by passed the message layer.
	 */
	@Override
	public void register(ComponentLocator locator, ComponentDescriptor descriptor) throws DMPException {
		Log.log("ComponentRegistryImpl.register(): " + descriptor.getType());
		// TODO: Prototype only implementation
		ComponentRegister register = new ComponentRegister(locator, descriptor);
		components.put(locator.getUUID(), register);
	}

	@Override
	public void unregister(UUID uuid) throws DMPException {
		// TODO: Prototype only implementation
		components.remove(uuid);
		
	}

	/**
	 * Provide a collection of DMP components registered after <code>lastSyncDate</code>.
	 * If the invokation of this method occurs after this component is started, all registered information
	 * MUST be returned desite of <code>lastSyncDate</code>.
	 */
	@Override
	public Collection<ComponentRegister> registeredComponents(Date lastSyncDate) {
		ArrayList<ComponentRegister> result = new ArrayList<ComponentRegister>();
		for(ComponentRegister register: components.values()){
			if(lastSyncDate.before(register.getWhen())){
				result.add(register);
			}
		}
		return result;
	}

	public Collection<ComponentRegister> unregisteredComponents(Date lastSync){
		//TODO: Implement some unregistered log infrastructure
		return new ArrayList<ComponentRegister>(); 
		
	}
	
	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	/**
	 * TODO: For now hard code the component descriptor
	 */
	private void createDesctriptor() {
		descritor = new ComponentDescriptor();
		descritor.setType(COMPONENT_TYPE);
		// Produced message
		MessageType msgType = new MessageType(Message.Category.ACTION, REGISTER_MSG_TYPE);
		MessageTypes msgTypes = new MessageTypes();
		msgTypes.addType(msgType);
		
		descritor.setMsgTypesConsummed(msgTypes);
	}
	
	private Message processMessage(Message data) throws DMPException {
		
		Element action = (Element)data.getBodyContent();
		String command = action.getChild("command").getTextTrim();
		if("register".equals(command)){
			register(action);
			return null;
		}else if("unregister".equals(command)){
			unregister(action);
			return null;
		}else if("lookup".equals("lookup")){
			return lookup(action);
		}else{
			throw new DMPException(ErrorCode.ERROR_INVALID_MESSAGE, "", null);
		}
	}

	private Message lookup(Element action) {
		// TODO Auto-generated method stub
		return null;
	}

	private void unregister(Element action) {
		// TODO Auto-generated method stub
		
	}

	private void register(Element action) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Message messageDeliveredWithReply(Message message)
			throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}


}
