package org.netbeams.dsp.example.miceaction.model;

import java.awt.Point;

/**
 * The netBEAMS mouse action information. It captures all the information of the mouse
 * actions performed in a defined screen. It can be a window, panel, etc. Dimentions and 
 * relative points are related to the screen itself.
 * @author marcello
 *
 */
public final class NetBeamsMouseInfo {

	/**
	 * The (x,y) point coordinates relative to the screen where the action occurred.
	 */
	private Point pointClicked;
	/**
	 * The message produced by the producer, describing the action that happened with the mouse
	 */
	private String message;
	/**
	 * The type of mouse button based on the MouseEvent.getButton() value
	 */
	private NetBeamsMouseButtonEnum mouseButton;
	
	/**
	 * Creates a new NetBeamsMouseInfo based on a given point, button type and message.
	 * @param point is the (x,y) coordinates related to the frame/panel shown by the producer.
	 * @param button is the type of button used.
	 * @param message is a description of the event.
	 */
	private NetBeamsMouseInfo(Point point, NetBeamsMouseButtonEnum button, String message) {
		this.pointClicked = point;
		this.mouseButton = button;
		this.message = message;
	}
	
	/**
	 * Factory method used to make new instances of the NetBeamsMouseInfo
	 * @param whereClicked is the position where the click of the mouse occurred.
	 * @param button the type of the button @see {@link NetBeamsMouseButtonEnum}
	 * @param message is the complete that describes the action
	 * @return a new NetBeamsMouseInfo instance based on the input specified.
	 */
	public static NetBeamsMouseInfo makeNetBeamsMouseInfo(Point whereClicked, NetBeamsMouseButtonEnum button, String message) {
		return new NetBeamsMouseInfo(whereClicked, button, message);
	}

	/**
	 * @return the point of the mouse action
	 */
	public Point getPointClicked() {
		return this.pointClicked;
	}

	/**
	 * @return The description of the event
 	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return The type of the button captured
	 */
	public NetBeamsMouseButtonEnum getMouseButton() {
		return this.mouseButton;
	}
	
	@Override
	public String toString() {
		return this.mouseButton + " " + this.message;
	}
}