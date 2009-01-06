package org.netbeams.dsp;


import java.util.List;
import java.util.UUID;

import org.netbeams.dsp.message.*;
import org.netbeams.dsp.util.ErrorCode;


/**
 * Currently this factory create XML (JDOM) based message. In the future it would be possible to configure the 
 * a different encoding type.
 * 
 * @author kleber
 *
 */
public class MessageFactory {
	
	private MessageFactory() {}
	
	public static Message newMessage(Class<? extends Message> messageType, MessageContent messageContent, BaseComponent producer)
		throws DSPException
	{
	
		ComponentLocator locator = new ComponentLocator();
		locator.setComponentNodeId(producer.getComponentNodeId());
		locator.setNodeAddress(NodeAddressHelper.LOCAL_NODEADDRESS);
		
		ComponentIdentifier indentifier = new ComponentIdentifier();
		indentifier.setComponentType(producer.getComponentType());
		indentifier.setComponentLocator(locator);
				
		Message  message = createMessage(messageType, messageContent, indentifier);
		return message;
	}


	private static Message createMessage(Class<? extends Message> messageType,
			MessageContent messageContent, ComponentIdentifier producer) 
		throws DSPException 
	{
		Message message = null;
		
		if(messageType.equals(ActionMessage.class)){
			message = new ObjectFactory().createActionMessage();
		}else if(messageType.equals(CreateMessage.class)){
			message = new ObjectFactory().createCreateMessage();
		}else if(messageType.equals(DeleteMessage.class)){
			message = new ObjectFactory().createDeleteMessage();
		}else if(messageType.equals(EventMessage.class)){
			message = new ObjectFactory().createEventMessage();
		}else if(messageType.equals(InsertMessage.class)){
			message = new ObjectFactory().createInsertMessage();
		}else if(messageType.equals(QueryMessage.class)){
			message = new ObjectFactory().createQueryMessage();
		}else if(messageType.equals(UpdateMessage.class)){
			message = new ObjectFactory().createUpdateMessage();
		}else if(messageType.equals(MeasureMessage.class)){
			message = new ObjectFactory().createMeasureMessage();
		}else{
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
	
	/**
	 * @param messages
	 * @return a new instance of Messages Container to be sent by the broker.
	 */
	public static MessagesContainer createMessagesContainer(List<Message> messages) {
		MessagesContainer container = new ObjectFactory().createMessagesContainer();
		container.getMessage().addAll(messages);
		return container;
	}
}
