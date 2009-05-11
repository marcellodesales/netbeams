package org.netbeams.dsp.persistence.model.component.data;

import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.persistence.model.location.SensorLocation;

/**
 * Defines a property key to be saved on the KVM data store. The key is composed by a location information from the
 * sensor and the identification of the value. s For instance, the IP address and the component content type could
 * uniquely identify the data.
 * 
 * 192.168.10.101:org.netbeams.dsp.ysi
 * 
 * @author marcello
 * 
 */
public class PropertyKey {

    /**
     * It's the sensor location
     */
    private SensorLocation sensorLocation;
    /**
     * It's the message containing a message content.
     */
    private Message dspMessage;

    /**
     * Creates a new property key based on the given sensor location and the dsp message.
     * 
     * @param sensorLocation
     * @param dspMessage
     */
    public PropertyKey(SensorLocation sensorLocation, Message dspMessage) {
        this.sensorLocation = sensorLocation;
        this.dspMessage = dspMessage;
    }
    
    /**
     * Creates a new property key based on the given persistent Message unit.
     * @param persistentMessage
     */
    public PropertyKey(PersistentMessageUnit persistentMessage) {
        this.sensorLocation = persistentMessage.getSensorLocation();
        this.dspMessage = persistentMessage.getDspMessage();
    }

    @Override
    public int hashCode() {
        return 30 * this.sensorLocation.getIpAddress().hashCode()
                + 31 * this.dspMessage.getMessageID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyKey) {
            return ((PropertyKey) obj).sensorLocation.getIpAddress().equals(this.sensorLocation.getIpAddress())
                    && ((PropertyKey) obj).dspMessage.getMessageID().equals(
                            this.dspMessage.getMessageID());
        } else
            return false;
    }

    @Override
    public String toString() {
        return this.sensorLocation.getIpAddress() + ":" + this.dspMessage.getContentType();
    }
}
