package org.netbeams.dsp.demo.mouseactions.model;

import java.awt.Point;

/**
 * The netBEAMS mouse action information. It captures all the information of the mouse actions performed in a defined
 * screen. It can be a window, panel, etc. Dimentions and relative points are related to the screen itself.
 * 
 * @author marcello
 * 
 */
public final class NetBeamsMouseInfo {

    private long whenOccurred;
    /**
     * The (x,y) point coordinates relative to the screen where the action occurred.
     */
    private Point pointClicked;
    /**
     * The type of mouse button based on the MouseEvent.getButton() value
     */
    private NetBeamsMouseButtonEnum mouseButton;
    /**
     * Defines the action performed in the mouse
     */
    private NetBeamsMouseActionEnum mouseAction;

    /**
     * Creates a new NetBeamsMouseInfo based on a given point, button type and message.
     * 
     * @param point is the (x,y) coordinates related to the frame/panel shown by the producer.
     * @param button is the type of button used.
     * @param event is a description of the event.
     * @param whenOccurred is a description of the event.
     */
    private NetBeamsMouseInfo(NetBeamsMouseActionEnum action, NetBeamsMouseButtonEnum button, Point point,
            long whenOccurred) {
        this.pointClicked = point;
        this.mouseButton = button;
        this.mouseAction = action;
        this.whenOccurred = whenOccurred;
    }

    public long getWhenOccurred() {
        return whenOccurred;
    }

    /**
     * Factory method used to make new instances of the NetBeamsMouseInfo
     * 
     * @param whereClicked is the position where the click of the mouse occurred.
     * @param button the type of the button @see {@link NetBeamsMouseButtonEnum}
     * @param message is the complete message that describes the action
     * @param whenOccurredMilliseconds is the milliseconds representation of the time of the sampling
     * @return a new NetBeamsMouseInfo instance based on the input specified.
     */
    public static NetBeamsMouseInfo makeNetBeamsMouseInfo(NetBeamsMouseActionEnum action,
            NetBeamsMouseButtonEnum button, Point whereClicked, long whenOccurredMilliseconds) {
        return new NetBeamsMouseInfo(action, button, whereClicked, whenOccurredMilliseconds);
    }

    /**
     * @return the point of the mouse action
     */
    public Point getPointClicked() {
        return this.pointClicked;
    }

    /**
     * @return The description of the event through the action performed
     */
    public NetBeamsMouseActionEnum getMouseAction() {
        return this.mouseAction;
    }

    /**
     * @return The type of the button captured
     */
    public NetBeamsMouseButtonEnum getMouseButton() {
        return this.mouseButton;
    }

    @Override
    public String toString() {
        return this.mouseButton + " " + this.mouseAction + " at " + pointClicked.toString();
    }
}
