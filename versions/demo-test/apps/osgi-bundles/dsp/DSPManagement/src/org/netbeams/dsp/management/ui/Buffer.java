
package org.netbeams.dsp.management.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;



public class Buffer
{

    private static final Logger log = Logger.getLogger(Buffer.class);
    
    private List<String> data = new ArrayList<String>();

    public synchronized void add(String newData)
    {
        data.add(newData);
        this.notify();
    }



    public synchronized String getData()
    {
        while (data.size() == 0) {
            try {
                this.wait();
            }
            catch (InterruptedException ex) {
                log.debug("A new message was added to the buffer...");
            }
        }
        String stringifiedData = "";
        for (String item : data) {
            if (!stringifiedData.equals(""))
                stringifiedData += "$";
            stringifiedData += item;
        }
        data.clear();
        return stringifiedData;
    }

}
