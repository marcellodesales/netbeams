package org.netbeams.dsp.demo.miceaction.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.netbeams.dsp.persistence.DSPInMemoryDataPersistence;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This servlet is responsible for displaying the actions from the mouse
 * interactions. It defines a given
 * 
 * @author marcello
 * 
 */
public class ViewMiceActionsServlet extends HttpServlet {

    private int refreshCounter = 0;

    private BundleContext bc;

    /**
     * Refresh rate of the servlet that will display the current state of the
     * persistence repository.
     */
    private static final byte REFRESH = 10;

    /**
     * Value for the serial version.
     */
    private static final long serialVersionUID = 1L;

    public ViewMiceActionsServlet(BundleContext bc) {
        this.bc = bc;
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Test Servlet</title>");
		out.println("<meta http-equiv='refresh' content='"+ REFRESH +"'></head>");
		out.println("<body>This is just a test... This page has been refreshed "+ ++ this.refreshCounter +" times...");
		
		Map<UUID, List<String>> repository = DSPInMemoryDataPersistence.INSTANCE.getRepositoryData();
		
		out.println("<BR><BR><b>Register Mouse Actions</b><BR><form method='POST' action='/registerMiceActions'>");
		out.println("MouseID: <input type='text' name='mouseProducerId'><BR>");
		out.println("Mouse Data n: <input type='text' name='mouseProducerData'><BR>");
		out.println("Mouse Data n+1: <input type='text' name='mouseProducerData'><BR>");
		out.println("<input type='submit' value='Register Mouse Click'></form>");
		
		ServiceReference reference = bc.getServiceReference(DSPInMemoryDataPersistence.class.getName());
		
        DSPInMemoryDataPersistence memRepository = (DSPInMemoryDataPersistence)bc.getService(reference);
        
        out.println("<BR><BR>Producers and their observable values.");
        Map<UUID, List<String>> memRepo = memRepository.getRepositoryData();
        for (UUID observer : memRepo.keySet()) {
            out.println("<BR><b>Producer = " + observer + "</b>");
            for (String value : memRepo.get(observer)) {
                out.println("<BR><b>value = " + value);
            }
        }
		out.println("</body></html>");
		out.close();
	}
}