/*
 * Created on Sat Oct 18 21:26:01 PDT 2008
 */

package org.netbeams.dsp.persistence.osgi;

import org.apache.log4j.Logger;
import org.netbeams.dsp.persistence.controller.DSPDataPersistence;
import org.netbeams.dsp.platform.osgi.ActivatorHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The DataPersistenceActivator is responsible for the activator for the
 * Persistence.
 * @author Marcello de Sales (March 29, 2009)
 */
public class DataPersistenceActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(DataPersistenceActivator.class);

    /**
     * Bundle context
     */
    private BundleContext bundleContext;
    /**
     * The service registration instance
     */
    private ServiceRegistration serviceRegistration;
    /**
     * The reference to the Wire Transport client
     */
    private DSPDataPersistence persistence;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        log.info("DataPersistence.Activator.start()");
        this.bundleContext = bc;
        this.persistence = new DSPDataPersistence();
        this.serviceRegistration = ActivatorHelper.registerOSGIService(bundleContext, this.persistence);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        log.info("DataPersistence.Activator.stop()");
        this.persistence.stopComponent();
        ActivatorHelper.unregisterOSGIService(this.bundleContext, this.serviceRegistration);
    }
}