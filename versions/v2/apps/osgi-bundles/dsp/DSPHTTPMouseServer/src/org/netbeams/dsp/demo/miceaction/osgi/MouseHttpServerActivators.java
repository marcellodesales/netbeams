package org.netbeams.dsp.demo.miceaction.osgi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;

import org.netbeams.dsp.demo.miceaction.controller.CollectMouseActionsServlet;
import org.netbeams.dsp.demo.miceaction.controller.ViewMiceActionsServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class MouseHttpServerActivators implements BundleActivator {

    private static BundleContext bc;

    private static HttpService httpService;

    private ServiceReference httpSR;

    //@Override
    public void start(BundleContext bc) throws BundleException {

        System.out.println("Start Mice Actions Server");
        MouseHttpServerActivators.bc = bc;

        ServiceReference reference = bc.getServiceReference(HttpService.class
                .getName());
        HttpService service = (HttpService) bc.getService(reference);

        this.httpSR = reference;
        httpService = service;

        this.httpSR = bc.getServiceReference(HttpService.class.getName());
        httpService = (HttpService) bc.getService(this.httpSR);

        System.out.println("HttpService.class.getName():"
                + HttpService.class.getName());

        this.printServiceProperties();
        this.registerServlets();
        
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        
        System.out.println("Mouse Services Available:");
        System.out.println("View: http://"+host+":8080/showMiceActions");
        System.out.println("Register: http://"+host+":8080/registerMiceActions");
    }

    private void printServiceProperties() {
        Object port = this.httpSR.getProperty("port");
        if (port != null) {
            port = port.toString();
        } else {
            System.out.println("Ooops - failed to find the port property!!!");
        }
        // Dump the properties as known by the http service
        System.out.println("--- Propery keys ---");
        for (String key : httpSR.getPropertyKeys()) {
            System.out.println(key + ": --> " + httpSR.getProperty(key));
        }
    }

    //	
    // private void registerStaticResources() {
    // String alias = "/index.html";
    // String resourceName = "/http_test/index.html";
    // String internalName = "n1";
    //
    // HttpContext context = httpService.createDefaultHttpContext();
    // try {
    // httpService.registerResources(alias, internalName, null);
    // } catch (NamespaceException e) {
    // e.printStackTrace();
    // }
    // String uri = "URI: " + ":8080"+alias; // create the probable URI
    // port+alias
    // System.out.println (uri);
    //	      
    // String servletAlias = "/greetings";
    //
    // System.out.println("Registered Greetings Servlet with alias: "
    // + servletAlias);
    // }
    //	
    private void registerServlets() {
        String aliasShow = "/showMiceActions";
        String aliasRegisterActions = "/registerMiceActions";
        // / Since the HTTP Service is available from
        // "http://localhost:8080" the
        // / Greetings servlet will be available from
        // "http://localhost:8080/show"
        try {
            httpService.registerServlet(aliasShow, new ViewMiceActionsServlet(bc),
                    null, null);
            httpService.registerServlet(aliasRegisterActions, new CollectMouseActionsServlet(bc),
                    null, null);

        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NamespaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //@Override
    public void stop(BundleContext arg0) throws Exception {
        System.out.println("Stopping mice actions server");
        MouseHttpServerActivators.bc = null;
    }
}
