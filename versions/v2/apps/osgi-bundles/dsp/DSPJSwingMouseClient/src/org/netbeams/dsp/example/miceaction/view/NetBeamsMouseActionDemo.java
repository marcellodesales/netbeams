package org.netbeams.dsp.example.miceaction.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseActionEnum;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseButtonEnum;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseInfo;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseListener;

public class NetBeamsMouseActionDemo extends JPanel implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;

	private BlankArea blankArea;

	private JTextArea textArea;

	private static final String NEWLINE = "\n";

	/**
	 * The NetBeams Mouse listeners, who are interested about the mouse events on the JPanel.
	 */
	private List<NetBeamsMouseListener> netBeamsMouseListener;

	public NetBeamsMouseActionDemo() {
		super(new GridBagLayout());
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
		JScrollPane scrollPane = new JScrollPane(this.textArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
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
		this.netBeamsMouseListener.add(listener);
	}

	/**
	 * Notifies all NetBeams components interested in the mouse actions.
	 * @param description the description of the event
	 * @param me the mouse event that fired the notification
	 */
	private void notifyNetBeamsMouseListeners(String description, MouseEvent me) {
		NetBeamsMouseInfo nbmi = NetBeamsMouseInfo.makeNetBeamsMouseInfo(me.getPoint(), 
				                   NetBeamsMouseButtonEnum.getInstanceByValue(me.getButton()), 
				                   description);
		for (NetBeamsMouseListener nbml : this.netBeamsMouseListener) {
			nbml.trackMouseActionUpdate(nbmi);
		}
	}
	
	private void eventOutput(String eventDescription, MouseEvent e) {
		textArea.append(eventDescription + " detected on mouse button "+ e.getButton() + 
				         e.getComponent().getClass().getName() + "." + NEWLINE);
		textArea.setCaretPosition(textArea.getDocument().getLength());
		this.notifyNetBeamsMouseListeners(eventDescription, e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.PRESSED + " (# of clicks: " + e.getClickCount()
				+ ") on (" + e.getX() + " , " + e.getY() + ")", e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.RELEASED + " at (" + e.getX() + " , " + e.getY() + ")", e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.ENTERED + " at (" + e.getX() + " , " + e.getY()
				+ ")", e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.EXITED + " at (" + e.getX() + " , " + e.getY()
				+ ")", e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.CLICKED + " (# of clicks: " + e.getClickCount()
				+ ") on (" + e.getX() + " , " + e.getY() + ")", e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.DRAGGED + " at " + e.getX() + " , " + e.getY() + ")", e);		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.eventOutput(NetBeamsMouseActionEnum.MOVED + " at (" + e.getX() + " , " + e.getY() + ")", e);
	}
	
	public static class JFrameExecutor extends JFrame implements Runnable {

		private NetBeamsMouseActionDemo demo;

		public JFrameExecutor(NetBeamsMouseActionDemo demo) {
			super("NetBeams Mouse Click Demo 0.1");
			this.demo = demo;
			
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// Make sure we have nice window decorations.
			JFrame.setDefaultLookAndFeelDecorated(true);
			// Create and set up the content pane.
			this.demo.setOpaque(true); // content panes must be opaque
			this.setContentPane(this.demo);
		}
		
		@Override
		public void run() {
			this.createAndShowGUI();
		}
		
		/**
		 * Create the GUI and show it. For thread safety, this method should be
		 * invoked from the event-dispatching thread.
		 */
		private void createAndShowGUI() {
			// Display the window.
			this.pack();
			this.setVisible(true);
		}
	}

	public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
		NetBeamsMouseActionDemo demo = new NetBeamsMouseActionDemo();
		NetBeamsMouseActionDemo.JFrameExecutor ex = new NetBeamsMouseActionDemo.JFrameExecutor(demo);
	    javax.swing.SwingUtilities.invokeLater(ex);
		ex.addWindowListener(new WindowAdapter () {
		      public void windowClosing(WindowEvent e) {
		          System.out.println("Closing...");
		      }
		});
	}
}