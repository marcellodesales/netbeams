package org.netbeams.dsp.xml;

import java.util.UUID;

import org.jdom.Element;
import org.jdom.Namespace;
import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.MessageXMLConstant;
import org.netbeams.dsp.NodeAddress;

public class Converter {
	
	/**
	 * Obtain a component locator from XML
	 * @param elem
	 * @return
	 */
	public static ComponentIdentifier toComponentLocator(Element locator, Namespace namespace){
		Element eComponentId = locator.getChild(MessageXMLConstant.ELEMENT_COMPONENT_ID, namespace);
		Element eNodeAddress = locator.getChild(MessageXMLConstant.ELEMENT_NODE_ADDRESS, namespace);
		UUID uuid = UUID.fromString(eComponentId.getTextTrim());
		NodeAddress na = new NodeAddress(eNodeAddress.getTextTrim());
		
		return new ComponentLocator(uuid, na);
	}

}
