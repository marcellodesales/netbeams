package org.netbeams.dsp.core.broker;

import java.util.Collection;

import org.jdom.Document;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageConsumer;
import org.netbeams.dsp.broker.MessageBroker;
import org.netbeams.dsp.broker.MessageBrokerAccessor;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.match.Matcher;
import org.netbeams.dsp.util.Log;

public class MessageBrokerImpl implements MessageBroker, MessageBrokerAccessor {

	private ComponentManager componentManager;
	private Matcher matcher;
	
	private Object lock;
	
	public MessageBrokerImpl(){
		lock = new Object();
	}
	
	@Override
	public void setComponentManger(ComponentManager componentManager) {
		this.componentManager = componentManager;	
	}
	
	@Override
	public ComponentManager getComponentManger() {
		// TODO Auto-generated method stub
		return componentManager;
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}
	
	@Override
	public Matcher getMatcher() {
		return matcher;
	}	

	@Override
	public void initComponent() throws DMPException {
		
		
	}

	@Override
	public void startComponent() throws DMPException {
		Log.log("MessageBrokerImpl.startComponent()");
		
	}
	
	@Override
	public void stopComponent() throws DMPException{
		Log.log("MessageBrokerImpl.stopComponent()");
	}

	@Override
	public Message pull(Message request) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * TODO: This is a prototype implementation only. Currently it just forward the call to the
	 * consumers. 
	 * 
	 */
	@Override
	public void push(Message data) throws DMPException {
		Log.log("MessageBrokerImpl.push()");
		
		ComponentManager currentCompManager = null;
		Matcher currentMatcher = null;
		synchronized (lock) {
			currentCompManager = componentManager;
			currentMatcher = matcher;
		}
		
		if(currentCompManager == null){
			Log.log("MessageBrokerImpl.push: Component Manager is unavailavble");
			return;
		}
		
		if(currentMatcher == null){
			Log.log("MessageBrokerImpl.push: Matcher is unavailavble");
			return;
		}
		
		Log.log((Document)data.getNativeRepresentation());
		
		// Find the matching consumers for this message.
		Collection<ComponentLocator> consumers = matcher.match(data);
				
		for(ComponentLocator loc: consumers){
			if(loc.isLocal()){
				// Find a reference to the component
				DMPComponent component = componentManager.obtainComponent(loc.getUUID());
				 // The match service guaratees the consumer implements MessageConsumer
				MessageConsumer consumer = (MessageConsumer)component;
				consumer.push(data);
			}else{
				throw new DMPException("Remote consumers are not yet supported");
			}
		}
		Log.log("MessageBrokerImpl.push(): end");

	}

	@Override
	public Message pull(Message request, long waitTime) throws DMPException {
		return null;
	}

	@Override
	public MessageBrokerAccessor getMessageBrokerAccessor() {
		return this;
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
	public void start() throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(Message data) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message sendWaitForReply(Message request) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message sendWaitForReply(Message request, long waitTime)
			throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}


}
