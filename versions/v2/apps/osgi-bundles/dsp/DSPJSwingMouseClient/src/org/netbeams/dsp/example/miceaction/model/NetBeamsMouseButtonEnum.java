package org.netbeams.dsp.example.miceaction.model;

import java.awt.event.MouseEvent;

/**
 * Defines the states of the button, which one was clicked, etc.
 * @author marcello
 *
 */
public enum NetBeamsMouseButtonEnum {

	/**
	 * The left button of the mouse
	 */
	LEFT_BUTTON(MouseEvent.BUTTON1), 
	/**
	 * The center button of the mouse
	 */
	CENTER_BUTTON(MouseEvent.BUTTON1), 
	/**
	 * The right button of the mouse
	 */
	RIGHT_BUTTON(MouseEvent.BUTTON3),
	/**
	 * There's no button pressed, just mouse motion
	 */
	JUST_MOTION(0);
	
	private int buttonValue;
	
	private NetBeamsMouseButtonEnum(int value) {
		this.buttonValue = value;
	}
	
	/**
	 * @return the value related to the MouseEvent.getButton() method.
	 */
	public int getValue() {
		return this.buttonValue;
	}
	
	/**
	 * @param buttonValue is the value of the button based on MouseEvent.getButton()
	 * @return an instance of NetBeamsMouseButton with the given button value based on the
	 * value.
	 */
	public static NetBeamsMouseButtonEnum getInstanceByValue(int buttonValue) {
		for (NetBeamsMouseButtonEnum button : NetBeamsMouseButtonEnum.values()) {
			if (button.buttonValue == buttonValue) {
				return button;
			} 
		} 
		throw new IllegalArgumentException("Value must be in the range of java.awt.event.MouseEvent.getButton() constants");
	}
}
 