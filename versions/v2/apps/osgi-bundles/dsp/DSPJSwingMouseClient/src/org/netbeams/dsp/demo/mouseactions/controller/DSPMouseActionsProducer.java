package org.netbeams.dsp.demo.mouseactions.controller;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.demo.mouseactions.ObjectFactory;
import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;
import org.netbeams.dsp.demo.mouseactions.model.dsp.MouseActionDSPComponent;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.NetworkUtil;

/**
 * The DSPMouseActionsProducer creates the DSP Messages from the mouse actions captured from the
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class DSPMouseActionsProducer implements NetBeamsMouseListener {

    /**
     * Default Logger
     */
    private static final Logger log = Logger.getLogger(DSPMouseActionsProducer.class);
    /**
     * The local memory defines the measurements from the mouse actions.
     */
    private List<NetBeamsMouseInfo> localMemory;
    private DSPContext dspContext;
    
    /**
     * Creates a new Mouse Actions client that is responsible for sending the information to the DSP component.
     * 
     * @param dspBc is the DSPContext implementation initialized by the OSGi framework.
     * @param component is the DSP base component responsible for this producer.
     */
    public DSPMouseActionsProducer(DSPContext dspContext) {
        this.dspContext = dspContext;
        this.localMemory = new LinkedList<NetBeamsMouseInfo>();
        log.trace("The DSPMouseActionsProducer initialized: internal sender at every 10 seconds");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.netbeams.dsp.demo.mouseactions.controller.NetBeamsMouseListener#trackMouseActionUpdate(org.netbeams.dsp.demo
     * .mouseactions.model.NetBeamsMouseInfo)
     */
    public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
        log.debug("Mouse Actions Local Memory size: " + this.localMemory.size());
        this.localMemory.add(netBeamsMouseInfo);
        if (this.localMemory.size() <= 30) {
            log.debug("Tracking updates since there are less than 30 items on the producer local memory...");
        } else {
            log.debug("Sending the Items to the Messages Queues...");
            this.send();
        }
    }
    
    private void send() {
        MouseActionsContainer data = this.makeMouseActionsContainerFromMouseActions();

        // Create the message
        String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        
        log.debug("Packaging Mouse Actions to be sent from " + localIPAddress);
        log.debug("Total number of Mouse Actions: " + data.getMouseAction().size());
        
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(
                MouseActionDSPComponent.class.getName(), localIPAddress, data.getContentContextForJAXB());
        
        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("DSPWebLogger",
                System.getenv("WIRE.TRANSPORT.SERVER"), null);
        // Note that the consumer has the Wire Transport default DEMO IP as the destination.
        
        log.debug("Packaging Mouse Actions to be sent to "
                + consumer.getComponentLocator().getNodeAddress().getValue());
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);

        try {
            Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, data, ObjectFactory.class);
            
            // Always check if there is a broker available
            MessageBrokerAccessor messageBroker = this.dspContext.getDataBroker();
            if(messageBroker != null){
                    messageBroker.send(message);
            }else{
                    log.debug("Message broker not available");
            }
            
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (DSPException e) {
            log.error(e.getMessage(), e);
        }
    }
        
    /**
     * @return the MouseActionsContainer for the given set of mouseActions, which is passed in the instantiation of
     *         an instance of this DSPMouseWorker. It also clears the previous state.
     */
    private MouseActionsContainer makeMouseActionsContainerFromMouseActions() {
        MouseActionsContainer actionsContainer = new MouseActionsContainer();
        for (NetBeamsMouseInfo mouseInfoCollected : this.localMemory) {
            MouseAction mc = new MouseAction();
            mc.setButton(ButtonName.fromValue(mouseInfoCollected.getMouseButton().toString().replace("_BUTTON", "")));
            mc.setEvent(EventName.fromValue(mouseInfoCollected.getMouseAction().toString()));
            mc.setX((int) mouseInfoCollected.getPointClicked().getX());
            mc.setY((int) mouseInfoCollected.getPointClicked().getY());

            actionsContainer.getMouseAction().add(mc);
        }
        this.localMemory.clear();
        return actionsContainer;
    }
}