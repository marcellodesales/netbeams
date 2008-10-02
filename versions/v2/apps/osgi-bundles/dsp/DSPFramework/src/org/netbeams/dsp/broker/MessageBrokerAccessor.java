package org.netbeams.dsp.broker;

import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.Message;

public interface MessageBrokerAccessor {

	/**
	 * Push message to into the infrastructure. The message broker implementation is responsible for
	 * delivering the the messaget to the consumer.
	 * 
	 * @param request
	 * @return
	 * @throws DMPException
	 */

	public void send(Message data ) throws DMPException;
	
	/**
	 * Push message to into the infrastructure.
	 * 
	 * @param request
	 * @return
	 * @throws DMPException
	 */
	public Message sendWaitForReply(Message request) throws DMPException;

	/**
	 * 
	 * @param request
	 * @param waitTime
	 * @return
	 * @throws DMPException
	 */
	public Message sendWaitForReply(Message request, long waitTime) throws DMPException;

	public void push(Message message);

}
