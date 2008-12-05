package org.netbeams.dsp.demo.mouseactions.model.dsp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.demo.mouseactions.controller.DSPMouseActionsProducer;
import org.netbeams.dsp.demo.mouseactions.controller.NetBeamsMouseCollector;
import org.netbeams.dsp.demo.mouseactions.view.NetBeamsMouseActionDemo;
import org.netbeams.dsp.demo.stocks.producer.StockProducer;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.Log;

/**
 * MouseActionDSPComponent is the DSP component for the mouse actions. It starts a new JFrame
 * where the user can interact with the area, and consequently data will be generated and saved into a local
 * memory with a certain number. 
 * 
 * In order to acquire the data generated, the client to this API must register different MouseActionListeners
 * to the data producer, in this case, the JFrame instance. For demo purposes, this producer adds 3 different
 * listeners, including one for the DSP component and others.
 * 
 * @author marcello de sales <marcello.sales@gmail.com>
 *
 */
public class MouseActionDSPComponent implements DSPComponent  {
    /**
     * Part of the DSP component
     */
    private static final ComponentDescriptor componentDescriptor;
    /**
     * Component type defined for the DSP framework to identify this message 
     */
    public final static String COMPONENT_TYPE = "org.netbeams.dsp.example.miceaction";
    /**
     * Defines the type of payload that will be sent. Please refer to the XML packages with the defined value
     */
    public final static String MSG_CONTENT_TYPE_STOCK_TICK = "mouse.action";
    
    static {
        componentDescriptor = new ComponentDescriptor();
        
        Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
        Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
        // MouseAction
        MessageCategory messageCategory = 
            new MessageCategory(StockProducer.class.getName(), MSG_CONTENT_TYPE_STOCK_TICK);
        
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
     * Instance of the DSP Mouse Memo, a JFrame implementation of a mouse observer. It's used to capture the 
     * mouse interactions with an area on the screen. The data generated from this component is then sent to
     * the DSP component via the DSPWorker executor
     */
    private NetBeamsMouseActionDemo demo;
    /**
     * The JFrameExecutor is responsible for executing the JFrame in different classloaders, and for the OSGi
     * context, it helps creating a new Swing thread.
     */
    private NetBeamsMouseActionDemo.JFrameExecutor jfexec;
    /**
     * The ExecutorService instance is responsible for starting and stopping the execution of the data collection.
     * NOTE that this is a strongly-coupled reference to the ExecutorService from 
     * DSPMouseActionsHttpServerCollectorClient.scheduler, which is just an instance of 
     * Executors.newSingleThreadScheduledExecutor()
     */
    private ExecutorService dspDataSend;

    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    @Override
    public ComponentDescriptor getComponentDescriptor() {
        return componentDescriptor;
    }

    @Override
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        Log.log(COMPONENT_TYPE + ".initComponent()");

        this.context = context;
        this.componentNodeId = componentNodeId;
    }

    @Override
    public String getComponentNodeId() {
        return componentNodeId;
    }

    @Override
    public void deliver(Message request) throws DSPException {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
    }

    @Override
    public Message deliverWithReply(Message message) throws DSPException {
        // TODO How we should handle an invokation to this method when the
        // component is not a consumer?
        return null;
    }

    @Override
    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startComponent() {
        Log.log("MouseAction.startComponent()");
        
        this.demo = new NetBeamsMouseActionDemo();
        this.jfexec = new NetBeamsMouseActionDemo.JFrameExecutor(this.demo);
        
        javax.swing.SwingUtilities.invokeLater(this.jfexec);
        this.jfexec.addWindowListener(new WindowAdapter () {
            public void windowClosing(WindowEvent e) {
                  try {
                      System.out.println("Closing the mouse demo window... Stopping the component");
                      
                      stopComponent();
                      
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
              }
        });
        
        NetBeamsMouseCollector mc = NetBeamsMouseCollector.makeNewNetBeamsMouseCollector();
        demo.addNetBeamsMouseListener(mc);
        
//      NetBeamsMouseSystemOutput systemOutObserver = new NetBeamsMouseSystemOutput();
//      demo.addNetBeamsMouseListener(systemOutObserver);
        
        DSPMouseActionsProducer hs = new DSPMouseActionsProducer(context, this);
        demo.addNetBeamsMouseListener(hs);
        this.dspDataSend = DSPMouseActionsProducer.scheduler;
    }

    @Override
    public void stopComponent() {
        Log.log("MouseAction.stopComponent()");
        this.dspDataSend.shutdown();
    }
}
