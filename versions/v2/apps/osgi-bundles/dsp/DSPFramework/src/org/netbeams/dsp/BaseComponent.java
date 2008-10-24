package org.netbeams.dsp;

import java.util.UUID;

public interface BaseComponent {
	/**
	 * Provide the information about the component's localization and indetification 
	 * 
	 * @return
	 */
	public UUID getUUID();
	
	/**
	 * Provide the component's type. The type is the package name of the component.
	 * The package org.dsp.component.* is reserved.
	 * 
	 * This method is invoked by the platform. This type information is used to identify the
	 * component.
	 * 
	 * @return
	 */
	public String getComponentType();
	
	/**
	 * This method is invoked the platform before any data is sent to or received from the component.
	 * The component identification is provided by the platform. This information is persisted so when
	 * the component is restarted the same id is maintained. The persisted identificatio is lost when 
	 * component is uninstalled.
	 *  
	 * @param componentID
	 * @param context
	 * @throws DSPException
	 */
	public void initComponent(UUID uuid, DSPContext context) throws DSPException;


}
