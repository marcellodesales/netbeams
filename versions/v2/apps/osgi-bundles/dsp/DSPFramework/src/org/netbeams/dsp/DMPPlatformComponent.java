package org.netbeams.dsp;

public interface DMPPlatformComponent extends DMPComponent{
		
	/**
	 * Invoked by the platform to allow the imlementation to acquire any necessary resource. No message will be sent
	 * until <code>start</code> is invoked. 
	 */
	public void activate() throws DMPException;
	
	/**
	 * This method can be invoked by the platform ONLY after stop. The implementation should release any resource 
	 * acquired during <code>activate</code>
	 * 
	 * @throws DMPException
	 */
	public void deactivate() throws DMPException;

}
