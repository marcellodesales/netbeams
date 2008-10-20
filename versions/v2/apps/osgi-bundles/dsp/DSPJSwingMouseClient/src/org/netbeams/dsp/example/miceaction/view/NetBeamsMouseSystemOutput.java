package org.netbeams.dsp.example.miceaction.view;

import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseInfo;
import org.netbeams.dsp.example.miceaction.model.NetBeamsMouseListener;

public class NetBeamsMouseSystemOutput implements NetBeamsMouseListener {

	@Override
	public void trackMouseActionUpdate(NetBeamsMouseInfo netBeamsMouseInfo) {
		System.out.println("Mouse System Out Observer...");
		System.out.println(netBeamsMouseInfo);
		System.out.println("");
	}

}
