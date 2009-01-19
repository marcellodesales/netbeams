package org.netbeams.dsp.platform.broker;

/**
 * Simple Message Broker implementaion.
 *
 * TODO: Handle concurrency.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.netbeams.dsp.util.DSPUtils;
import org.netbeams.dsp.util.ErrorCode;

public class MessageBroker implements MessageBrokerAccessor {
	
	private static final Logger log = Logger.getLogger(MessageBroker.class);

	// Used only Locally to enable communication between the broker and local platform components.
	private UUID uuid;

	private Object lock;

	private Map<String, DSPComponent> components;
	// Assume at most one type per node
	private Map<String, DSPComponent> componentsByType;

	private Matcher matcher;


	public MessageBroker(Matcher matcher){
		lock = new Object();
		components = new HashMap<String, DSPComponent>();
		componentsByType = new HashMap<String, DSPComponent>();
		this.matcher = matcher;
	}

	public MessageBrokerAccessor getMessageBrokerAccessor() {
		return this;
	}

	/**
	 * Inform the Broker a new DSP component is available.
	 */
	public void attach(DSPComponent component) throws DSPException {
		log.info("attach dsp component " + component.getComponentNodeId());
		
		// Save reference
		components.put(component.getComponentNodeId(), component);
		String type = component.getComponentType();
		componentsByType.put(type, component);
	}

	public void deattach(String componentID) throws DSPException {
		log.info("deattach dsp component " + componentID);
	}


	public void start() throws DSPException{
		log.info("start()");
	}


	public void stop() throws DSPException{
		log.info("stop()");
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
	 * @Override
	 */
	public void send(Message message) throws DSPException {
		
		log.info("send() message id : " + message.getMessageID());
		if(log.isDebugEnabled()){
			log.debug("message summary: " + messageSummary(message));
		}
		
		Collection<ComponentIdentifier> consumers = obtainConsumers(message);

		// Deliver messages
		if(!consumers.isEmpty()){
			for(ComponentIdentifier consumer : consumers){
				// We can deliberatly ignore the message
				if(GlobalComponentTypeName.NO_COMPONENT.equals(consumer.getComponentType())){
					continue;
				}
				DSPComponent component = obtainDSPComponent(consumer);
				if(component != null){
					component.deliver(message);
				}else{
					log.warn("No attached dsp component " + DSPUtils.toString(consumer));
				}
			}
		}else{
			log.warn("No consumers found for message");
		}
	}

	/**
	 * @Override
	 */
	public Message sendWaitForReply(Message request) throws DSPException {
		
		log.info("sendWaitForReply() message id : " + request.getMessageID());
		if(log.isDebugEnabled()){
			log.debug("message summary: " + messageSummary(request));
		}
			
		ComponentIdentifier consumer = request.getHeader().getConsumer();
		// The MUST be exactly one consumer
		if(consumer == null){
			throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER, 
				"sendWaitForReply insterface requires exaclty ONE consumer");
		}
		DSPComponent component = obtainDSPComponent(consumer);
		if(component != null){
			return component.deliverWithReply(request);
		}		
		// No match found
		throw new DSPException(ErrorCode.ERROR_NO_DSPCOMPONENT_FOUND, 
					"No DSPComponent found");
	}

	/**
	 * @Override
	 */
	public Message sendWaitForReply(Message request, long waitTime) throws DSPException {
		
		log.info("sendWaitForReply(req,time) message id : " + request.getMessageID());
		if(log.isDebugEnabled()){
			log.debug("message summary: " + messageSummary(request));
		}

		ComponentIdentifier consumer = request.getHeader().getConsumer();
		// The MUST be exactly one consumer
		if(consumer == null){
			throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER, 
				"sendWaitForReply insterface requires exaclty ONE consumer");
		}
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
		Collection<ComponentIdentifier> result = new ArrayList<ComponentIdentifier>();
		
		ComponentIdentifier consumer  = message.getHeader().getConsumer(); // Optional
		// If there are not target consumers, then try to find a match
		if(consumer != null){
			log.debug("Message has pre-defined consumer");
			result.add(consumer);			
		}
		// Try to find more matches
		result.addAll(match(message));
		
		return result;
	}

	
	private Collection<ComponentIdentifier> match(Message message) {
		return matcher.match(message);
	}

	private DSPComponent obtainDSPComponent(ComponentIdentifier consumer) {
		DSPComponent component = null;
		// Let's try the UUID first
		ComponentLocator locator = consumer.getComponentLocator();
		if(locator != null){
			String comonentNodeId = locator.getComponentNodeId();
			if(comonentNodeId != null){
				return components.get(comonentNodeId);
			}else{
				return componentsByType.get(consumer.getComponentType());
			}
		}
		String type = consumer.getComponentType();
		return componentsByType.get(type);
	}

	private String messageSummary(Message message) {
		StringBuffer buff = new StringBuffer();
		buff.append("id=").append(message.getMessageID()).append("; ");
		buff.append("producer=").append(message.getHeader().getProducer().getComponentType()).append("; ");
		buff.append("content type=").append(message.getContentType()).append("; ");
		ComponentIdentifier consumer = message.getHeader().getConsumer();
		if(consumer != null){
			buff.append("consumer=");
			buff.append(consumer.getComponentType()).append(",");
		}
		return buff.toString();
	}


}
