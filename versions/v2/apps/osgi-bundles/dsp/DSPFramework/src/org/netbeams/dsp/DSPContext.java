package org.netbeams.dsp;

/**
 * DSP Context provides the mechanism by which DSP components can assess the DSP platform.
 */



public interface DSPContext {

	/**
	 * Provides the access to the MessageBroker. The access is implemented through a adapter pattern.
	 * 
	 * @return
	 * @throws DSPException
	 */
	public MessageBrokerAccessor getDataBroker() throws DSPException;
	
}
