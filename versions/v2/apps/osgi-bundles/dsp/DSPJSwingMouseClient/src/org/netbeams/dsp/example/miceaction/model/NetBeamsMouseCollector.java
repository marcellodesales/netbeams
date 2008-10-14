package org.netbeams.dsp.example.miceaction.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the data to be processed from the mouse events.
 * @author marcello
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

	@Override
	public void trackMouseActionUpdate(NetBeamsMouseInfo nbMInfo) {
		this.mouseEvents.add(nbMInfo);
		//Send to DSP transfer channel...
		System.out.println("Mouse Event: " + nbMInfo);
	}

}
