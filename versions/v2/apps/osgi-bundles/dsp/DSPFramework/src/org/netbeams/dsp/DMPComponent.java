package org.netbeams.dsp;




public interface DMPComponent{	

	/**
	 * 
	 * @return
	 */
	public ComponentLocator getLocator();
	
	/**
	 * Provide the component's type. The type is the package name of the component.
	 * The package org.dmp.component.* is reserved.
	 * 
	 * This method is invoked by the platform. This type information is used to identify the
	 * component.
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * 
	 * @return
	 */
	public ComponentDescriptor getDescriptor();
		

	/**
	 * This method is invoked the platform before any data is sent to or received from the component.
	 * The component identification is provided by the platform. This information is persisted so when
	 * the component is restarted the same id is maintained. The persisted identificatio is lost when 
	 * component is uninstalled.
	 *  
	 * @param componentID
	 * @param context
	 * @throws DMPException
	 */
	public void initComponent(ComponentLocator locator, DMPContext context) throws DMPException;
	
	
	/**
	 * Method invoked by the data broker to delivery messages as result of a call to @see MessageBrokerAccessor.send(Message) 
	 * This methos is invoked asynchronously. The component implementaion should not block or take long to reply.
	 * 
	 * The message may require acknowlodgment (@see Message.).
	 * 
	 * @param message
	 * @throws DMPException
	 */
	public void messageDelivered(Message message) throws DMPException;
	
	/**
	 * Method invoked by the data broker to pull messages as result of a call to @see MessageBrokerAccessor#send() 
	 * The <code>request</code> is used by the invoking component to provide the target component with
	 * additional data so it can fullfil the request.
	 * 
	 * This is a synchronous call.
	 * 
	 * @param request Data that can help define which data can should be returned
	 * @return Requested data
	 * @throws DMPException
	 */

	public Message messageDeliveredWithReply(Message message) throws DMPException;

	
	/**
	 * Invoked by the platform to inform the component it should start its processes. This method 
	 * should not block. After the return of this method, the platform may send or receive messages. 
	 */
	public void startComponent()throws DMPException;
	
	/**
	 * Invoked by the platform to inform the component it MUST stop its processes. This method 
	 * should not block. No message will be send after this invocation. 
	 */
	public void stopComponent() throws DMPException;

	
}
