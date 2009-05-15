package org.netbeams.dsp.demo.mouseactions.controller;

import org.netbeams.dsp.demo.mouseactions.model.NetBeamsMouseInfo;

public class NetBeamsMouseSystemOutput implements NetBeamsMouseListener {

	public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
		System.out.println("Mouse System Out Observer...");
		System.out.println(netBeamsMouseInfo);
		System.out.println("");
	}

}
