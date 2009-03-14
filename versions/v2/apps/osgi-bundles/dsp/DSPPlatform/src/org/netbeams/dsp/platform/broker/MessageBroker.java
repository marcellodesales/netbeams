package org.netbeams.dsp.platform.broker;

/**
 * Simple Message Broker implementation.
 * 
 * TODO: Handle concurrency.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.matcher.MatchRule;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.netbeams.dsp.util.ErrorCode;

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

        log.info("send() message id : " + message.getMessageID());
        if (log.isDebugEnabled()) {
            log.debug("message summary: " + messageSummary(message));
        }

        Set<MatchRule> consumers = obtainConsumers(message);
        // Deliver messages
        if (!consumers.isEmpty()) {
            for (MatchRule consumerRule : consumers) {
                // We can deliberately ignore the message
                if (GlobalComponentTypeName.NO_COMPONENT.equals(consumerRule.getTarget().getConsumerComponentType())) {
                    continue;
                }   
                
                //Get the target identifier of the component that has to be sent    
                ComponentIdentifier targetComponentcompIdent = new ComponentIdentifier();
                targetComponentcompIdent.setComponentLocator(consumerRule.getTarget().getLocator());
                targetComponentcompIdent.setComponentType(consumerRule.getTarget().getConsumerComponentType());
                
                //Update the header of the message with the target component from the component
                if (message.getHeader().getConsumer() == null) {
                    message.getHeader().setConsumer(targetComponentcompIdent);
                    log.debug("Message Header updated: Consumer for message had not been defined...");
                }
                
                if (this.isMatchForLocalNode(message.getHeader().getProducer(), consumerRule)) {
                    log.debug("Message is intendend for local delivery with the given rule...");
                                        
                    DSPComponent localComponent = this.obtainDSPComponent(targetComponentcompIdent);
                    
                    if (localComponent != null) {
                        log.debug("Ready to deliver message to the local component...");
                        localComponent.deliver(message);
                    } else {
                        log.error("$$ Requered dsp component " + consumerRule.getTarget().getConsumerComponentType() + 
                                " not attached!!!");
                    }

                } else {
                    log.debug("Message is intendend for Global delivery...");   
                    String gatewayComponentType = consumerRule.getTarget().getGatewayComponentType();
                    if (gatewayComponentType != null) {
                        log.debug("Message is to be delivered through the gateway " + gatewayComponentType);
                        DSPComponent gatewayComponent = componentsByType.get(gatewayComponentType);
                        gatewayComponent.deliver(message);
                    
                    } else {
                        log.error("$$ Requered Gateway dsp component " + consumerRule.getTarget().getConsumerComponentType() + 
                                " not attached!!!");
                    }                    
                }
            }
        } else {
            log.warn("No consumers found for message");
        }
    }
    
    /**
     * Matches to any local representation or to ALL/ANY producer IP.
     * 
     * @param producer
     * @param mr
     * @return
     */
    private boolean isMatchForLocalNode(ComponentIdentifier messageProducer, MatchRule mr) {

        ComponentLocator producerLocator = messageProducer.getComponentLocator();
        ComponentLocator criteriaLocator = mr.getCriteria().getLocator();

        log.debug("Rule Criteria: " + criteriaLocator.getNodeAddress().getValue());
        log.debug("Against Producer: " + producerLocator.getNodeAddress().getValue());
        log.debug("Is the producer locator the same as the criteria?");
        log.debug("If not, is it the same as LOCAL, LOCALHOST, or Loopback IP?");
        return producerLocator.getNodeAddress() == null || producerLocator.getNodeAddress().getValue().equals("")
                || producerLocator.getNodeAddress().getValue().equals(criteriaLocator.getNodeAddress().getValue())
                || "ALL".equals(criteriaLocator.getNodeAddress().getValue())
                || "ANY".equals(criteriaLocator.getNodeAddress().getValue())
                || "LOCALHOST".equalsIgnoreCase(criteriaLocator.getNodeAddress().getValue())
                || "LOCAL".equalsIgnoreCase(criteriaLocator.getNodeAddress().getValue())
                || "127.0.0.1".equals(criteriaLocator.getNodeAddress().getValue());
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

    private Set<MatchRule> obtainConsumers(Message message) {
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
        } return null;
    }

    private String messageSummary(Message message) {
        StringBuilder buff = new StringBuilder();
        buff.append("id=").append(message.getMessageID()).append("; ");
        buff.append("prod=").append(message.getHeader().getProducer().getComponentType()).append("; ");
        buff.append("content_type=").append(message.getContentType()).append("; ");
        ComponentIdentifier consumer = message.getHeader().getConsumer();
        if (consumer != null) {
            buff.append("consumer=");
            buff.append(consumer.getComponentType()).append("; ");
            buff.append("node=");
            buff.append(consumer.getComponentLocator().getNodeAddress().getValue()).append("; ");

        }
        return buff.toString();
    }
}
