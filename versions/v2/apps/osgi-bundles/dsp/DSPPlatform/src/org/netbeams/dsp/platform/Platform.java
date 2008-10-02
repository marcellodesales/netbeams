/**
 * 
 */
package org.netbeams.dsp.platform;

/**
 * @author kleber
 *
 */

import org.netbeams.dsp.DMPContext;
import org.netbeams.dsp.DMPContextFactory;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.broker.MessageBroker;
import org.netbeams.dsp.management.component.ComponentManager;
import org.netbeams.dsp.match.Matcher;
import org.netbeams.dsp.registry.ComponentRegistry;
import org.netbeams.dsp.util.Log;


public class Platform implements DMPContextFactory{

	private ComponentManager componentManager;
	
	// Platforma components
	private MessageBroker broker;
	private Matcher matcher;
	private ComponentRegistry registry;
	
	// TODO: For now, I do need multiple context as it only holds references.
	private DMPContextImpl context;
	
	private Object shutdownLock;
	
	public Platform(){
		shutdownLock = new Object();
		context = new DMPContextImpl();
	}
	
	/**
	 * Invoked by the Running Environment before any other method.
	 */
	public void init(){
		createComponentManager();	
	}
	
	
	/**
	 * Invoked by the Running Environment.
	 * 
	 * @throws DMPException
	 * @throws PlatformException
	 */
	void start() throws DMPException, PlatformException {
	}
	
	/**
	 * Invoked by the Running Environment.
	 */
	void stop()
	{
		componentManager.stopDMPComponents();
	}

	
	@Override
	public DMPContext createContext() {
		return context;
	}

	/**
	 * Dependency Injection. Due its dynamic nature it is possible that the Running Environment
	 * invokes this method mutiple times during the life-cycle of the Component Manager depending on 
	 * Component Manager Availability. Implementataions MUST handle this fact accordingly.
	 * 
	 * @param newBroker
	 * @throws PlatformException
	 */
	public void setMessageBroker(MessageBroker newBroker) throws PlatformException{
		Log.log("Platform.setMessageBroker(): " + (newBroker != null));
		
		if(newBroker != null){
			assert(broker == null);
			// Fullfil dependency when possible
			if(componentManager != null){
				newBroker.setComponentManger(componentManager);
			}
			if(matcher != null){
				newBroker.setMatcher(matcher);
			}
			// TODO: Run Life-Cycle before wiring
			try {
				newBroker.initComponent();
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				newBroker.startComponent();
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			broker = newBroker;
			context.setMessageBrokerAccessor(broker.getMessageBrokerAccessor());
		}else{
			assert(broker != null);
			// The Component Registry is unavailable
			MessageBroker previousBroker = broker;
			broker = null;
			// Inform dependents components ASAP
			context.setMessageBrokerAccessor(null);
			// TODO: Run Life-Cycle
			try {
				previousBroker.stopComponent();
			} catch (DMPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}

	/**
	 * Dependency Injection. Due its dynamic nature it is possible that the Running Environment
	 * invokes this method mutiple times during the life-cycle of the Component Manager depending on 
	 * Component Manager Availability. Implementataions MUST handle this fact accordingly.
     *
	 * @param newRegistry
	 * @throws PlatformException
	 */
	public void setComponentRegistry(ComponentRegistry newRegistry) throws PlatformException{		
		Log.log("Platform.setComponentRegistry(): " + (newRegistry != null));
		
		if(newRegistry != null && this.registry != null){
			throw new PlatformException("Component Registry already bound.");
		}
		
		if(newRegistry != null){
			assert(registry == null);
			if(matcher != null){
				assert(matcher.getComponentRegistry() == null);
			}
			if(componentManager != null){
				assert(componentManager.getComponentRegistry() == null);
			}

			// TODO: Run Life-Cycle before wiring
			newRegistry.initComponent();
			newRegistry.startComponent();
			registry = newRegistry;
			// Wire
			if(matcher != null){
				matcher.setComponentRegistry(registry);
			}
			if(componentManager != null){
				componentManager.setComponentRegistry(registry);
			}
		}else{
			assert(registry != null);
			if(matcher != null){
				assert(matcher.getComponentRegistry() != null);
			}
			if(componentManager != null){
				assert(componentManager.getComponentRegistry() != null);
			}

			// The Component Registry is unavailable
			ComponentRegistry previousRegistry = registry;
			registry = null;
			// Wire
			if(matcher != null){
				matcher.setComponentRegistry(null);
			}
			if(componentManager != null){
				componentManager.setComponentRegistry(null);
			}
			// TODO: Run Life-Cycle
			previousRegistry.stopComponent();
		}
	}
	
	/**
	 * Dependency Injection. Due its dynamic nature it is possible that the Running Environment
	 * invokes this method mutiple times during the life-cycle of the Component Manager depending on 
	 * Component Manager Availability. Implementataions MUST handle this fact accordingly.
	 * 
	 * @param newMatcher
	 * @throws PlatformException
	 * @throws DMPException 
	 */
	public void setMatcher(Matcher newMatcher) throws PlatformException, DMPException{
		Log.log("Platform.setMatcher(): " + (newMatcher != null));
		
		if(newMatcher != null){
			assert(matcher == null);
			if(broker != null){
				assert(broker.getMatcher() == null);
			}
			// Fullfil dependency when possible
			if(registry != null){
				newMatcher.setComponentRegistry(registry);
			}
			// TODO: Run Life-Cycle before wiring
			newMatcher.initComponent();
			newMatcher.startComponent();
			matcher = newMatcher;
			if(broker != null){
				broker.setMatcher(matcher);
			}
		}else{
			assert(matcher != null);
			if(broker != null){
				assert(broker.getMatcher() == null);
			}
			// The Component Registry is unavailable
			Matcher previousMatcher = matcher;
			matcher = null;
			// This extra comparison is here just to make the code easier to understand. No impact in 
			// performance.
			if(broker != null){
				broker.setMatcher(null);
			}
			// TODO: Run Life-Cycle
			previousMatcher.stopComponent();
		}

	}
	
	public ComponentManager getComponentManager(){
		return componentManager;
	}
		
	///////////////////////
	/// Privage Section ///
	///////////////////////
	
	/**
	 * @param Non-null Component Manager
	 */
	private void createComponentManager(){
		componentManager = new ComponentManagerImpl();
		// TODO: No need to be a external component. Move this to the constructor
		try {
			componentManager.init(this);
		} catch (DMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
