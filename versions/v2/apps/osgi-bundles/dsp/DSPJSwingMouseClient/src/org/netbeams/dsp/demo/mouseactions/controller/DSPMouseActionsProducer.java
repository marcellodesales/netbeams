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
import org.netbeams.dsp.wiretransport.osgi.DSPWireTransportActivator;

public class DSPMouseActionsProducer implements NetBeamsMouseListener {

    /**
     * Default Logger
     */
    private static final Logger log = Logger.getLogger(DSPMouseActionsProducer.class);
    /**
     * Executor responsible for the execution of the thread with a fixed delay to send the values.
     */
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * The local memory defines the measurements from the mouse actions.
     */
    private List<NetBeamsMouseInfo> localMemory;
    /**
     * Reference to the DSP messages queue service where the messages are delivered.
     */
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
        log.trace("The DSPMouseActionsProducer initialized: internal sender at every 20 seconds");
    }

    public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
        if (this.localMemory.size() <= 30) {
            log.trace("Tracking updates since there are less than 30 items on the producer local memory...");
            this.localMemory.add(netBeamsMouseInfo);
        } else {
            log.trace("Not Tracking updates... Mouse Actions JSwing Internal Memory Full...");
        }
    }

    /**
     * The DSPMouseWorker implements a pattern of a given worker thread that is used at different types by the Executor.
     * 
     * @author marcello de sales <marcello.sales@gmail.com>
     */
    private class DspMouseWorker extends Thread {

        /**
         * Default Logger
         */
        private final Logger thrlog = Logger.getLogger(DspMouseWorker.class);
        /**
         * The mouse actions is the list of all observations (measurements).
         */
        private List<NetBeamsMouseInfo> mouseActions;

        /**
         * Constructs the a new DSPMouseWorker thread to send the mouse actions
         * @param mouseActions
         */
        public DspMouseWorker(List<NetBeamsMouseInfo> mouseActions) {
            this.mouseActions = mouseActions;
            this.setDaemon(true);
            thrlog.trace("Starting the worker for mouse actions with " + mouseActions.size() + " instances.");
        }

        /**
         * @return the MouseActionsContainer for the given set of mouseActions, which is passed in the instantiation of
         *         an instance of this DSPMouseWorker.
         */
        private MouseActionsContainer makeMouseActionsContainerFromMouseActions() {
            MouseActionsContainer actionsContainer = new MouseActionsContainer();
            thrlog.debug("A total of " + this.mouseActions.size() + " mouse actions will be registered...");
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

            thrlog.debug("Preparying to send MouseActionsContainer data to Messages Queues");
            MouseActionsContainer data = this.makeMouseActionsContainerFromMouseActions();

            // Create the message
            String localIPAddress;
            try {
                localIPAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e1) {
                localIPAddress = "127.0.0.1";
            }
            thrlog.trace("Sending Mouse Actions from " + localIPAddress);
            ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                    MouseActionDSPComponent.class.getName(), localIPAddress, data.getContentContextForJAXB());
            ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("WEB",
                    DSPWireTransportActivator.DSP_TRANSPORT_DESTINATION, null);
            // Note that the consumer has the Wire Transport default DEMO IP as the destination.
            thrlog.trace("Sending Mouse Actions to " + consumer.getComponentLocator().getNodeAddress().getValue());
            Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);

            try {
                if (data != null) {
                    thrlog.debug("Number of Mouse Actions to send: " + data.getMouseAction().size() + "items");
                }
                Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data);
                messagesQueueService.addMessageToOutboundQueue(message);

            } catch (JAXBException e) {
                thrlog.error(e.getMessage(), e);
            } catch (ParserConfigurationException e) {
                thrlog.error(e.getMessage(), e);
            }
        }
    }
}