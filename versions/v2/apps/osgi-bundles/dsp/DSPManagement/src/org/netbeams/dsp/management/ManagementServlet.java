
package org.netbeams.dsp.management;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.message.NodeAddress;



public class ManagementServlet
    extends HttpServlet
{
	private static final Logger log = Logger.getLogger(ManagementServlet.class);
	
    final public static String BASE_URI     = "/dsp-management";

    private boolean isActive;
    private DSPManager dspManager;
    
    /**
     * 
     * @param dspManager
     */
    public ManagementServlet(DSPManager dspManager){
        isActive = false;
        this.dspManager = dspManager;
    }

    /**
     * @Override 
     */    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
    	if(!dspManager.isActive()){
    		log.warn("Management component is active ");
    		outputNoActive(response);
    		return;
    	}
    	
    	Map params = request.getParameterMap();
    	String action = (String)params.get("ACTION");
    	if(action == null){
    		response.setContentType("text/html; charset=utf-8");
    		outputFile(response, "notActive");
    	}
    	
    	if("CONFIGURE_DSPCOMPONENT".equals(action)){
    		configureDSPComponent(response, params);	
    	}else{
    		log.warn("Request is unknown");
    	}
    	
        String uri = request.getRequestURI();

        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        out.println(data);
    }



    private void configureDSPComponent(HttpServletResponse response, Map params) {
    	String nodeAddressStr = (String)params.get("NODE_ADDRESS");
    	String componentNodeId = (String)params.get("COMPONENT_NODEID");
    	String propName = (String)params.get("PROPERTY_NAME");
    	String propValue = (String)params.get("PROPERTY_VALUE");
    	
    	DSProperties properties = new DSProperties();
    	DSProperty property = new DSProperty();
    	property.setName(propName);
    	property.setValue(propValue);
    	
    	NodeAddress nodeAddress = new NodeAddress(nodeAddressStr);
    	
    	try {
			dspManager.updateProperties(nodeAddress, componentNodeId, properties);
		} catch (DSPException e) {
			log.warn("Could not update property for " + componentNodeId + ' ' + nodeAddressStr);
		}   	
	}

	private void outputFile(HttpServletResponse response, String fileName) throws IOException {
        final int READ_BUFFER = 4096;

        ServletOutputStream out = response.getOutputStream();
        InputStream is = this.getClass().getResourceAsStream(fileName);
        byte b[] = new byte[READ_BUFFER];
        int l = 0;
        while ((l = is.read(b)) > 0) {
            out.write(b, 0, l);
        }
    	
		
	}



	private static final long serialVersionUID = ("urn:" + ManagementServlet.class
                                                       .getName()).hashCode();

}
