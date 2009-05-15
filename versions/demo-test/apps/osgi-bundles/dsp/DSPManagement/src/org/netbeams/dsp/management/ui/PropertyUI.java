
package org.netbeams.dsp.management.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;



public class PropertyUI
{

	private static PropertyUI singleton = new PropertyUI();
	
    private static final Logger log = Logger.getLogger(PropertyUI.class);
    
    private List<String> actions = new ArrayList<String>();


    public static void createButton(int row, int column, String text)
    {
        singleton.createWidget("BUTTON", row, column, text);
    }


    public static void createInput(int row, int column, String text)
    {
        singleton.createWidget("INPUT", row, column, text);
    }


    public static void createLabel(int row, int column, String text)
    {
        singleton.createWidget("LABEL", row, column, text);
    }

    
    public static void setComponents(List<String> components)
    {
    	String inst = "{action: 'SET_COMPONENTS', components: '";
    	String comp = "";
    	for (String c : components) {
    		if (!comp.equals(""))
    			comp += ":";
    		comp += c;
    	}
    	inst += comp + "'}";
        singleton.addAction(inst);
    }

    
    public static void clear()
    {
        singleton.addAction("{action: 'CLEAR'}");
    }

    public static void createNullAction()
    {
    	singleton.addAction("{action: 'NULL'}");
    }

    private void createWidget(String type, int row, int column, String text)
    {
    	singleton.addAction("{action: 'CREATE', type: '" + type + "', row: " + row + ", column: " + column + ", text: '" + text + "'}");
    }
 

    
    private synchronized void addAction(String action)
    {
    	actions.add(action);
    	this.notify();
    }
    
    
    public static String getData()
    {
    	return singleton.waitForNextAction();
    }

    
    private synchronized String waitForNextAction()
    {
        while (actions.size() == 0) {
            try {
                wait();
            }
            catch (InterruptedException ex) {
                log.debug("A new UI event was added to the buffer...");
            }
        }
        String firstData = actions.get(0);
        actions.remove(0);
        return firstData;
    }

}
