
package org.netbeams.dsp.weblogger;



import java.util.UUID;

import javax.servlet.ServletException;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.ComponentLocator;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.Log;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;



public class DataCollector
    implements DSPComponent
{

    // Component Type
    public final static String         COMPONENT_TYPE = "org.netbeams.dsp.weblogger";

    private static ComponentDescriptor componentDescriptor;


    private UUID                       uuid;

    private DSPContext                 context;
    private ComponentLocator           locator;
    private ComponentDescriptor        descriptor;

    private Buffer                     theBuffer;
    private HttpService                httpService;



    // ///////////////////////////////////////////
    // //////// DSP Component Interface //////////
    // ///////////////////////////////////////////


    @Override
    public UUID getUUID()
    {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public String getComponentType()
    {
        return COMPONENT_TYPE;
    }



    @Override
    public void initComponent(UUID uuid, DSPContext context)
        throws DSPException
    {
        Log.log("DataCollector.initComponent()");
        this.context = context;
        this.uuid = uuid;
    }



    @Override
    public ComponentDescriptor getComponentDescriptor()
    {
        return componentDescriptor;
    }



    @Override
    public void startComponent()
    {
        Log.log("DataCollector.startComponent()");
        theBuffer = new Buffer();
        httpService = (HttpService) context.getResource("osgi:HttpService");
        try {
            httpService.registerServlet(GetDataServlet.BASE_URI,
                    new GetDataServlet(theBuffer), null, null);
            httpService.registerServlet(WebAppServlet.BASE_URI,
                    new WebAppServlet(), null, null);
        }
        catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NamespaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    @Override
    public void stopComponent()
    {
        Log.log("DataCollector.stopComponent()");
        httpService.unregister(GetDataServlet.BASE_URI);
        httpService.unregister(WebAppServlet.BASE_URI);
    }



    @Override
    public void deliver(Message message)
        throws DSPException
    {
        Log.log("DataCollector.deliver()");
        processMessage(message);
    }



    @Override
    public Message deliverWithReply(Message request)
        throws DSPException
    {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
        return null;
    }



    @Override
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
        Log.log("DataCollector message.class=" + newData);
        theBuffer.add(newData);
    }

}
