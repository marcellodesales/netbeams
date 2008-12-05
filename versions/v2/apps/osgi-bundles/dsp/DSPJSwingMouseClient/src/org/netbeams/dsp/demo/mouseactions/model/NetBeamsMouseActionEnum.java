package org.netbeams.dsp.demo.mouseactions.model;

/**
 * Captures the mouse actions on a given environment such as a windows
 * @author marcello
 *
 */
public enum NetBeamsMouseActionEnum {

	/**
	 * Whenever a mouse button is clicked 
	 */
	CLICKED, 
	/**
	 * Whenever the mouse is moved with a button clicked 
	 */
	DRAGGED, 
	/**
	 * Whenever the mouse enters an observed area
	 */
	ENTERED, 
	/**
	 * Whenever the mouse leaves an observed area
	 */
	EXITED, 
	/**
	 * Whenever the mouse moves around an observed area
	 */
	MOVED, 
	/**
	 * Whenever a mouse button is pressed
	 */
	PRESSED, 
	/**
	 * Whenever a mouse button is released
	 */
	RELEASED;
	
	public String getHumanReadable() {
		switch (this) {
			case CLICKED: return "Mouse clicked";
			case DRAGGED: return "Mouse dragged";
			case ENTERED: return "Mouse entered";
			case EXITED: return "Mouse exited";
			case MOVED: return "Mouse moved";
			case PRESSED: return "Mouse pressed";
			case RELEASED: return "Mouse released";
			default: return null;
		}
	}
}