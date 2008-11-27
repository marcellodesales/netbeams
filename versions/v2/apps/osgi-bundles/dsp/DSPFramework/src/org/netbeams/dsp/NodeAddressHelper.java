package org.netbeams.dsp;

import org.netbeams.dsp.message.NodeAddress;

public class NodeAddressHelper {

	public static final NodeAddress LOCAL_NODEADDRESS = new NodeAddress();
	static {
		LOCAL_NODEADDRESS.setValue("LOCAL");
	}

	public static final NodeAddress NO_ADDRESS = new NodeAddress();
	static {
		LOCAL_NODEADDRESS.setValue("no-address");
	}
	
	
}
