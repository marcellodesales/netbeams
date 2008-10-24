package org.netbeams.dsp.platform.management.component.osgi;

import java.util.Comparator;

public class BundleConfigPriorityComparator implements Comparator<BundleConfig> {

	@Override
	public int compare(BundleConfig o1, BundleConfig o2) {
		
		return  o1.getPriority() - o2.getPriority();
	}
	
}
