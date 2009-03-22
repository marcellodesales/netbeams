
package org.netbeams.dsp.management;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.management.ui.PropertyUI;
import org.netbeams.dsp.management.ui.StaticContentServlet;
import org.netbeams.dsp.message.NodeAddress;



public class ManagementServlet extends HttpServlet
{
	
	private static final long serialVersionUID = ("urn:" + ManagementServlet.class
            .getName()).hashCode();

	
    final public static String BASE_URI = "/dsp-management";
    
    private String             documentRoot = "/web/";


	private static final Logger log = Logger.getLogger(ManagementServlet.class);
	
    private boolean isActive;
    private Manager manager;
    
    /**
     * 
     * @param dspManager
     */
    public ManagementServlet(Manager manager){
        isActive = false;
        this.manager = manager;
    }
 
    /**
     * @Override 
     */    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        log.debug("PropertyUIServlet Processing URI: " + uri);
        
        if(processStaticContent(uri, request, response)){
        	return;
        }
        
//    	if(!dspManager.isActive()){
//    		log.warn("Management component is not active ");
//    		outputNoActive(response);
//    		return;
//    	}
        
      }



    private boolean processStaticContent(String uri, HttpServletRequest request, HttpServletResponse response) 
    	throws IOException 
    {
    	boolean result = false;
    	
        String resource = uri.substring(BASE_URI.length() + 1);

        if (resource.equals("")){
            resource = "index.html";
            PropertyUI.clear();
            List<String> components = obtainComponents();
         	PropertyUI.setComponents(components);
        }

        String fileName = documentRoot + resource;

        if (resource.endsWith(".html")) {
            response.setContentType("text/html");
            sendTextFile(response, fileName);
            result = true;
        }
        else if (resource.endsWith(".js")) {
            response.setContentType("text/javascript");
            sendTextFile(response, fileName);
            result = true;            
        }
        else if (resource.endsWith(".png")) {
            response.setContentType("image/png");
            sendImageFile(response, fileName);
            result = true;            
        }
        else
            log.debug("Unknown Content-Type: " + resource);		
        
        return result;
	}
    
    private List<String> obtainComponents() 
    {
    	List<String> components = new ArrayList<String>();
    	String[][] regComps = manager.getRegisteredDspComponents();
    	for(String[] comp: regComps){
    		components.add(comp[0]);
    	}
    	return components;
    }

	private void sendTextFile(HttpServletResponse resp, String fileName)
	    throws IOException
	{
	    final int READ_BUFFER = 4096;
	
	    ServletOutputStream out = resp.getOutputStream();
	    InputStream is = this.getClass().getResourceAsStream(fileName);
	    byte b[] = new byte[READ_BUFFER];
	    int l = 0;
	    while ((l = is.read(b)) > 0) {
	        out.write(b, 0, l);
	    }
	}
    
    
    private void sendImageFile(HttpServletResponse resp, String fileName)
	    throws IOException
	{
	    ServletOutputStream out = resp.getOutputStream();
	    InputStream is = this.getClass().getResourceAsStream(fileName);
	    BufferedImage image = ImageIO.read(is);
	    ImageIO.write((RenderedImage) image, "png", out);
	}
    

	private void outputNoActive(HttpServletResponse response) throws IOException {
    	PropertyUI.createNullAction();
    	outputData(response);		
	}

	private void outputData(HttpServletResponse response) throws IOException {
       	response.setContentType("text/plain");

    	String data = PropertyUI.getData();

       	ServletOutputStream out = response.getOutputStream();
    	out.println(data);
    	out.flush();
	}

}
