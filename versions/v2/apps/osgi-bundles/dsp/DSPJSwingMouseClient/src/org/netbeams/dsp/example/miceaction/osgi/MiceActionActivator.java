package org.netbeams.dsp.example.miceaction.osgi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseCollector;
import org.netbeams.dsp.example.miceaction.view.NetBeamsMouseActionDemo;
import org.netbeams.dsp.example.miceaction.view.NetBeamsMouseSystemOutput;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class MiceActionActivator implements BundleActivator{

	public static BundleContext bc;
	
	private NetBeamsMouseActionDemo demo;
	
	private NetBeamsMouseActionDemo.JFrameExecutor jfexec;
	
	private class BundleJFrameWindowAdapter extends WindowAdapter {
		
		private BundleContext bc;
		
		public BundleJFrameWindowAdapter(BundleContext bc) {
			this.bc = bc;
		}
		
		public void windowClosing(WindowEvent e) {
	          try {
	        	  System.out.println("Closing the demo window...");
	        	  stop(this.bc);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	      }
	};
	
	@Override
	public void start(BundleContext bc) throws Exception {
		MiceActionActivator.bc = bc;

		this.demo = new NetBeamsMouseActionDemo();
		this.jfexec = new NetBeamsMouseActionDemo.JFrameExecutor(this.demo);
		
	    javax.swing.SwingUtilities.invokeLater(this.jfexec);
	    this.jfexec.addWindowListener(new BundleJFrameWindowAdapter(MiceActionActivator.bc));
		
		NetBeamsMouseCollector mc = NetBeamsMouseCollector.makeNewNetBeamsMouseCollector();
		demo.addNetBeamsMouseListener(mc);
		
		NetBeamsMouseSystemOutput systemOutObserver = new NetBeamsMouseSystemOutput();
		demo.addNetBeamsMouseListener(systemOutObserver);
	}

	@Override
	public void stop(BundleContext bc) throws Exception {
		this.demo = null;
		if (this.jfexec.isVisible()) {
			this.jfexec.dispose();
		}
		bc = null;
		System.out.println("Bundle should be stopped now...");
	}

}
