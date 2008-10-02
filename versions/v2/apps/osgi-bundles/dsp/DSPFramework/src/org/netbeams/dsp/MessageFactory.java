package org.netbeams.dsp;


/**
 * Currently this factory create XML (JDOM) based message. In the future it would be possible to configure the 
 * a different encoding type.
 * 
 * @author kleber
 *
 */
public class MessageFactory {
	
	private MessageFactory() {}
	
	public static Message newMessage(DMPComponent component, Message.Category categorty, String type){
		
		return new MessageJDOMImp(categorty, type, component.getLocator());
	}
	
}
