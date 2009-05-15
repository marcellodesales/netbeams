package org.netbeams.dsp.util;

import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.AbstractNodeAddress;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.NodeAddress;

public abstract class DSPUtils {
	
	public static ComponentIdentifier obtainIdentifier(String componentType, String componentNodeId, NodeAddress nodeAddress){
		ComponentLocator locator = new ComponentLocator();
		locator.setComponentNodeId(componentNodeId);
		locator.setNodeAddress(NodeAddressHelper.LOCAL_NODEADDRESS);
		
		ComponentIdentifier indentifier = new ComponentIdentifier();
		indentifier.setComponentType(componentType);
		indentifier.setComponentLocator(locator);
		
		return indentifier;
	}
	
	public static String toString(ComponentIdentifier identifier){
		StringBuilder builder = new StringBuilder();
		
		ComponentLocator locator = identifier.getComponentLocator();
		builder.append("id=").append(locator.getComponentNodeId()).append(';');
		AbstractNodeAddress address = locator.getNodeAddress();
		String node = (address != null)? address.getValue() : "";
		builder.append("node=").append(node).append(';');
		builder.append("type=").append(identifier.getComponentType()).append(';');
		
		return builder.toString();
	}
}
