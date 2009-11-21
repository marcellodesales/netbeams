package org.netbeams.dsp.demo.mouseactions.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.netbeams.dsp.demo.mouseactions.controller.NetBeamsMouseListener;
import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseActionEnum;
import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseButtonEnum;
import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;

/**
 * NetBeams Mouse Action demo is a Swing based application that captures the mouse motions
 * on a JPanel screen.
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class NetBeamsMouseActionDemo extends JPanel implements MouseListener, MouseMotionListener {

    /**
     * Local logger for the demo
     */
    private static final Logger log = Logger.getLogger(NetBeamsMouseActionDemo.class);
    /**
     * Serial number of the class 
     */
    private static final long serialVersionUID = 1L;
    /**
     * Black area used to capture the mouse actions
     */
    private BlankArea blankArea;
    /**
     * The text area of the application.
     */
    private JTextArea textArea;
    /**
     * New line character for the output
     */
    private static final String NEWLINE = "\n";
    /**
     * The NetBeams Mouse listeners, who are interested about the mouse events on the JPanel.
     */
    private List<NetBeamsMouseListener> netBeamsMouseListener;

    /**
     * Creates a new MouseActions demo application with the given DSP messages broker reference.
     * 
     * @param messageBroker is the DSP messages broker reference, used to deliver messages to the DSP broker.
     */
    public NetBeamsMouseActionDemo() {
        super(new GridBagLayout());
        log.info("Starting the MouseActions User Interface");

        GridBagLayout gridbag = (GridBagLayout) getLayout();
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;

        c.insets = new Insets(1, 1, 1, 1);
        this.blankArea = new BlankArea(new Color(0.98f, 0.97f, 0.85f));
        gridbag.setConstraints(this.blankArea, c);
        this.add(this.blankArea);

        c.insets = new Insets(0, 0, 0, 0);
        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(this.textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 75));
        gridbag.setConstraints(scrollPane, c);
        this.add(scrollPane);

        // Register for mouse events on blankArea
        this.blankArea.addMouseListener(this);
        this.blankArea.addMouseMotionListener(this);

        this.setPreferredSize(new Dimension(450, 450));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        this.netBeamsMouseListener = new ArrayList<NetBeamsMouseListener>();
    }

    /**
     * Adds a new listener to the NetBeamsMouse events.
     * @param listener is a NetBeamsMouseListener that is interested in the mouse events.
     */
    public void addNetBeamsMouseListener(NetBeamsMouseListener listener) {
        log.debug("Adding a new listener " + listener.getClass().getName());
        this.netBeamsMouseListener.add(listener);
    }

    /**
     * Notifies all NetBeams components interested in the mouse actions.
     * 
     * @param description the full description from the event
     * @param me is the mouse event that trigged the event
     * @param action the action related to the mouse event
     */
    private void notifyNetBeamsMouseListeners(String description, MouseEvent me, NetBeamsMouseActionEnum action) {
        log.debug("Notifying the Mouse Action listeners");
        NetBeamsMouseInfo nbmi = NetBeamsMouseInfo.makeNetBeamsMouseInfo(action, NetBeamsMouseButtonEnum
                .getInstanceByValue(me.getButton()), me.getPoint(), System.currentTimeMillis());
        for (NetBeamsMouseListener nbml : this.netBeamsMouseListener) {
            nbml.trackMouseActionUpdate(nbmi);
        }
    }

    /**
     * Creates an output message for the frame.
     * @param eventDescription is the event description
     * @param e is the AWT event from the mouse.
     * @param action is the action performed. This action is send from a wrapper method.
     */
    private void eventOutput(String eventDescription, MouseEvent e, NetBeamsMouseActionEnum action) {
        String output = eventDescription + " detected on mouse button " + e.getButton()
                + e.getComponent().getClass().getName() + "." + NEWLINE;
        textArea.append(output);
        textArea.setCaretPosition(textArea.getDocument().getLength());
        log.debug(output);
        this.notifyNetBeamsMouseListeners(eventDescription, e, action);
    }

    public void mousePressed(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.CLICKED + " (# of clicks: " + e.getClickCount() + ") on (" + e.getX()
                + " , " + e.getY() + ")", e, NetBeamsMouseActionEnum.CLICKED);
    }

    public void mouseReleased(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.RELEASED + " at (" + e.getX() + " , " + e.getY() + ")", e,
                NetBeamsMouseActionEnum.RELEASED);
    }

    public void mouseEntered(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.ENTERED + " at (" + e.getX() + " , " + e.getY() + ")", e,
                NetBeamsMouseActionEnum.ENTERED);
    }

    public void mouseExited(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.EXITED + " at (" + e.getX() + " , " + e.getY() + ")", e,
                NetBeamsMouseActionEnum.EXITED);
    }

    public void mouseClicked(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.CLICKED + " (# of clicks: " + e.getClickCount() + ") on (" + e.getX()
                + " , " + e.getY() + ")", e, NetBeamsMouseActionEnum.CLICKED);
    }

    public void mouseDragged(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.DRAGGED + " at " + e.getX() + " , " + e.getY() + ")", e,
                NetBeamsMouseActionEnum.DRAGGED);
    }

    public void mouseMoved(MouseEvent e) {
        this.eventOutput(NetBeamsMouseActionEnum.MOVED + " at (" + e.getX() + " , " + e.getY() + ")", e,
                NetBeamsMouseActionEnum.MOVED);
    }

    /**
     * Default executor for the application
     * 
     * @author Marcello de Sales (marcello.sales@gmail.com)
     */
    public static class JFrameExecutorForMouseActions extends JFrame implements Runnable {
        
        /**
         * Serial version
         */
        private static final long serialVersionUID = 1L;
        /**
         * Local logger for the class
         */
        private static final Logger thLog = Logger.getLogger(JFrameExecutorForMouseActions.class);
        /**
         * Instance of the demo Swing application
         */
        private NetBeamsMouseActionDemo demo;

        public JFrameExecutorForMouseActions(NetBeamsMouseActionDemo demo) {
            super("NetBeams Mouse Actions Demo 0.1");
            this.demo = demo;
            
            thLog.info("Starting the Java Swing API for the Mouse Actions...");
            
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // Make sure we have nice window decorations.
            JFrame.setDefaultLookAndFeelDecorated(true);
            // Create and set up the content pane.
            this.demo.setOpaque(true); // content panes must be opaque
            this.setContentPane(this.demo);
        }

        // @Override
        public void run() {
            this.createAndShowGUI();
        }

        /**
         * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching
         * thread.
         */
        private void createAndShowGUI() {
            thLog.debug("Showing the Java Swing Frame for the Mouse Actions demo...");
            // Display the window.
            this.pack();
            this.setVisible(true);
        }
    }
}