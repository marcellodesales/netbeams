
package org.netbeams.dsp.weblogger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



public class PropertyUIServlet
    extends HttpServlet
{

    private static final Logger log = Logger.getLogger(PropertyUIServlet.class);
    
    final public static String BASE_URI     = "/property-ui";


    public PropertyUIServlet()
    {
    }



    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        log.debug("PropertyUIServlet Processing URI: " + uri);
        if (uri.indexOf("/PULL") != -1) {
        	response.setContentType("text/plain");
        	ServletOutputStream out = response.getOutputStream();
        	String data = PropertyUI.getData();
        	out.println(data);
        }
        else {
        	// User pressed button on Property UI
        }
    }



    private static final long serialVersionUID = ("urn:" + PropertyUIServlet.class
                                                       .getName()).hashCode();

}
