package org.netbeams.dsp.demo.mouseactions.controller;

import java.util.ArrayList;
import java.util.List;

import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;

/**
 * Collects the data to be processed from the mouse events.
 * @author Marcello de Sales
 */
public final class NetBeamsMouseCollector implements NetBeamsMouseListener {
	
	/**
	 * The aggregated part of the NetBeamsMouseCollector
	 */
	private List<NetBeamsMouseInfo> mouseEvents;
	
	/**
	 * Builds a new collector, instantiating the list of events
	 */
	private NetBeamsMouseCollector() {
		this.mouseEvents = new ArrayList<NetBeamsMouseInfo>();
	}
	
	/**
	 * @return a new instance of NetBeamsMouseCollector
	 */
	public static NetBeamsMouseCollector makeNewNetBeamsMouseCollector() {
		return new NetBeamsMouseCollector();
	}

	/* (non-Javadoc)
	 * @see org.netbeams.dsp.demo.mouseactions.controller.NetBeamsMouseListener#trackMouseActionUpdate(org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo)
	 */
	public void trackMouseActionUpdate(NetBeamsMouseInfo nbMInfo) {
		this.mouseEvents.add(nbMInfo);
	}

}
