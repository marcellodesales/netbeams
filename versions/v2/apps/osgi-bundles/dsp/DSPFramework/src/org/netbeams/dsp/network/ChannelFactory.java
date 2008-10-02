package org.netbeams.dsp.network;

import org.netbeams.dsp.NodeAddress;

public interface ChannelFactory {

	public Channel createChannel(NodeAddress nodeAddress);
}
