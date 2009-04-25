
package org.netbeams.dsp.management.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.management.Manager;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.util.NetworkUtil;
import org.w3c.dom.Document;


public class PropertyUIServlet extends HttpServlet
{
	private static final long serialVersionUID = ("urn:" + PropertyUIServlet.class
            .getName()).hashCode();

    private static final Logger log = Logger.getLogger(PropertyUIServlet.class);
    
    final public static String BASE_URI     = "/property-ui";

    private boolean isActive;
    private Manager manager;

    private UIModel uiModel = new UIModel();
    
    boolean shouldStop = false;

    public PropertyUIServlet(Manager manager){
        isActive = false;
        this.manager = manager;
    }

    /**
     * @Override 
     */    
//    public void init() throws ServletException
//    {
//    	// TODO: Hack for the demo only. Get this information from the DSPManager
//    	List<String> components = new ArrayList<String>();
//    	components.add("StockProducer");
//    	PropertyUI.setComponents(components);
//    }
 
    /**
     * @Override 
     */    
    public void destroy(){
    	shouldStop = true;
    }

    
    /**
     * @Override 
     */    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        log.debug("PropertyUIServlet Processing URI Query: " + uri);
        if (uri.indexOf("/PULL") != -1) {
        	response.setContentType("text/plain");
        	ServletOutputStream out = response.getOutputStream();
        	String data = PropertyUI.getData();
        	out.println(data);
        }
        else {
        	// User pressed button on Property UI
        	try{
	        	ActionUI action = createActionUI(request);
	        	String data = processAction(action);
	        	ServletOutputStream out = response.getOutputStream();
	        	out.println(data);
        	}catch(RuntimeException e){
        		log.warn("Error processign the message", e);
        		throw e;
        	}
        }
    }

    private String processAction(ActionUI action) {
    	if("QUERY".equals(action.getActionWidgetID())){
    		processQuery(action);
    	}else if("UPDATE".equals(action.getActionWidgetID())){
    		processUpdate(action);
    	}
		return null;
	}

	private void processUpdate(ActionUI action) {
		String interacionId = null;
    	DSProperties props = new DSProperties();
    	// Obtain properties
    	
    	for(int x = 0; x < action.getInputValues().size(); ++ x){
    		String name = uiModel.properties.getProperty().get(x).getName();
    		String value = action.getInputValues().get(x);
    		DSProperty p = new DSProperty();
    		p.setName(name);
    		p.setValue(value);
    		props.getProperty().add(p);
    	}
    	
    	try {
    		interacionId = manager.update(uiModel.selectedComponent, "org.netbeams.dsp.demo.stocks.producer", NetworkUtil.getCurrentEnvironmentNetworkIp(), props);
		} catch (DSPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Update UI with pending state
		PropertyUI.createLabel(uiModel.properties.getProperty().size() + 3, 1, "Pending...");
		
		while(!shouldStop){
			MessageContent dom = manager.retrieveInteractionReply(interacionId);
			// HACK:
			if(dom != null){
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Create Layout
		PropertyUI.clear();
		List<DSProperty> pList = props.getProperty();
		int x = 0;
		for(;x < pList.size(); ++x){
			PropertyUI.createLabel(x+1, 1, pList.get(x).getName());
			PropertyUI.createInput(x+1, 2, pList.get(x).getValue());
		}
		PropertyUI.createButton(x + 1, 1, "Update");
		PropertyUI.createButton(x+1, 2, "Clear");
		// Update Model
		uiModel.properties = props;
	}

	private void processQuery(ActionUI action) {
		
		// Add pending label
		PropertyUI.clear();
		PropertyUI.createLabel(1, 1, "Pending...");
		
		String interacionId = null;
    	try {
    		DSProperties dummy = new DSProperties();
    		interacionId = manager.query(action.getInputValues().get(0), "", 
    				NetworkUtil.getCurrentEnvironmentNetworkIp(), dummy);
		} catch (DSPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MessageContent content = null;
		while(!shouldStop){
			content = manager.retrieveInteractionReply(interacionId);
			// HACK:
			if(content != null){
				log.debug("Found message for " + interacionId);
				break;
			}else{
				log.debug("No message for " + interacionId);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DSProperties props = (DSProperties)content;
		// Update model
		uiModel.properties = props;
		uiModel.selectedComponent = action.getInputValues().get(0);
		// Create Layout
		PropertyUI.clear();
		List<DSProperty> pList = props.getProperty();
		int x = 0;
		for(;x < pList.size(); ++x){
			PropertyUI.createLabel(x+1, 1, pList.get(x).getName());
			PropertyUI.createInput(x+1, 2, pList.get(x).getValue());
		}
		PropertyUI.createButton(x + 1, 1, "Update");
		PropertyUI.createButton(x+1, 2, "Clear");
	}

	private ActionUI createActionUI(HttpServletRequest request) {
		// /property-ui/PUSH=Query:DSPStockProducer
		
		ActionUI action = new ActionUI();
		
		String reqURI = request.getRequestURI();
		if(reqURI.indexOf("PUSH=Query") > -1){
		    String resource = reqURI.substring(BASE_URI.length() + 1);
		    String componentNoteId = resource.substring("PUSH=Query:".length());
		    
		    log.debug("Resource=" + resource + " componentNodeId=" + componentNoteId);		    
		    
		    action.setActionWidgetID("QUERY");
		    List<String> list = new ArrayList<String>();
		    list.add(componentNoteId);
		    action.setInputValues(list);
		}else if(reqURI.indexOf("PUSH=Update:") > -1){
		    String uiRequest = reqURI.substring(BASE_URI.length() + 1);
		    String in = uiRequest.substring("PUSH=Update:".length());
		    
		    log.debug("Inputs=" + in);	
		    
			List<String> inputs = parseInput(in);
			action.setActionWidgetID("UPDATE");
			action.setInputValues(inputs);
		}
	    
	    
		return action;
	}

	private List<String> parseInput(String inputs){
		List<String> result = new ArrayList<String>();
	    StringTokenizer t = new StringTokenizer(inputs, ":");
		while(t.hasMoreTokens()){
			result.add(t.nextToken());
		}
		return result;
	}



}
