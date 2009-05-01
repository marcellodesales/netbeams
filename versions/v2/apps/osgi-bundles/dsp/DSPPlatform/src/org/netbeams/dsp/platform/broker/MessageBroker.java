package org.netbeams.dsp.platform.broker;

/**
 * Simple Message Broker implementation.
 * 
 * TODO: Handle concurrency.
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.platform.matcher.MatchRule;
import org.netbeams.dsp.platform.matcher.MatchTarget;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.netbeams.dsp.util.ErrorCode;
import org.netbeams.dsp.util.NetworkUtil;

public class MessageBroker implements MessageBrokerAccessor {

    private static final Logger log = Logger.getLogger(MessageBroker.class);

    // Used only Locally to enable communication between the broker and local platform components.

    private Map<String, DSPComponent> components;
    // Assume at most one type per node
    private Map<String, DSPComponent> componentsByType;

    private Matcher matcher;

    public MessageBroker(Matcher matcher) {
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

    public void start() throws DSPException {
        log.info("start()");
    }

    public void stop() throws DSPException {
        log.info("stop()");
    }

    // ////////////////////////////////////////////////////////
    // //////// MessageBrokerAccessor Implementation //////////
    // ////////////////////////////////////////////////////////

    /**
     * Deliver the message to the local target DSP component or send to channel(s) to forward to other nodes.
     * 
     * TODO: The current implementation sends one message each time to channels. Maybe we can optimize it by send
     * messages in batch. This can be done in the Broker or Channel.
     * 
     * @param consumers Targets for this message
     * @throws DSPException If the message already has consumers.
     * @Override
     */
    public void send(Message message) throws DSPException {

        log.info("send invoked. Summary: " + messageSummary(message));

        Map<Target, Boolean> targets = new HashMap<Target, Boolean>();
        ComponentIdentifier originalConsumer = message.getHeader().getConsumer();
        
        Collection<MatchRule> matchingRules = matcher.match(message);
        
        // Deliver messages
        if (!matchingRules.isEmpty()) {
            for (MatchRule rule : matchingRules) {
                // We can deliberately ignore the message
                log.debug("Applying rule: " + rule.getRuleID());
                
                ComponentIdentifier targetConsumer = new ComponentIdentifier();
                // Apply consumer type target
                String typeTarget = rule.getTarget().getConsumerType();
                if("SAME".equals(typeTarget)){
                	if(originalConsumer != null){
                		targetConsumer.setComponentType(originalConsumer.getComponentType());
               	}else{
                		log.debug("SAME type target is incompatible with NO consumer");
                		continue;
                	}
                }else {
                	// The rule dictates the target type
                	targetConsumer.setComponentType(rule.getTarget().getConsumerType());
                }
                // Apply consumer address
                String addressTarget = rule.getTarget().getConsumerType();
                if("SAME".equals(addressTarget)){
                	if(originalConsumer != null){
                		targetConsumer.setComponentLocator(originalConsumer.getComponentLocator());
                	}else{
                		log.debug("SAME address target is incompatible with NO consumer");
                		continue;
                	}
                }else {
                	// The rule dictates the target type
                	targetConsumer.setComponentLocator(rule.getTarget().getLocator());
                }    
                
                // Deliver the message to the local component
                
                // Was the message delivered to the same target before?
                String tt = targetConsumer.getComponentType();
                String ta = (NodeAddressHelper.isLocal(targetConsumer.getComponentLocator().getNodeAddress().getValue()))?
                				"LOCAL" : targetConsumer.getComponentLocator().getNodeAddress().getValue();
                Target target = new Target(tt, ta);
                if(targets.containsKey(target)){
                	log.debug("Message already delivered to this consumer");
                	continue;
                }
                
                DSPComponent localComponent = null;
                if(rule.getTarget().getGatewayType() != null){
                	// Deliver through the gate way
                	log.debug("Delivering through gateway " + rule.getTarget().getGatewayType());
                	
                	localComponent = this.obtainDSPComponent(rule.getTarget().getGatewayType());
                	if(localComponent == null){
                		log.debug("Gateway not found...");
                		continue;
                	}
                }else{
                	// If no get way it MUST be local
                	String address  = targetConsumer.getComponentLocator().getNodeAddress().getValue();
                	if(!NodeAddressHelper.isLocal(address)){
                		log.warn("Node Address is neither local nor delivered through gateway: " +  address);
                		continue;
                	}
                	log.debug("Delivering localy to " + targetConsumer.getComponentType());
                	localComponent = this.obtainDSPComponent(targetConsumer.getComponentType());
                	if(localComponent == null){
                		log.debug("DSP component not found...");
                		continue;
                	}
                }
                               
                // Set the new consumer
                log.debug("Delivering ...");
                message.getHeader().setConsumer(targetConsumer);
                localComponent.deliver(message);
                targets.put(target, Boolean.TRUE);
            }

        } else {
            log.warn("No rules matches this message");
        }
    }

    /**
     * Matches to any local representation or to ALL/ANY producer IP.
     * 
     * @param producer
     * @param matchTarget
     * @return
     */
    private boolean isMatchForLocalNode(ComponentIdentifier messageConsumer, MatchTarget matchTarget) {

        ComponentLocator consumerLocator = messageConsumer.getComponentLocator();
        ComponentLocator targetLocator = matchTarget.getLocator();

        log.debug("Consumer Node Addr: " + consumerLocator.getNodeAddress().getValue());
        log.debug("Rule Target Node Addr: " + targetLocator.getNodeAddress().getValue());
        log.debug("Is the consumer locator the same as the target?");
        log.debug("If not, is it the same as LOCAL, LOCALHOST, or Loopback IP?");
        return consumerLocator.getNodeAddress().getValue().equals("")
                || consumerLocator.getNodeAddress().getValue().equals(targetLocator.getNodeAddress().getValue())
                || "ALL".equals(consumerLocator.getNodeAddress().getValue())
                || "ANY".equals(consumerLocator.getNodeAddress().getValue())
                || "LOCALHOST".equalsIgnoreCase(consumerLocator.getNodeAddress().getValue())
                || "LOCAL".equalsIgnoreCase(consumerLocator.getNodeAddress().getValue())
                || "127.0.0.1".equals(consumerLocator.getNodeAddress().getValue());
    }

    /**
     * @Override
     */
    public Message sendWaitForReply(Message request) throws DSPException {

        log.info("sendWaitForReply() message id : " + request.getMessageID());
        if (log.isDebugEnabled()) {
            log.debug("message summary: " + messageSummary(request));
        }

        ComponentIdentifier consumer = request.getHeader().getConsumer();
        // The MUST be exactly one consumer
        if (consumer == null) {
            throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER,
                    "sendWaitForReply insterface requires exaclty ONE consumer");
        }
        DSPComponent component = obtainDSPComponent(consumer);
        if (component != null) {
            return component.deliverWithReply(request);
        }
        // No match found
        throw new DSPException(ErrorCode.ERROR_NO_DSPCOMPONENT_FOUND, "No DSPComponent found");
    }

    /**
     * @Override
     */
    public Message sendWaitForReply(Message request, long waitTime) throws DSPException {

        log.info("sendWaitForReply(req,time) message id : " + request.getMessageID());
        if (log.isDebugEnabled()) {
            log.debug("message summary: " + messageSummary(request));
        }

        ComponentIdentifier consumer = request.getHeader().getConsumer();
        // The MUST be exactly one consumer
        if (consumer == null) {
            throw new DSPException(ErrorCode.ERROR_INTERFACE_REQUIRES_ONE_CONSUMER,
                    "sendWaitForReply insterface requires exaclty ONE consumer");
        }
        DSPComponent component = obtainDSPComponent(consumer);
        if (component != null) {
            return component.deliverWithReply(request, waitTime);
        }
        // No match found
        throw new DSPException(ErrorCode.ERROR_NO_DSPCOMPONENT_FOUND, "No DSPComponent found");
    }

    // ///////////////////////////////////
    // //////// Private Section //////////
    // ///////////////////////////////////

    private Set<MatchRule> obtainMatchingRulesThroughCriteria(Message message) {
        Set<MatchRule> result = new HashSet<MatchRule>();
        result.addAll(matcher.match(message));
        return result;
    }

    private DSPComponent obtainDSPComponent(ComponentIdentifier consumerIdentifier) {
        // Let's try the UUID first
        if (consumerIdentifier != null) {
            String comonentNodeId = consumerIdentifier.getComponentLocator().getComponentNodeId();
            if (comonentNodeId != null) {
                DSPComponent a = components.get(comonentNodeId);
                return a != null ? a : componentsByType.get(consumerIdentifier.getComponentType());
            } else {
                return componentsByType.get(consumerIdentifier.getComponentType());
            }
        }
        return null;
    }

    private DSPComponent obtainDSPComponent(String componentType) {
       if (componentType != null) {
    	   return componentsByType.get(componentType);
        }
        return null;
    }
    private String messageSummary(Message message) {
        StringBuilder buff = new StringBuilder();
        buff.append("ID=").append(message.getMessageID()).append("; ");
        buff.append("prodID=").append(message.getHeader().getProducer().getComponentLocator().getComponentNodeId()).append("; ");
        buff.append("prodType=").append(message.getHeader().getProducer().getComponentType()).append("; ");
        buff.append("prodIP=").append(message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue()).append("; ");
        buff.append("CT=").append(message.getContentType()).append("; ");
        buff.append("CORR=").append(message.getHeader().getCorrelationID()).append("; ");
        ComponentIdentifier consumer = message.getHeader().getConsumer();
        if (consumer != null) {
            buff.append("consType=").append(consumer.getComponentType()).append("; ");
        	if(message.getHeader().getConsumer().getComponentLocator() != null){
        		buff.append("consID=").append(message.getHeader().getConsumer().getComponentLocator().getComponentNodeId()).append("; ");
        	}
        	if(message.getHeader().getConsumer().getComponentLocator().getNodeAddress() != null){
        		buff.append("consIP=").append(message.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue());
        	}
        }else{
        	buff.append("NO Consumer");
        }
        return buff.toString();
    }
    
    private static class Target{
 
		private String type;
    	private String address;
    	
    	private int hashCode;
       	
    	public Target(String type, String address) {
			super();
			this.type = type;
			this.address = address;
			hashCode = (type + address).hashCode();
		}
    	
    	public int hashCode(){
    		return hashCode;
    	}
    	
    	public boolean equals(Object obj){
    		Target t = (Target)obj;
    		return type.endsWith(t.type) && address.endsWith(t.address);
    	}
    }
}
