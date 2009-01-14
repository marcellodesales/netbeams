package org.netbeams.dsp.messagesdirectory.model;

/**
 * MessagesQueueState is designed to identify the state of the messages queue.
 * 
 * @author Marcello de Sales
 * Jan 10, 2009 13:05
 *
 */
public enum MessagesQueueState {
	
	/**
	 * Whenever a messages queue is created to be transmitted;
	 */
	QUEUED, 
	/**
	 * Whenever a messages queue has been transmitted to a destination. 
	 */
	TRANSMITTED, 
	/**
	 * Whenever a messages queue has been acknowledged to be received by the destination.
	 */
	ACKNOWLEDGED;
		
}