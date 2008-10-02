package org.netbeams.dsp.network;

import org.netbeams.dsp.DMPException;

public interface Channel {

	public void send(byte[] data) throws DMPException;
	
	public void registerChannelListener(ChannelListener listener);
	
	public void unregisterChannelListener(ChannelListener listener);
}
