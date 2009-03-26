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
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.matcher.MatchRule;
import org.netbeams.dsp.platform.matcher.MatchTarget;
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
        log.debug("message summary: " + messageSummary(message));

        Set<MatchRule> matchingRules = this.obtainMatchingRulesThroughCriteria(message);
        // Deliver messages
        if (!matchingRules.isEmpty()) {
            for (MatchRule consumerRule : matchingRules) {
                // We can deliberately ignore the message
                log.debug("Applying rule: " + consumerRule.getRuleID());
                ComponentIdentifier ruleTargetIdent = new ComponentIdentifier();
                ruleTargetIdent.setComponentLocator(consumerRule.getTarget().getLocator());
                ruleTargetIdent.setComponentType(consumerRule.getTarget().getConsumerComponentType());

                message.getHeader().setConsumer(ruleTargetIdent);
                log.debug("Message Header updated with the rule's target section...");

                log.debug("Does the matching rule have a gateway...");
                String gatewayComponentType = consumerRule.getTarget().getGatewayComponentType();
                log.debug("Does the matching rule have a gateway... " + (gatewayComponentType != null
                        && !gatewayComponentType.trim().equals("") ? gatewayComponentType : "Not Defined"));

                DSPComponent localComponent = null;
                if (gatewayComponentType == null) {

                    log.debug("Delivery must go directly to the target component: "
                            + ruleTargetIdent.getComponentType());
                    localComponent = this.obtainDSPComponent(ruleTargetIdent);

                    if (localComponent != null) {
                        log.debug("Ready to deliver message to the local component "
                                + localComponent.getComponentType());
                    } else {
                        log.error("$$ Requered dsp component " + consumerRule.getTarget().getConsumerComponentType()
                                + " not attached!!!");
                    }

                } else {

                    log.debug("Delivery must go through the gateway component " + gatewayComponentType);
                    localComponent = componentsByType.get(gatewayComponentType);
                    if (localComponent != null) {
                        log.error("Ready to deliver message to the local GATEWAY component "
                                + localComponent.getComponentType() + " not attached!!!");
                    } else {
                        log.error("$$ Requered gateway dsp component " + gatewayComponentType + " not attached!!!");
                    }
                }

                if (localComponent != null) {
                    log.debug("About to deliver message to " + localComponent.getComponentType());
                    localComponent.deliver(message);
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

    private String messageSummary(Message message) {
        StringBuilder buff = new StringBuilder();
        buff.append("id=").append(message.getMessageID()).append("; ");
        buff.append("prodCNI=").append(message.getHeader().getProducer().getComponentLocator().getComponentNodeId()).append("; ");;
        buff.append("prod=").append(message.getHeader().getProducer().getComponentType()).append("; ");
        buff.append("content_type=").append(message.getContentType()).append("; ");
        ComponentIdentifier consumer = message.getHeader().getConsumer();
        if (consumer != null) {
            buff.append("cons=").append(consumer.getComponentType()).append("; ");
        	if(message.getHeader().getConsumer().getComponentLocator() != null){
        		buff.append("consCNI=").append(message.getHeader().getConsumer().getComponentLocator().getComponentNodeId()).append("; ");
        	}
        	if(message.getHeader().getConsumer().getComponentLocator().getNodeAddress() != null){
        		buff.append("node=").append(message.getHeader().getConsumer().getComponentLocator().getNodeAddress().getValue());
        	}
        }
        return buff.toString();
    }
}
