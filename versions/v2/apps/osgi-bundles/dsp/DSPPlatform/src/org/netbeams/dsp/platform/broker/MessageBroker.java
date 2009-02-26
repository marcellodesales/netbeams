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

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.netbeams.dsp.util.DSPUtils;
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

        log.info("send() message id : " + message.getMessageID());
        if (log.isDebugEnabled()) {
            log.debug("message summary: " + messageSummary(message));
        }

        Collection<ComponentIdentifier> consumers = obtainConsumers(message);

        // Deliver messages
        if (!consumers.isEmpty()) {
            for (ComponentIdentifier consumer : consumers) {
                // We can deliberately ignore the message
                if (GlobalComponentTypeName.NO_COMPONENT.equals(consumer.getComponentType())) {
                    continue;
                }

                if (this.isMessageForLocalConsumer(consumer)) {
                    log.debug("Attempting to delivering message locally...");
                    DSPComponent localComponent = obtainDSPComponent(consumer);
                    if (localComponent != null) {
                        localComponent.deliver(message);
                    } else {
                        log.warn("$$ Requered dsp component " + DSPUtils.toString(consumer) + " not attached!!!");
                    }

                } else {
                    //Updating the header of the message for this consumer from the rule...
                    message.getHeader().setConsumer(consumer);
                    log.debug("Delivering message to the default Wire Transport client...");
                    log.debug("The header of the message was updated with the consumer information from the matcher...");
                    DSPComponent transportComponent = this.obtainDSPDefaultWireTransport();
                    if (transportComponent != null) {
                        transportComponent.deliver(message);
                    } else {
                        
                    }
                }
            }
        } else {
            log.warn("No consumers found for message");
        }
    }

    /**
     * @return the name of the default wire transport client set on the bootstrap.
     */
    private DSPComponent obtainDSPDefaultWireTransport() {
        String defaultWireTransport = System.getProperty("DEFAULT_WIRE_TRANSPORT_CLIENT");
//        if (defaultWireTransport == null) {
//            log.error("MessageBroker needs a bootstrap property message: DEFAULT_WIRE_TRANSPORT_CLIENT.");
//            log.error("Please set this constant with the name of the default Wire Transport Component.");
//            log.error("This should reflect the name of a given component type (wire transport client default).");
//            throw new IllegalStateException("MessageBroker needs a bootstrap property message: "
//                    + "DEFAULT_WIRE_TRANSPORT_CLIENT.");
//        }
        log.debug("Default wire transport client: org.netbeams.dsp.wiretransport.client");
        return componentsByType.get("org.netbeams.dsp.wiretransport.client");
    }

    /**
     * @param consumer is the identification of the consumer.
     * @return true if the message is to be consumed locally. That is, if the consumer IP address is any of
     * LOCAL, LOCALHOST, 127.0.0.1, or has the same IP address from the current running machine.
     */
    private boolean isMessageForLocalConsumer(ComponentIdentifier consumer) {
        String destIp = consumer.getComponentLocator().getNodeAddress().getValue();
        return destIp.equals(NetworkUtil.getCurrentEnvironmentNetworkIp()) || "LOCALHOST".equalsIgnoreCase(destIp)
                || "LOCAL".equalsIgnoreCase(destIp) || "127.0.0.1".equals(destIp);
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

    private Set<ComponentIdentifier> obtainConsumers(Message message) {
        Set<ComponentIdentifier> result = new HashSet<ComponentIdentifier>();

        ComponentIdentifier consumer = message.getHeader().getConsumer(); // Optional
        // If there are not target consumers, then try to find a match
        if (consumer != null) {
            log.debug("Message " + message.getContentType() + " ID " + message.getMessageID()
                    + " has pre-defined consumer");
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
        // Let's try the UUID first
        ComponentLocator locator = consumer.getComponentLocator();
        if (locator != null) {
            String comonentNodeId = locator.getComponentNodeId();
            if (comonentNodeId != null) {
                return components.get(comonentNodeId);
            } else {
                return componentsByType.get(consumer.getComponentType());
            }
        }
        String type = consumer.getComponentType();
        return componentsByType.get(type);
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
