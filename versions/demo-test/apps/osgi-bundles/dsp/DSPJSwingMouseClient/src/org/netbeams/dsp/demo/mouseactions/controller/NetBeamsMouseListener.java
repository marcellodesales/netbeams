package org.netbeams.dsp.demo.mouseactions.controller;

import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;

/**
 * This is the netBEAMS mouse listener interface that tracks all the events regarding to the
 * mouse actions performed in a screen.
 * @author marcello.sales@gmail.com
 */
public interface NetBeamsMouseListener {

	/**
	 * Tracks the mouse action updates on the screen. It must create the NetBeamsMouseInfo
	 * to be sent to the listeners.
	 * @param netBeamsMouseInfo is the information about the mouse event.
	 * @see NetBeamsMouseInfo
	 */
	public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo);
}
