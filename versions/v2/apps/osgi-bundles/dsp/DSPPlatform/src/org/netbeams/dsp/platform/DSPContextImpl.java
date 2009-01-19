package org.netbeams.dsp.platform;

import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

class DSPContextImpl implements DSPContext {
	
	private MessageBrokerAccessor _msgBrokerAccessor;
	private BundleContext _bundleContext;
	private String _homePlatformDirectoryPath;
	
	public DSPContextImpl(BundleContext bundleContext, String homePlatformDirectoryPath) {
	    _bundleContext = bundleContext;
	    _homePlatformDirectoryPath = homePlatformDirectoryPath;
		_msgBrokerAccessor = null;
	}
	
	public synchronized void setMessageBrokerAccessor(MessageBrokerAccessor msgBrokerAccessor){
		_msgBrokerAccessor = msgBrokerAccessor;
	}
	
	/**
	 * @Override
	 */
	public synchronized MessageBrokerAccessor getDataBroker() throws DSPException {
		return _msgBrokerAccessor;
	}

	/**
	 * @Override
	 */
    public Object getResource(String resource)
    {
        if (resource.equals("osgi:HttpService")) {
            ServiceReference reference = _bundleContext.getServiceReference(HttpService.class.getName());
            return _bundleContext.getService(reference);
        }
        return null;
    }

	/**
	 * @Override
	 */
	public String getHomePlatformDirectoryPath() {
		return _homePlatformDirectoryPath;
	}

}
