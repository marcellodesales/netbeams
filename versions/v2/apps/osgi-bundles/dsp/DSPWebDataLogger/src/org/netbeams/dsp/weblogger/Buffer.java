
package org.netbeams.dsp.weblogger;

import java.util.ArrayList;
import java.util.List;



public class Buffer
{

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
                // Ignore
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
