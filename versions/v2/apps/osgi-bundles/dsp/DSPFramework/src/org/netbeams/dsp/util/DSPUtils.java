package org.netbeams.dsp.util;

import java.util.UUID;

import org.netbeams.dsp.ComponentIdentifier;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.NodeAddress;

public abstract class DSPUtils {
	
	public static ComponentIdentifier obtainIdentifier(String type, UUID uuid, NodeAddress nodeAddress){
		ComponentLocator locator = new ComponentLocator(uuid, NodeAddress.LOCAL_ADDRESS);
		return new ComponentIdentifier(type, locator);

	}
	
}
