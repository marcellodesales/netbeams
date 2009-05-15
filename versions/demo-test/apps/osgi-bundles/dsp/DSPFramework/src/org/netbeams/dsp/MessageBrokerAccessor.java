package org.netbeams.dsp;

import org.netbeams.dsp.message.Message;

public interface MessageBrokerAccessor {

	/**
	 * Push message to into the infrastructure. The message broker implementation is responsible for
	 * delivering the the messaget to the consumer.
	 * 
	 * @param request
	 * @return
	 * @throws DSPException
	 */

	public void send(Message data ) throws DSPException;
	
	/**
	 * Push message to into the infrastructure.
	 * 
	 * @param request
	 * @return
	 * @throws DSPException
	 */
	public Message sendWaitForReply(Message request) throws DSPException;

	/**
	 * 
	 * @param request
	 * @param waitTime
	 * @return
	 * @throws DSPException
	 */
	public Message sendWaitForReply(Message request, long waitTime) throws DSPException;

}
