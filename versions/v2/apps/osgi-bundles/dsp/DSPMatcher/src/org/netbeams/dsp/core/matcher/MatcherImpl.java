package org.netbeams.dsp.core.matcher;

/**
 * This is a basic implementation of the Matcher.
 * 
 *  This initial verions is meant only for the initial prototype. It re-creates the matching information
 *  everytime is starts. There is no persistence. 
 *  
 *  A better implementation would be persist the matching information and ask the register the latest updates
 *  since last update.
 *  
 *  I still need to think a little bit more on how the callback mechanism will work with this update.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.DMPPlatformComponent;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.MessageType;
import org.netbeams.dsp.MessageTypes;
import org.netbeams.dsp.message.matcher.MatchReply;
import org.netbeams.dsp.message.matcher.MatchRequest;
import org.netbeams.dsp.registry.ComponentRegister;
import org.netbeams.dsp.registry.ComponentRegistry;
import org.netbeams.dsp.util.Log;

public class MatcherImpl implements DMPPlatformComponent {

	private DMPContext context;
	private ComponentLocator locator;
	
	private HashMap<MessageType, List<ComponentLocator>> matchMap;
	private ComponentRegistry registry;

	private Date lastSyncTimeWithRegistry;
	
	private Engine engine;
	
	private Object lock;
	
	public MatcherImpl(){
		lock = new Object();
		matchMap = new HashMap<MessageType, List<ComponentLocator>>();
	}
	
	@Override
	public void initComponent(ComponentLocator locator, DMPContext context) throws DMPException {
		this.context = context;	
		this.locator = locator;
		lastSyncTimeWithRegistry = getRestTime();
	}
	
	@Override
	public void activate() throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivate() throws DMPException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void startComponent() throws DMPException {
		engine = new Engine();
		engine.start();
	}
	
	@Override
	public void stopComponent() throws DMPException {
		if(engine != null){
			engine.stop();
		}
	}
	
	@Override
	public ComponentDescriptor getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentLocator getLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void messageDelivered(Message message) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message messageDeliveredWithReply(Message message) throws DMPException {
		return processMessage(message);
	}

	public void setComponentRegistry(ComponentRegistry registry) {
		synchronized (lock) {
			// Is the Component Regisgtry unavailable? If so keep the current informatation about the
			// registry, but do not try to update it until it is available again.
			this.registry = registry;
		}
	}
	
	public ComponentRegistry getComponentRegistry() {
		synchronized (lock) {
			return registry;
		}
	}

	
	
	
	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	/*
	@Override
	public Collection<ComponentLocator> match(Message message) {
		
		Collection<ComponentLocator> result = new ArrayList<ComponentLocator>();
		
		ComponentLocator producer = null;
		try {
			producer = message.getProducer();
		} catch (DMPException e) {
			Log.log("Problems obtaining message producer", e);
			return result;
		}
		MessageType msgType = null;
		try {
			msgType = message.getType();
		} catch (DMPException e) {
			Log.log("Problems obtaining message type", e);
			return result;
		}
		
		Log.log("MatcherImpl.match(): For UUID[" + producer.getUUID() + "] " + 
				msgType);
		
		List<ComponentLocator> consumers = null;
		synchronized (matchMap) {
			consumers = matchMap.get(msgType);
		}
		
		if(consumers != null){
			result.addAll(consumers);
			for(ComponentLocator loc: result){
				Log.log("MatcherImpl.match(): found UUID[" + loc.getUUID() + "] ");
			}
		}
		
		Log.log("MatcherImpl.match(): end");
		return result;
	}
	*/

	private Message processMessage(Message message) throws DMPException {
		Message replyMessage = null;
		Object body = message.getBody();
		if (context instanceof MatchRequest) {
			MatchRequest request = (MatchRequest) body;
			MatchReply messageReply = processMatchRequest();
			replyMessage = createMessage()
			replyMessage = MessageFactory.newMessage(component, categorty, type);
			
			
		}
		return createMessage;
	}

	
	private Date getRestTime(){
		return new Date(0L);
	}

	/**
	 * Create the information and data structures to support message type matching between producers and
	 * consumers.
	 * 
	 * TODO: The current implementation only maps a produced type to list of consumers. It MUST be enhanced
	 * before any serious deployment.
	 */
	private void updateMatchInformation() {
		Log.log("MatcherImpl.updateMatchInformation()");
		
		ComponentRegistry compRegistry = null;
		Date currentLastSync = null;
		synchronized (lock) {
			compRegistry = registry;
		}
		if(compRegistry == null){
			Log.log("Component Registry is not available");
			return;
		}
		
		currentLastSync = lastSyncTimeWithRegistry;
		
		// Was the Component Registry initialized after the last sync?
		if(currentLastSync.before(compRegistry.whenInitialized())){
			currentLastSync = getRestTime();

		}
		
		Date newLastSync = currentLastSync;
		// TODO: For the prototype only store the consumers for the type.
		//       An additional constraint is that only full match is used
		Collection<ComponentRegister> registers = compRegistry.registeredComponents(newLastSync);
		Log.log("MatcherImpl.buildMatchMap(): Found " + registers.size() + " components");
		// TODO: Improve critical section implementation
		synchronized (matchMap) {
			//  Add produced message types first.
			for(ComponentRegister reg : registers){
				Log.log("MatcherImpl.buildMatchMap(): UUID[" + reg.getLocator().getUUID() + "]" + 
						reg.getDescriptor().getType());
				// Update lastSyncWithRegistry
				if(reg.getWhen().after(newLastSync)){
					newLastSync = reg.getWhen();
				}
				MessageTypes prodTypes = reg.getDescriptor().getMsgTypesProduced();
				if(prodTypes != null){
					for(MessageType msgType : prodTypes.getTypes()){
						addProducedType(msgType);
					}	
				}
			}
			// Add consumed message types
			for(ComponentRegister reg : registers){
				MessageTypes consumedTypes = reg.getDescriptor().getMsgTypesConsummed();
				if(consumedTypes != null){
					ComponentLocator locator = reg.getLocator();
					for(MessageType msgType : consumedTypes.getTypes()){
						addConsumedType(msgType, locator);
					}	
				}
			}		
		}
		// Update last synch time if necessary
		if(newLastSync.after(currentLastSync)){
			lastSyncTimeWithRegistry = newLastSync;
		}
		Log.log("MatcherImpl.buildMatchMap(): end. " + registers.size() + " components");

	}

	private void addConsumedType(MessageType msgType, ComponentLocator locator) {
		Log.log("MatcherImpl.addConsumedType(): " +  msgType);
		List<ComponentLocator> consumers = matchMap.get(msgType);
		// TODO: For this prototype we ignore consumers without a producer. 
		//       We need to think of systematic way to handle this case.
		if(consumers != null){
			// TODO: Another shorcut: Do not handle duplicates.
			consumers.add(locator);
		}else{
			Log.log("MatcherImpl.addConsumedType(): No producers found");
		}
	}



	private void addProducedType(MessageType msgType) {
		Log.log("MatcherImpl.addProducedType(): " +  msgType);
		if(matchMap.get(msgType) == null){
			matchMap.put(msgType, new ArrayList<ComponentLocator>());
		}
	}
	
	/////////////////////////////////
	////////// Inner Class //////////
	/////////////////////////////////
	
	private class Engine implements Runnable{
		
		private Thread thread;
		private boolean done;
		private boolean isFirstRun;
		
		private Engine(){
			done = false;
			thread = null;
		}
		
		private void start() throws DMPException{
			if(thread != null){
				throw new DMPException("Matcher Engine is alredy started");
			}
			thread = new Thread(this, "Matcher Engine");
			isFirstRun = true;
			done = false;
			thread.start();
		}
		
		private void stop(){
			if(thread != null){
				done = true;
				thread.interrupt();
			}
		}
		
		@Override
		public void run() {
			while(true){
				if(Thread.interrupted()){
					break;
				}
				if(done){
					break;
				}
				try {
					// TODO: Move this to cofiguration.
					long sleepTime = 30 * 1000;
					if(isFirstRun){
						sleepTime = 5 * 1000;
						isFirstRun = false;
					}
					Thread.sleep(30 * 1000);
				} catch (InterruptedException e){
					break;
				}
				updateMatchInformation();
			}	
			Log.log("Matcher Engine stopped...");
		}
	}




}

