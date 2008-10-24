package org.netbeams.dsp.platform.broker;

/**
 * Simple Message Broker implementaion.
 *
 * TODO: Handle concurrency.
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.netbeams.dsp.util.ErrorCode;


public class MessageBroker implements MessageBrokerAccessor {
	
	private static final Logger log = Logger.getLogger(MessageBroker.class);

	// Used only Locally to enable communication between the broker and local platform components.
	private UUID uuid;

	private Object lock;

	private Map<UUID, DSPComponent> components;
	// Platform Component
	private Map<String, DSPComponent> componentsByType;

	private Matcher matcher;


	public MessageBroker(Matcher matcher){
		lock = new Object();
		components = new HashMap<UUID, DSPComponent>();
		componentsByType = new HashMap<String, DSPComponent>();
		this.matcher = matcher;
	}

	public MessageBrokerAccessor getMessageBrokerAccessor() {
		return this;
	}

	/**
	 * Inform the Broker a new DSP component is available. T
	 */
	public void attach(DSPComponent component) throws DSPException {
		// Save reference
		components.put(component.getUUID(), component);
		// Add Platform component index
		String type = component.getComponentType();
		componentsByType.put(type, component);
	}

	public void deattach(UUID uuid) throws DSPException {

	}


	public void start() throws DSPException{

	}


	public void stop() throws DSPException{

	}


	//////////////////////////////////////////////////////////
	////////// MessageBrokerAccessor Implementation //////////
	//////////////////////////////////////////////////////////

	/**
	 * Deliver the message to the local target DSP component or send to channel(s) to forward to other nodes.
	 *
	 * TODO: The current implementation sends one message each time to channels. Maybe we can optimize it
	 *       by send messages in batch. This can be done in the Broker or Channel.
	 *
	 * @param consumers Targets for this message
	 * @throws DSPException If the message already has consumers.
	 */
	@Override
	public void send(Message message) throws DSPException {
		Collection<ComponentIdentifier> consumers = obtainConsumers(message);

		// Deliver messages
		if(!consumers.isEmpty()){
			for(ComponentIdentifier consumer : consumers){
				// We can deliberatly ignore the message
				if(GlobalComponentTypeName.NO_COMPONENT.equals(consumer.getType())){
					continue;
				}
				DSPComponent component = obtainDSPComponent(consumer);
				if(component != null){
					component.deliver(message);
				}
			}
		}
	}

	@Override
	public Message sendWaitForReply(Message request) throws DSPException {
		List<ComponentIdentifier> consumers = request.getConsumers();
		// The MUST be exactly one consumer
		if(consumers.size() != 1){
			throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER, 
				"sendWaitForReply insterface requires exaclty ONE consumer");
		}
		ComponentIdentifier consumer = consumers.get(0);
		DSPComponent component = obtainDSPComponent(consumer);
		if(component != null){
			return component.deliverWithReply(request);
		}		
		// No match found
		throw new DSPException(ErrorCode.ERROR_NO_DSPCOMPONENT_FOUND, 
					"No DSPComponent found");
	}

	@Override
	public Message sendWaitForReply(Message request, long waitTime) throws DSPException {
		List<ComponentIdentifier> consumers = request.getConsumers();
		// The MUST be exactly one consumer
		if(consumers.size() != 1){
			throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER, 
				"sendWaitForReply insterface requires exaclty ONE consumer");
		}
		ComponentIdentifier consumer = consumers.get(0);
		DSPComponent component = obtainDSPComponent(consumer);
		if(component != null){
			return component.deliverWithReply(request, waitTime);
		}		
		// No match found
		throw new DSPException(ErrorCode.ERROR_NO_DSPCOMPONENT_FOUND, 
					"No DSPComponent found");
	}


	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////

	private Collection<ComponentIdentifier> obtainConsumers(Message message) {
		Collection<ComponentIdentifier> consumers  = message.getConsumers(); // Optional

		// If there are not target consumers, then try to find a match
		if(consumers.isEmpty()){
			consumers = match(message);
		}
		
		return consumers;
	}

	
	private Collection<ComponentIdentifier> match(Message message) {
		return matcher.match(message);
	}

	private DSPComponent obtainDSPComponent(ComponentIdentifier consumer) {
		DSPComponent component = null;
		// Let's try the UUID first
		ComponentLocator locator = consumer.getLocator();
		if(locator != null){
			UUID uuid = locator.getUUID();
			if(uuid != null){
				return components.get(uuid);
			}
		}
		String type = consumer.getType();
		return componentsByType.get(type);

	}


}
