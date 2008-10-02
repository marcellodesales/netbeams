package org.netbeams.dsp.registry;

/**
 * Component resposble for keeping registration record of components.
 * 
 * 
 * @author Kleber Sales
 * 
 */
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DMPComponent;
import org.netbeams.dsp.DMPException;
import org.netbeams.dsp.MessageConsumer;
import org.netbeams.dsp.MessageProducer;
import org.netbeams.dsp.PlatformComponent;

public interface ComponentRegistry extends PlatformComponent, DMPComponent,
		MessageProducer, MessageConsumer {
			
	/**
	 * Invoked by the Platform. The implementatin should use this method to perform any initialization
	 */
	public void initComponent();
	
	/**
	 * When the component was initialized.
	 * 
	 * @return
	 */
	public Date whenInitialized();
	
	/**
	 * This methods are only invoked by platform component to make the client component implementation simpler.
	 * We may remove this in the future.
	 * 
	 * @param locator
	 * @param descriptor
	 * @throws DMPException
	 */
	public void register(ComponentLocator locator, ComponentDescriptor descriptor)
		throws DMPException;

	/**
	 * This methods are only invoked by platform component to make the client component implementation simpler.
	 * We may remove this in the future.
	 *
	 * @param uuid
	 * @throws DMPException
	 */
	public void unregister(UUID uuid)
	throws DMPException;
	
	/**
	 * This methods are only invoked by platform component to make the client component implementation simpler.
	 * We may remove this in the future.
	 * 
	 * It returns registered componensts since <code>lastSync<code>
	 *
	 * @return
	 */
	public Collection<ComponentRegister> registeredComponents(Date lastSync);

	/**
	 * This methods are only invoked by platform component to make the client component implementation simpler.
	 * We may remove this in the future.
	 * 
	 * It returns unregistered componensts since <code>lastSync<code>
	 *
	 * @return
	 */
	public Collection<ComponentRegister> unregisteredComponents(Date lastSync);
	
}
