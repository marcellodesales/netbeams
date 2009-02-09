/**
 * 
 */
package org.netbeams.dsp.platform;

/**
 * Platform is the runtime environment where the DSPComponents run.
 * 
 * #10/08/2008 - Kleber Sales - Creation
 *
 **/

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPContextFactory;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.platform.broker.MessageBroker;
import org.netbeams.dsp.platform.management.component.ComponentManager;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.osgi.framework.BundleContext;


public class Platform implements DSPContextFactory{
	
	private static final Logger log = Logger.getLogger(Platform.class);
	
	public static final String COMPONENT_TYPE = "org.dsp.platform";
	
	public static final String MATCHER_NODE_ID = "Marcher";

	private String DSP_HOME;
	
	private ComponentManager componentManager;
	private MessageBroker broker;
	private Matcher matcher;
	
	private PlatformDSPAccess platformDSPAccess;
	
	// TODO: For now, I do need multiple context as it only holds references.
	private DSPContextImpl context;
	
	private Object shutdownLock;
	
	public Platform(String home, BundleContext bundleContext){
		DSP_HOME = home;
		shutdownLock = new Object();
		context = new DSPContextImpl(bundleContext, home);
	}
		
	/**
	 * Invoked by the Running Environment before any other method.
	 */
	public void init(){
		log.info("init()");
		
		try {
			createMatcher();
		} catch (DSPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		createMessageBroker();
		context.setMessageBrokerAccessor(broker);
		
		try {
			createComponentManager();
		} catch (PlatformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		platformDSPAccess = new PlatformDSPAccess();
		
		log.info("init() completed");
	}
	
	/**
	 * Invoked by the Running Environment.
	 * 
	 * @throws DSPException
	 * @throws PlatformException
	 */
	void start() throws DSPException, PlatformException {
		log.info("start()");
		startPlatform();
		log.info("start() completed");
	}
	
	/**
	 * Invoked by the Running Environment.
	 */
	void stop()	{
		log.info("stop()");
		stopPlatform();
		log.info("stop() completed.");
	}
	
	/**
	 * @Override
	 */
	public DSPContext createContext() {
		return context;
	}

	public ComponentManager getComponentManager(){
		return componentManager;
	}
		
	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	/**
	 * @param Non-null Component Manager
	 * @throws PlatformException 
	 */
	private void createComponentManager() throws PlatformException{
		componentManager = new ComponentManager();
		componentManager.init(this, DSP_HOME);
		componentManager.setMessageBroker(broker);
	}

	private void createMessageBroker() {		
		broker = new MessageBroker(matcher);
	}
	
	private void startPlatform() throws PlatformException {
		try {
			broker.start();
		} catch (DSPException e) {
			// TODO Auto-generated catch block
			log.error("Could not start message broker", e);
			throw new PlatformException(e);
		}

		componentManager.start();	
	}

	private void stopPlatform() {
		try {
			broker.stop();
		} catch (DSPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			componentManager.stop();
		} catch (PlatformException e) {
			log.error("Error stopping Component Manager", e);
		}		
	}

	
	private void createMatcher() throws DSPException {
		matcher = new Matcher();
		matcher.initComponent(MATCHER_NODE_ID, context);
	}
	

	/////////////////////////////////
	////////// Inner Class //////////
	/////////////////////////////////
	
	/**
	 * PlatformDSPAccess allows Platform to access DSPComponents and be access by them.
	 * 
	 * This Class encapsulates the message exchange issues so we make the code in Platform class 
	 * only concerned with its coorination nature.
	 */

	protected class PlatformDSPAccess implements DSPComponent{

		DSPContext context;
		String componentNodeId;
		
		public PlatformDSPAccess(){		
		}
		
		/**
		 * Messages sent to the Platform.
		 * 
		 * @Override
		 */
		public void deliver(Message message) throws DSPException {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @Override
		 */
		public Message deliverWithReply(Message message) throws DSPException {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @Override
		 */
		public Message deliverWithReply(Message message, long waitTime)
				throws DSPException {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @Override
		 */
		public ComponentDescriptor getComponentDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @Override
		 */
		public void startComponent() throws DSPException {
			// So far, nothing to do...
			
		}

		/**
		 * @Override
		 */
		public void stopComponent() throws DSPException {
			// So far, nothing to do...
		}

		/**
		 * @Override
		 */
		public String getComponentType() {
			return COMPONENT_TYPE;
		}

		/**
		 * @Override
		 */
		public String getComponentNodeId() {
			return componentNodeId;
		}

		/**
		 * @Override
		 */
		public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
			this.componentNodeId = componentNodeId;
			this.context = context;
		}
		
		/**
		 * Message Broker Accessor interface
		 */
		
		public void send(Message data ) throws DSPException{
			context.getDataBroker().send(data);
		}
 
		public Message sendWaitForReply(Message request) throws DSPException{
			return context.getDataBroker().sendWaitForReply(request);
			
		}
		public Message sendWaitForReply(Message request, long waitTime) throws DSPException{
			return context.getDataBroker().sendWaitForReply(request, waitTime);		
		}
	}
	
}
