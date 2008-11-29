package  org.netbeams.dsp.example.miceaction.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseInfo;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseListener;

public class DSPMouseActionsHttpServerCollectorClient implements NetBeamsMouseListener {
    
    private List<String> localMemory;

    public final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public DSPMouseActionsHttpServerCollectorClient() {
        this.localMemory = new LinkedList<String>();
        this.scheduler.scheduleWithFixedDelay(
                new DspMouseWorker(this.localMemory), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
        if (this.localMemory.size() <= 10) {
            this.localMemory.add(netBeamsMouseInfo.toString());
        }
    }
    
    private class DspMouseWorker extends Thread {

        private List<String> mouseActions;
        
        public DspMouseWorker(List<String> mouseActions) {
            this.mouseActions = mouseActions;
            this.setDaemon(true);
        }
        
        @Override
        public void run() {
            System.out.println("Waking up " + getClass() + " to register actions to server");
            System.out.println("http://localhost:8080/registerMiceActions");
            
            HttpClient client = new HttpClient();
            client.getParams().setParameter("http.useragent", "DSP Desktop client on JSwing ");
            
            PostMethod post = new PostMethod("http://localhost:8080/registerMiceActions");
            
            List<NameValuePair> formValues = new LinkedList<NameValuePair>();
            formValues.add(new NameValuePair("mouseProducerId", UUID.randomUUID().toString()));
            for (String data : this.mouseActions) {
                formValues.add(new NameValuePair("mouseProducerData", data));
            }
            post.setRequestBody(formValues.toArray(new NameValuePair[formValues.size()]));
            // execute method and handle any error responses.
            
            BufferedReader br = null;
            int returnCode;
            try {
                returnCode = client.executeMethod(post);
                
                if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                    System.err.println("The Post method is not implemented by this URI");
                    // still consume the response body
                    post.getResponseBodyAsString();
                  } else {
                    br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
                    String readLine;
                    while(((readLine = br.readLine()) != null)) {
                      System.err.println(readLine);
                    }
                  }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}