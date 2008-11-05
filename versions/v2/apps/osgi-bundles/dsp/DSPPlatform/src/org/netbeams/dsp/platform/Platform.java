/**
 * 
 */
package org.netbeams.dsp.platform;

/**
 * @author kleber
 *
 */

import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPContextFactory;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.platform.broker.MessageBroker;
import org.netbeams.dsp.platform.management.component.ComponentManager;
import org.netbeams.dsp.platform.matcher.Matcher;
import org.osgi.framework.BundleContext;


public class Platform implements DSPContextFactory{

	private String DSP_HOME;
	
	private ComponentManager componentManager;
	private MessageBroker broker;
	private Matcher matcher;
	
	
	// TODO: For now, I do need multiple context as it only holds references.
	private DSPContextImpl context;
	
	private Object shutdownLock;
	
	public Platform(String home, BundleContext bundleContext){
		DSP_HOME = home;
		shutdownLock = new Object();
		context = new DSPContextImpl(bundleContext);
	}
		
	/**
	 * Invoked by the Running Environment before any other method.
	 */
	public void init(){
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
	}
	
	/**
	 * Invoked by the Running Environment.
	 * 
	 * @throws DSPException
	 * @throws PlatformException
	 */
	void start() throws DSPException, PlatformException {
		startPlatform();
	}
	
	/**
	 * Invoked by the Running Environment.
	 */
	void stop()	{
		stopPlatform();
	}
	

	@Override
	public DSPContext createContext() {
		return context;
	}

	public ComponentManager getComponentManager(){
		return componentManager;
	}
		
	/////////////////////////////////////
	////////// Privage Section //////////
	/////////////////////////////////////
	
	/**
	 * @param Non-null Component Manager
	 * @throws PlatformException 
	 */
	private void createComponentManager() throws PlatformException{
		componentManager = new ComponentManager();
		componentManager.init(this);
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
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	
	private void createMatcher() throws DSPException {
		matcher = new Matcher(DSP_HOME);
		matcher.initComponent(null, context);
	}
	

}
