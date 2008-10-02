package org.netbeams.dsp.broker;

import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.match.Matcher;

public interface MessageBroker {

	/**
	 * Dependency injection. Invoke by the platform implementation.
	 * 
	 * @param componentManager ComponentManager
	 */
	public void setComponentManger(ComponentManager componentManager);
	public ComponentManager getComponentManger();

	/**
	 * Dependency injection. Invoke by the platform implementation.
	 * 
	 * @param matcher Matcher
	 */
	public void setMatcher(Matcher matcher);
	public Matcher getMatcher();
	
	/**
	 * Invoked by the platform to allow the imlementation to acquire any cecessary resource. No message will be sent
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
	
	/**
	 * This methos is guaranteed be invoked after init(). The implementation MUST not block. It should return 
	 * as soon as possible.
	 * 
	 * @throws DMPException
	 */
	public void start() throws DMPException;
	
	public void stop() throws DMPException;
	
	/**
	 * @return MessageBrokerAccessor implementation which will be used by DSP components to send messages
	 */
	public MessageBrokerAccessor getMessageBrokerAccessor();
}
