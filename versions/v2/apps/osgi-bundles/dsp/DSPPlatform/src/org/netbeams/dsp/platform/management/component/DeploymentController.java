package org.netbeams.dsp.platform.management.component;

import org.netbeams.dsp.platform.PlatformException;

public interface DeploymentController {

	public void setComponentManager(ComponentManager componentManager);
	
	public void start() throws PlatformException;
	
	public void stop() throws PlatformException;

}
