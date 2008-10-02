package org.netbeams.dsp;

import org.netbeams.dsp.broker.MessageBrokerAccessor;

public interface DMPContext {

	public MessageBrokerAccessor getDataBroker() throws DMPException;
	
}
