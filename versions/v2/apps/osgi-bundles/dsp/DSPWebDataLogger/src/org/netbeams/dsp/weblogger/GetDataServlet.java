
package org.netbeams.dsp.weblogger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class GetDataServlet
    extends HttpServlet
{

    private Buffer theBuffer;



    public GetDataServlet(Buffer theBuffer)
    {
        this.theBuffer = theBuffer;
    }



    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        System.out.println("Data Servlet Processing URI: " + uri);
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        String data = theBuffer.getData();
        out.println(data);
    }



    private static final long serialVersionUID = ("urn:" + GetDataServlet.class
                                                       .getName()).hashCode();

}
