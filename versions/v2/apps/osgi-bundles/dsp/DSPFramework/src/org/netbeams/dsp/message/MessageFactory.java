package org.netbeams.dsp.message;


import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.NodeAddress;
import org.netbeams.dsp.data.MessageContent;
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
	
		ComponentLocator locator = new ComponentLocator(producer.getUUID(), NodeAddress.LOCAL_ADDRESS);
		ComponentIdentifier indentifier = new ComponentIdentifier(producer.getComponentType(), locator);
		
		Message  message = createMessage(messageType, messageContent, indentifier);
		return message;
	}
		
	private static Message createMessage(Class<? extends Message> messageType,
			MessageContent messageContent, ComponentIdentifier componentIdentifier) 
		throws DSPException 
	{
		if(messageType.equals(ActionMessage.class)){
			return new ActionMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(CreateMessage.class)){
			return new CreateMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(DeleteMessage.class)){
			return new DeleteMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(EventMessage.class)){
			return new EventMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(InsertMessage.class)){
			return new InsertMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(QueryMessage.class)){
			return new QueryMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(UpdateMessage.class)){
			return new UpdateMessage(componentIdentifier, messageContent);
		}else if(messageType.equals(MeasurementMessage.class)){
			return new MeasurementMessage(componentIdentifier, messageContent);
		}else{
			throw new DSPException(ErrorCode.ERROR_INVALID_MESSAGE, "Invalide Message Type=" + messageType.getName());
		}
		
	}
}
