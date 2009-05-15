package org.netbeams.dsp.demo.miceaction.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.netbeams.dsp.persistence.DSPInMemoryDataPersistence;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class CollectMouseActionsServlet extends HttpServlet {

    private BundleContext bc;

    public CollectMouseActionsServlet(BundleContext bc) {
        this.bc = bc;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // This method is called by the servlet container to process a GET request.
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGetOrPost(req, resp);
    }

    // This method is called by the servlet container to process a POST request.
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGetOrPost(req, resp);
    }

    // This method handles both GET and POST requests.
    private void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the value of a request parameter; the name is case-sensitive
        String mouseProducerId = req.getParameterValues("mouseProducerId")[0];
        String[] mouseProducerData = req.getParameterValues("mouseProducerData");
        if (mouseProducerId != null && !"".equals(mouseProducerId) && mouseProducerData != null && !"".equals(mouseProducerData)) {
            ServiceReference reference = bc.getServiceReference(DSPInMemoryDataPersistence.class.getName());
            DSPInMemoryDataPersistence memData = (DSPInMemoryDataPersistence)bc.getService(reference);
            memData.insertData(UUID.fromString(mouseProducerId), mouseProducerData);
        }
        
        // The following generates a page showing all the request parameters
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/plain");
        out.println("RECEIVED DATA FROM " + mouseProducerId);
        // Get the values of all request parameters
        out.close();
    }

}
