package org.netbeams.dsp.demo.mouseactions.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class BlankArea extends JLabel {
    
	/**
	 * Serializable version number
	 */
	private static final long serialVersionUID = 1L;
	
	private Dimension minSize = new Dimension(100, 50);

    public BlankArea(Color color) {
    	setText("NetBeams.org OSGi Mouse Demo... Just move your mouse here or/and click me");
        setBackground(color);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    public Dimension getMinimumSize() {
        return minSize;
    }

    public Dimension getPreferredSize() {
        return minSize;
    }
}