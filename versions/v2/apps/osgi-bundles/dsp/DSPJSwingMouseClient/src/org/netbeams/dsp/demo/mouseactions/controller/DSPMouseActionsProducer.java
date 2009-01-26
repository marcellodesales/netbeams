package org.netbeams.dsp.demo.mouseactions.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;
import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;
import org.netbeams.dsp.wiretransport.controller.DSPWireTransportHttpClient;

public class DSPMouseActionsProducer implements NetBeamsMouseListener {

    /**
     * Detault Logger
     */
    private static final Logger log = Logger.getLogger(DSPMessagesDirectory.class);

    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * The local memory defines the measurements from the mouse actions.
     */
    private List<NetBeamsMouseInfo> localMemory;
    private DSPMessagesDirectory messagesQueueService;

    /**
     * Creates a new Mouse Actions client that is responsible for sending the information to the DSP component.
     * 
     * @param dspBc is the DSPContext implementation initialized by the OSGi framework.
     * @param component is the DSP base component responsible for this producer.
     */
    public DSPMouseActionsProducer(DSPMessagesDirectory messagesQueue) {
        this.localMemory = new LinkedList<NetBeamsMouseInfo>();
        scheduler.scheduleWithFixedDelay(new DspMouseWorker(this.localMemory), 0, 20, TimeUnit.SECONDS);
        this.messagesQueueService = messagesQueue;
        log.trace("The DSPMouseActionsProducer initialized: internal sender at every 10 seconds for 3 times...");
    }

    public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
        if (this.localMemory.size() <= 30) {
            log.trace("Tracking updates since there are less than 30 items on the producer local memory...");
            this.localMemory.add(netBeamsMouseInfo);
        }
    }

    /**
     * The DSPMouseWorker implements a pattern of a given worker thread that is used at different types by the Executor.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspMouseWorker extends Thread {

        /**
         * Detault Logger
         */
        private final Logger thrlog = Logger.getLogger(DspMouseWorker.class);
        
        /**
         * The mouse actions is the list of all observations (measuraments).
         */
        private List<NetBeamsMouseInfo> mouseActions;

        public DspMouseWorker(List<NetBeamsMouseInfo> mouseActions) {
            this.mouseActions = mouseActions;
            this.setDaemon(true);
            thrlog.trace("starting the worker for mouse actions with " + mouseActions.size() + "instances.");
        }

        /**
         * The DSP interface where the message is sent through the Broker.
         * 
         * @param data is the payload for the DSP component. It contains the given MouseActionsContainer with the
         *        measurements of the mouse captured from the JFrame.
         * @throws DSPException with any communication error in the platform.
         */
        private void send(MouseActionsContainer data) {
            thrlog.debug("Preparying to send MouseActionsContainer data");
            // Create the message
            String localIPAddress;
            try {
                localIPAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e1) {
                localIPAddress = "127.0.0.1";
            }
            thrlog.trace("Sending from " + localIPAddress);
            ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                    MouseActionDSPComponent.class.getName(), localIPAddress, data.getContentContextForJAXB());
            ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("WEB",
                    DSPWireTransportHttpClient.DEFAULT_DEMO_IP, null);
            
            thrlog.trace("Sending to " + consumer.getComponentLocator().getNodeAddress().getValue());
            Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);
            
            try {
                Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data);
                messagesQueueService.addMessageToOutboundQueue(message);
                if (data != null) {
                    thrlog.debug("Sending " + data.getMouseAction().size() + "items");
                }

            } catch (JAXBException e) {
                thrlog.error(e.getMessage(), e);
            } catch (ParserConfigurationException e) {
                thrlog.error(e.getMessage(), e);
            }

            // // Always check if there is a broker available
            // MessageBrokerAccessor messageBroker = bc.getDataBroker();
            // if (messageBroker != null) {
            // messageBroker.send(message);
            // } else {
            // Log.log("MouseAction.push: Message Broker not available");
            // }
        }

        /**
         * @return the MouseActionsContainer for the given set of mouseActions, which is passed in the instantiation of
         *         an instance of this DSPMouseWorker.
         */
        private MouseActionsContainer makeMouseActionsContainerFromMouseActions() {
            MouseActionsContainer actionsContainer = new MouseActionsContainer();
            for (NetBeamsMouseInfo mouseInfoCollected : this.mouseActions) {
                MouseAction mc = new MouseAction();
                mc.setButton(ButtonName.fromValue(mouseInfoCollected.getMouseButton().toString()));
                mc.setEvent(EventName.fromValue(mouseInfoCollected.getMouseAction().toString()));
                mc.setX((int) mouseInfoCollected.getPointClicked().getX());
                mc.setY((int) mouseInfoCollected.getPointClicked().getY());

                actionsContainer.getMouseAction().add(mc);
            }
            return actionsContainer;
        }

        public void run() {

            MouseActionsContainer data = this.makeMouseActionsContainerFromMouseActions();
            this.send(data);

            // this section determines other communication channels

            // System.out.println("Waking up " + getClass() +
            // " to register actions to server");
            // System.out.println("http://localhost:8080/registerMiceActions");
            // HttpClient client = new HttpClient();
            // client.getParams().setParameter("http.useragent",
            // "DSP Desktop client on JSwing ");
            //
            // PostMethod post = new
            // PostMethod("http://localhost:8080/registerMiceActions");
            //
            // List<NameValuePair> formValues = new LinkedList<NameValuePair>();
            // formValues.add(new NameValuePair("mouseProducerId",
            // UUID.randomUUID().toString()));
            // // for (String data : this.mouseActions) {
            // // formValues.add(new NameValuePair("mouseProducerData", data));
            // // }
            // post.setRequestBody(formValues.toArray(new
            // NameValuePair[formValues.size()]));
            // // execute method and handle any error responses.
            //
            // BufferedReader br = null;
            // int returnCode;
            // try {
            // returnCode = client.executeMethod(post);
            //
            // if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
            // System.err.println("The Post method is not implemented by this URI"
            // );
            // // still consume the response body
            // post.getResponseBodyAsString();
            // } else {
            // br = new BufferedReader(new
            // InputStreamReader(post.getResponseBodyAsStream()));
            // String readLine;
            // while (((readLine = br.readLine()) != null)) {
            // System.err.println(readLine);
            // }
            // }
            //
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        }
    }
}