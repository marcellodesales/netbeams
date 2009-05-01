package org.netbeams.dsp;

import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.util.NetworkUtil;

public class NodeAddressHelper {
	
	public static String localAddressStr;
	
	public static final NodeAddress LOCAL_NODEADDRESS = new NodeAddress();
	static {
		LOCAL_NODEADDRESS.setValue("LOCAL");
	}
	
	public static final NodeAddress REMOTE_ADDRESS = new NodeAddress();
	static {
		REMOTE_ADDRESS.setValue("REMOTE");
	}
	
	public static final NodeAddress NONE_ADDRESS = new NodeAddress();
	static {
		NONE_ADDRESS.setValue("NONE");
	}		

	public static final NodeAddress ANY_ADDRESS = new NodeAddress();
	static {
		ANY_ADDRESS.setValue("ANY");
	}		
	
	
	public static final NodeAddress SAME_ADDRESS = new NodeAddress();
	static {
		SAME_ADDRESS.setValue("SAME");
	}	
	
	public static boolean isLocal(String address){
		return "LOCAL".equalsIgnoreCase(address) || "LOCALHOST".equalsIgnoreCase(address) ||
    	"127.0.0.1".equals(address) || address.equalsIgnoreCase(NetworkUtil.getCurrentEnvironmentNetworkIp());
	}
}
