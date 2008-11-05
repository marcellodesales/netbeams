
package org.netbeams.dsp.weblogger;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.netbeams.dsp.util.Log;




public class WebAppServlet
    extends HttpServlet
{

    private String        documentRoot = "/web/";



    public WebAppServlet()
    {
    }



    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        Log.log("Processing URI: " + uri);
        // Strip leading '/'
        String resource = uri.substring(1);
        if (resource.equals(""))
            resource = "index.html";

        String fileName = documentRoot + resource;

        if (resource.endsWith(".html")) {
            response.setContentType("text/html");
            sendTextFile(response, fileName);
        }
        else if (resource.endsWith(".js")) {
            response.setContentType("text/javascript");
            sendTextFile(response, fileName);
        }
        else if (resource.endsWith(".png")) {
            response.setContentType("image/png");
            sendImageFile(response, fileName);
        }
        else
            Log.log("Unknown Content-Type: " + resource);
    }



    private void sendTextFile(HttpServletResponse resp, String fileName)
        throws IOException
    {
        final int READ_BUFFER = 4096;
        
        ServletOutputStream out = resp.getOutputStream();
        InputStream is = this.getClass().getResourceAsStream(fileName);
        byte b[] = new byte[READ_BUFFER];
        int l = 0;
        while((l = is.read(b)) > 0) {
            out.write(b, 0, l);
        }
    }



    private void sendImageFile(HttpServletResponse resp, String fileName)
        throws IOException
    {
        //resp.setContentLength((int) raf.length());
        ServletOutputStream out = resp.getOutputStream();
        InputStream is = this.getClass().getResourceAsStream(fileName);
        BufferedImage image = ImageIO.read(is);
        ImageIO.write((RenderedImage) image, "png", out);
    }



    private static final long serialVersionUID = ("urn:" + WebAppServlet.class
                                                       .getName()).hashCode();

}
