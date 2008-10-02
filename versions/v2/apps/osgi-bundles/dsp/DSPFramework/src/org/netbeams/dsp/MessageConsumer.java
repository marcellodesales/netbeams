package org.netbeams.dsp;

/**
 * 
 */


public interface MessageConsumer {

	/**
	 * Method invoked by the data broker to push data from other components
	 * 
	 * @param data
	 * @throws DMPException
	 */
	public void push(Message msg) throws DMPException;
	

}
