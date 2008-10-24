package org.netbeams.dsp;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NodeAddress {
	
	// TODO Decide the node address structure
	public static NodeAddress LOCAL_ADDRESS; 
	public static NodeAddress NO_ADDRESS = new NodeAddress("no-address"); 
	
	static{
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String localHostName = localHost.getHostName();
			LOCAL_ADDRESS = new NodeAddress(localHostName);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String address;
	
	public NodeAddress(String address){
		this.address = address;
	}
	
	public String getAddress() { return this.address; }
	
	public int hashCode(){
		return address.hashCode();
	}
	
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj instanceof NodeAddress){
			NodeAddress o = (NodeAddress)obj;
			return address.equals(o.getAddress());
		}
		return false;
	}

}
