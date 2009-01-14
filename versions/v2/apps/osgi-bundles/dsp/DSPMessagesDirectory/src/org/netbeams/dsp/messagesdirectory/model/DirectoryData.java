package org.netbeams.dsp.messagesdirectory.model;

import java.net.URL;
import java.util.UUID;

import org.netbeams.dsp.message.Message;

/**
 * Representation of a given DSP Message in the Messages Directory. It contains references to the DSP message, the URL
 * destination, the state and the messages container identification in which the message will be "wrapped up".
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 * 
 */
public class DirectoryData {

    /**
     * The destination URL of the component that will receive the message
     */
    private URL destinition;
    /**
     * The DSP message to be delivered.
     */
    private Message message;
    /**
     * The message state
     */
    private MessagesQueueState state;
    /**
     * The container ID. This value is only set when the message on the state MessagesQueueState.TRANSMITTED and
     * ACKNOWLEDGED.
     */
    private UUID containerId;

    /**
     * Creates a new Directory Data composed by the given component destination URL and the dsp Message, as well as in
     * the QUEUED state.
     * 
     * @param componentDestinition is the URL of the DSP component.
     * @param dspMessage is the DSP message to be sent.
     */
    public DirectoryData(URL componentDestinition, Message dspMessage) {
        this.destinition = componentDestinition;
        this.message = dspMessage;
        this.state = MessagesQueueState.QUEUED;
    }

    /**
     * Factory method for the DirectoryData.
     * 
     * @param componentDestinition is the URL of the component.
     * @param dspMessage is the dspMessage.
     * @return a new instance of the DirectoryData.
     */
    public static DirectoryData makeNewInstance(URL componentDestinition, Message dspMessage) {
        return new DirectoryData(componentDestinition, dspMessage);
    }

    public MessagesQueueState getState() {
        return this.state;
    }

    /**
     * Whenever a message has been transmitted in a packet.
     */
    public void changeStateToTransmitted() {
        this.state = MessagesQueueState.TRANSMITTED;
    }

    /**
     * Whenever a message has been acknowledged from the server-side.
     */
    public void changeStateToAcknowledged() {
        this.state = MessagesQueueState.ACKNOWLEDGED;
    }

    public UUID getContainerId() {
        return this.containerId;
    }

    public void setMessagesContainerId(UUID messagesContainerId) {
        this.containerId = messagesContainerId;
    }

    public URL getDestinition() {
        return this.destinition;
    }

    public Message getMessage() {
        return this.message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DirectoryData) {
            return this.destinition.equals(((DirectoryData) obj).destinition)
                    && this.message.getMessageID().equals(((DirectoryData) obj).message.getMessageID());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.destinition.hashCode() + this.state.hashCode() + this.containerId.hashCode()
                + this.message.hashCode();
    }
}
