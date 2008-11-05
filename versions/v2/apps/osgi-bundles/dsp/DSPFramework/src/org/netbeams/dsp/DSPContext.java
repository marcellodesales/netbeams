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
	

	/**
	 * Returns a resource from the underlying DSP implementation. If the
	 * underlying implementation is OSGi, this will allow access to OSGi
	 * related resources.
	 * 
	 * @param resource Description of the resource.
	 * @return Reference to the resource, null if the resource could not be located.
	 */
	public Object getResource(String resource);
	
}
