package org.netbeams.dsp.demo.mouseactions.model.dsp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.demo.mouseactions.controller.DSPMouseActionsProducer;
import org.netbeams.dsp.demo.mouseactions.view.NetBeamsMouseActionDemo;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.messagesdirectory.controller.DSPMessagesDirectory;

/**
 * MouseActionDSPComponent is the DSP component for the mouse actions. It starts a new JFrame where the user can
 * interact with the area, and consequently data will be generated and saved into a local memory with a certain number.
 * 
 * In order to acquire the data generated, the client to this API must register different MouseActionListeners to the
 * data producer, in this case, the JFrame instance. For demo purposes, this producer adds 3 different listeners,
 * including one for the DSP component and others.
 * 
 * @author marcello de sales <marcello.sales@gmail.com>
 * 
 */
public class MouseActionDSPComponent implements DSPComponent {

    private static final Logger log = Logger.getLogger(MouseActionDSPComponent.class);

    /**
     * Part of the DSP component
     */
    private static final ComponentDescriptor componentDescriptor;
    /**
     * Component type defined for the DSP framework to identify this message
     */
    public final static String COMPONENT_TYPE = "org.netbeams.dsp.demo.mouseactions";
    /**
     * Defines the type of payload that will be sent. Please refer to the XML packages with the defined value
     */
    public final static String MSG_CONTENT_TYPE_MOUSE_ACTIONS = "mouse.actions";

    static {
        componentDescriptor = new ComponentDescriptor();

        Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
        Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
        // MouseAction
        MessageCategory messageCategory = new MessageCategory(MouseActionDSPComponent.class.getName(),
                MSG_CONTENT_TYPE_MOUSE_ACTIONS);

        producedMessageCategories.add(messageCategory);
        componentDescriptor.setMsgCategoryProduced(producedMessageCategories);
        componentDescriptor.setMsgCategoryConsumed(consumedMessageCategories);
    }
    /**
     * This is the default DSP context defined by the OSGi framework.
     */
    private DSPContext context;
    /**
     * Defines the runtime component node ID where the instance will be running
     */
    private String componentNodeId;
    /**
     * Instance of the DSP Mouse Memo, a JFrame implementation of a mouse observer. It's used to capture the mouse
     * interactions with an area on the screen. The data generated from this component is then sent to the DSP component
     * via the DSPWorker executor
     */
    private NetBeamsMouseActionDemo demo;
    /**
     * The JFrameExecutor is responsible for executing the JFrame in different classloaders, and for the OSGi context,
     * it helps creating a new Swing thread.
     */
    private NetBeamsMouseActionDemo.JFrameExecutorForMouseActions jfexec;
    /**
     * The reference to the messages queue DSP component.
     */
    private DSPMessagesDirectory messagesQueueComp;

    /**
     * Creates a new instance of the MouseActionDSPComponent with the given reference to the Messages Queue
     * 
     * @param messagesQueue is the service where the DSP component needs to send messages.
     */
    public MouseActionDSPComponent(DSPMessagesDirectory messagesQueue) {
        log.debug("Instantiating the DSPMouseActions component");
        this.messagesQueueComp = messagesQueue;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    public ComponentDescriptor getComponentDescriptor() {
        return componentDescriptor;
    }

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        log.info(COMPONENT_TYPE + ".initComponent()");

        this.context = context;
        this.componentNodeId = componentNodeId;
    }

    public String getComponentNodeId() {
        return componentNodeId;
    }

    public void deliver(Message request) throws DSPException {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
    }

    public Message deliverWithReply(Message message) throws DSPException {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
        return null;
    }

    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    public void startComponent() {
        log.info("MouseAction.startComponent()");

        log.debug("Trying to start the mouse actions demo application...");
        this.demo = new NetBeamsMouseActionDemo();
        this.jfexec = new NetBeamsMouseActionDemo.JFrameExecutorForMouseActions(this.demo);

        log.debug("Ready to start the Mouse Actions User Interface...");

        javax.swing.SwingUtilities.invokeLater(this.jfexec);
        this.jfexec.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    log.info("Closing the mouse actions demo window... Stopping the component");

                    stopComponent();

                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        });

        // NetBeamsMouseCollector mc = NetBeamsMouseCollector.makeNewNetBeamsMouseCollector();
        // demo.addNetBeamsMouseListener(mc);

        // NetBeamsMouseSystemOutput systemOutObserver = new NetBeamsMouseSystemOutput();
        // demo.addNetBeamsMouseListener(systemOutObserver);

        DSPMouseActionsProducer hs = new DSPMouseActionsProducer(this.messagesQueueComp);
        this.demo.addNetBeamsMouseListener(hs);

        log.debug("Mouse Actions demo UI is ready to receive interaction...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeams.dsp.DSPComponent#stopComponent()
     */
    public void stopComponent() {
        log.info("MouseAction.stopComponent()");
        if (this.demo != null && this.demo.isShowing()) {
            this.demo.setVisible(false);
            this.demo = null;
        }
    }
}
