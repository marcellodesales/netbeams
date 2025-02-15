package org.netbeams.dsp;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.message.ActionMessage;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.CreateMessage;
import org.netbeams.dsp.message.DeleteMessage;
import org.netbeams.dsp.message.EventMessage;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.InsertMessage;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.ErrorCode;
import org.netbeams.dsp.util.NetworkUtil;

/**
 * Currently this factory create XML (JDOM) based message. In the future it would be possible to configure the a
 * different encoding type.
 * 
 * @author kleber
 * 
 */
public class MessageFactory {

    private static final Logger log = Logger.getLogger(MessageFactory.class);

    private MessageFactory() {
    }

    public static Message newMessage(Class<? extends Message> messageType, MessageContent messageContent,
            BaseComponent producer) throws DSPException {
        return newMessage(messageType, messageContent, producer, null, null);
    }

    public static Message newMessage(Class<? extends Message> messageType, MessageContent messageContent,
            BaseComponent producer, String consumerNodeId, String consumerNodeAddress) throws DSPException {

        ComponentLocator locator = new ComponentLocator();
        locator.setComponentNodeId(producer.getComponentNodeId());
        locator.setNodeAddress(NodeAddressHelper.LOCAL_NODEADDRESS);

        ComponentIdentifier indentifier = new ComponentIdentifier();
        indentifier.setComponentType(producer.getComponentType());
        indentifier.setComponentLocator(locator);

        Message message = createMessage(messageType, messageContent, indentifier);
        return message;
    }

    public static Message newMessage2(Class<? extends Message> messageType, MessageContent messageContent,
            BaseComponent producer, String consumerNodeId, String componentType, String consumerNodeAddress)
            throws DSPException {
        ComponentIdentifier ciProducer = createComponentIdentifier(producer.getComponentNodeId(), producer
                .getComponentType(), NetworkUtil.getCurrentEnvironmentNetworkIp());

        Message message = createMessageInternal(messageType, ciProducer);

        ComponentIdentifier ciConsumer = createComponentIdentifier(consumerNodeId, componentType, consumerNodeAddress);
        setConsumer(message, ciConsumer);

//        Node n = marshallMessageContent(producer, messageContent);
        Body body = new Body();
        body.setAny(messageContent);
        message.setBody(body);

        return message;
    }

    private static Message createMessage(Class<? extends Message> messageType, MessageContent messageContent,
            ComponentIdentifier producer) throws DSPException {
        Message message = null;

        if (messageType.equals(ActionMessage.class)) {
            message = new ActionMessage();
        } else if (messageType.equals(CreateMessage.class)) {
            message = new CreateMessage();
        } else if (messageType.equals(DeleteMessage.class)) {
            message = new DeleteMessage();
        } else if (messageType.equals(EventMessage.class)) {
            message = new EventMessage();
        } else if (messageType.equals(InsertMessage.class)) {
            message = new InsertMessage();
        } else if (messageType.equals(QueryMessage.class)) {
            message = new QueryMessage();
        } else if (messageType.equals(UpdateMessage.class)) {
            message = new UpdateMessage();
        } else if (messageType.equals(MeasureMessage.class)) {
            message = new MeasureMessage();
        } else {
            throw new DSPException(ErrorCode.ERROR_INVALID_MESSAGE, "Invalide Message Type=" + messageType.getName());
        }

        // Create Header
        Header header = new Header();
        header.setProducer(producer);
        // Create Body
        Body body = new Body();
        body.setAny(messageContent);
        // Set up Message
        message.setMessageID(UUID.randomUUID().toString());
        message.setContentType(messageContent.getContentType());
        message.setHeader(header);
        message.setBody(body);

        return message;
    }

    private static Message createMessageInternal(Class<? extends Message> messageType, ComponentIdentifier producer)
            throws DSPException {
        Message message = null;

        if (messageType.equals(ActionMessage.class)) {
            message = new ActionMessage();
        } else if (messageType.equals(CreateMessage.class)) {
            message = new CreateMessage();
        } else if (messageType.equals(DeleteMessage.class)) {
            message = new DeleteMessage();
        } else if (messageType.equals(EventMessage.class)) {
            message = new EventMessage();
        } else if (messageType.equals(InsertMessage.class)) {
            message = new InsertMessage();
        } else if (messageType.equals(QueryMessage.class)) {
            message = new QueryMessage();
        } else if (messageType.equals(UpdateMessage.class)) {
            message = new UpdateMessage();
        } else if (messageType.equals(MeasureMessage.class)) {
            message = new MeasureMessage();
        } else {
            throw new DSPException(ErrorCode.ERROR_INVALID_MESSAGE, "Invalide Message Type=" + messageType.getName());
        }

        // Create Header
        Header header = new Header();
        header.setProducer(producer);
        // Create Body
        Body body = new Body();
        // Set up Message
        message.setMessageID(UUID.randomUUID().toString());
        message.setHeader(header);
        message.setBody(body);

        return message;
    }

    /**
     * @param messages
     * @return a new instance of Messages Container to be sent by the broker.
     */
    public static MessagesContainer createMessagesContainer(List<Message> messages) {
        MessagesContainer container = new MessagesContainer();
        container.getMessage().addAll(messages);
        return container;
    }

    private static ComponentIdentifier createComponentIdentifier(String componentNodeId, String componentType,
            String nodeAddress) throws DSPException {
        ComponentLocator locator = new ComponentLocator();
        locator.setComponentNodeId(componentNodeId);
        if (nodeAddress != null) {
            locator.setNodeAddress(new NodeAddress(nodeAddress));
        } else {
            locator.setNodeAddress(new NodeAddress(NetworkUtil.getCurrentEnvironmentNetworkIp()));
        }

        ComponentIdentifier indentifier = new ComponentIdentifier();
        indentifier.setComponentType(componentType);
        indentifier.setComponentLocator(locator);
        return indentifier;
    }

    private static void setConsumer(Message message, ComponentIdentifier ciConsumer) {
        Header h = message.getHeader();
        h.setConsumer(ciConsumer);
    }

}
