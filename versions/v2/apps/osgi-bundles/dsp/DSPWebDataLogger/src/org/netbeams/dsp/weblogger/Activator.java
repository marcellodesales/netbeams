package org.netbeams.dsp.weblogger;

import org.apache.log4j.Logger;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

    private static final Logger log = Logger.getLogger(Activator.class);

    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    private DataCollector consumer;

    HttpService httpService;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        log.debug("WebLogger.Activate.start()");

        this.bundleContext = bundleContext;

        ServiceReference servRef = this.bundleContext.getServiceReference(HttpService.class.getName());
        if (servRef == null) {
            log.error("The Http Service reference could not be retrieved from the OSGi platform!!!");
            throw new IllegalStateException("This DSP Component needs to have the HTTP service running!!!");
        }

        this.httpService = (HttpService) this.bundleContext.getService(servRef);
        if (httpService == null) {
            log.error("The Http Service could not be retrieved from the reference of the OSGi platform!!!");
            throw new IllegalStateException(
                    "The Http Service could not be retrieved from the reference of the OSGi platform!!!");
        }

        consumer = new DataCollector(httpService);
        serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, consumer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        log.debug("WebLogger.Activator.stop()");
        consumer.stopComponent();
        ActivatorHelper.unregisterOSGIService(bundleContext, serviceRegistration);
    }
}