package org.netbeams.dsp;

/**
 * This class provides information to allow the Platform to find a component. For consitency reason, the same infrastructure
 * is used to identify Nodes. In this case the UUID is null.
 */

import java.util.UUID;

public class ComponentLocator {

	private UUID _uuid;
	private NodeAddress nodeAddress;
	
	public ComponentLocator(UUID uuid, NodeAddress nodeAddress){
		_uuid = uuid;
		this.nodeAddress = nodeAddress;
	}
	
	public UUID getUUID(){
		return _uuid;
	}
	
	public NodeAddress getNodeAddress(){
		return this.nodeAddress;
	}

	public boolean isLocal(){
		// TODO: Assume all component are local.
		return true;
	}
}
