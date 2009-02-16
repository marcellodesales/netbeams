package org.netbeams.dsp.weblogger;



import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class DataCollector
    implements DSPComponent
{
    private static final Logger log = Logger.getLogger(DataCollector.class);

    // Component Type
    public final static String         COMPONENT_TYPE = "org.netbeams.dsp.weblogger";

    private static ComponentDescriptor componentDescriptor;


    private String                     componentNodeId;

    private DSPContext                 context;
    private ComponentLocator           locator;
    private ComponentDescriptor        descriptor;

    private Buffer                     theBuffer;
    private HttpService                httpService;

    /**
     * Creates a new DataCollector with the given reference to the HTTP service
     * @param httpServ is the HttpService retrieved by the Activator
     */
    public DataCollector(HttpService httpServ) {
        log.debug("Initializing the Data Collector with the HTTP instance " + httpServ);
        this.httpService = httpServ;
    }

    // ///////////////////////////////////////////
    // //////// DSP Component Interface //////////
    // ///////////////////////////////////////////


    //@Override
    public String getComponentNodeId()
    {
        // TODO Auto-generated method stub
        return null;
    }



    //@Override
    public String getComponentType()
    {
        return COMPONENT_TYPE;
    }



    //@Override
    public void initComponent(String componentNodeId, DSPContext context)
        throws DSPException
    {
        log.debug("DataCollector.initComponent()");
        this.context = context;
        this.componentNodeId = componentNodeId;
    }



    //@Override
    public ComponentDescriptor getComponentDescriptor()
    {
        return componentDescriptor;
    }



    //@Override
    public void startComponent()
    {
        log.debug("DataCollector.startComponent()");
        theBuffer = new Buffer();
        
        try {
            log.info("Registering Data Collection Servlets...");
            log.info("Receiving data at " + GetDataServlet.BASE_URI);
            log.info("Viewing data at " + WebAppServlet.BASE_URI);
            
            httpService.registerServlet(GetDataServlet.BASE_URI,
                    new GetDataServlet(theBuffer), null, null);
            httpService.registerServlet(WebAppServlet.BASE_URI,
                    new WebAppServlet(), null, null);
        }
        catch (ServletException e) {
            log.error(e.getMessage(), e);
        }
        catch (NamespaceException e) {
            log.error(e.getMessage(), e);
        }
    }



    //@Override
    public void stopComponent()
    {
        log.debug("DataCollector.stopComponent()");
        log.debug("Unregistering Servlets...");
        httpService.unregister(GetDataServlet.BASE_URI);
        httpService.unregister(WebAppServlet.BASE_URI);
    }



    //@Override
    public void deliver(Message message)
        throws DSPException
    {
        log.debug("DataCollector.deliver(): delivering message to the data collector...");
        processMessage(message);
    }



    //@Override
    public Message deliverWithReply(Message request)
        throws DSPException
    {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
        return null;
    }



    //@Override
    public Message deliverWithReply(Message message, long waitTime)
        throws DSPException
    {
        // TODO Auto-generated method stub
        return null;
    }



    // ///////////////////////////////////
    // //////// Privage Section //////////
    // ///////////////////////////////////


    private void processMessage(Message message)
    {
        String newData = message.getClass().getName();
        log.debug("DataCollector message.class=" + newData);
        log.debug("Adding DSP message to the buffer...");
        theBuffer.add(newData);
    }

}
