package org.netbeams.dsp;

public interface DSPPlatformComponent extends DSPComponent{
	
	
	/**
	 * Invoked by the platform to allow the imlementation to acquire any necessary resource. No message will be sent
	 * until <code>start</code> is invoked. 
	 */
	public void activate() throws DSPException;
	
	/**
	 * This method can be invoked by the platform ONLY after stop. The implementation should release any resource 
	 * acquired during <code>activate</code>
	 * 
	 * @throws DSPException
	 */
	public void deactivate() throws DSPException;

}
