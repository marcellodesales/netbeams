package org.netbeams.dsp.registry;

import org.jdom.Element;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.Message;
import org.netbeams.dsp.MessageTypes;

public class RegisterJDOMConverter {

	public static Message generateRegisterMessage(ComponentLocator locator, MessageTypes producedMessageType, MessageTypes consumedMsgTypes){
		Element register = new Element("Register", RegisterXML.NAMESPACE_PREFIX, RegisterXML.NAMESPACE_URI);
		
		
		
		return null;
	}
	
}
