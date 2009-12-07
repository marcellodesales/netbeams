package org.netbeams.dsp.persistence.model.component.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.persistence.model.location.SensorLocation;

/**
 * PersistentMessageUnit is the persistent message unit saved on a KeyValeyPairs Linked Hash Map. It is a reference to a
 * DSP message and its current state in the persistence layer. Furthermore, it's related to a Sensor Location.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public class PersistentMessageUnit {

    public static final String DEFAULT_DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    /**
     * The default date/time formatter
     */
    public static final DateFormat dateTimeFormat = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
    /**
     * It's the instance of the DSP Message
     */
    private Message dspMessage;
    /**
     * It's the location information about a sensor
     */
    private SensorLocation sensorLocation;
    /**
     * It's the state of the persistent message
     */
    private PersistentMessageState state;
    /**
     * It's the date and time information when the data was collected into the data store host.
     */
    protected Calendar collectionDateTime;
    /**
     * The content type is the name of the class extracted from the payload of the message.
     */
    private String messageContentType;
    /**
     * The message type is the name of the class extracted from the message class.
     */
    private String messageType;

    /**
     * Builds a new PersistentMessageUnit with a given dspMessage. The unit is created under the state of TRANSIENT.
     * 
     * @param dspMessage is the DSP message
     * @param sensorLocation is the location of the sensor
     * @param collectionTime is the current time when the message was collected.
     */
    public PersistentMessageUnit(Message dspMessage, SensorLocation sensorLocation, Calendar collectionTime) {
        this.dspMessage = dspMessage;
        this.sensorLocation = sensorLocation;
        this.state = PersistentMessageState.TRANSIENT;
        this.collectionDateTime = collectionTime;
    }

    /**
     * @return the DSP message that was collected from a DSP component.
     */
    public Message getDspMessage() {
        return this.dspMessage;
    }

    /**
     * @return the sensor location that produced the message.
     */
    public SensorLocation getSensorLocation() {
        return this.sensorLocation;
    }

    /**
     * @return the current state of the persistent Message.
     */
    public PersistentMessageState getState() {
        return this.state;
    }

    /**
     * @return the local host calendar information about the date and time of the collection
     */
    public String getCollectionDateTime() {
        return new SimpleDateFormat(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT).format(this.collectionDateTime
                .getTime());
    }

    /**
     * @return The collection date formatted using the DEFAULT_DATE_FORMAT constant.
     */
    public String getCollectionDateString() {
        return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(this.collectionDateTime.getTime());
    }

    /**
     * @return The collection time formatted using the DEFAULT_TIME_FORMAT constant.
     */
    public String getCollectionTimeString() {
        return new SimpleDateFormat(DEFAULT_TIME_FORMAT).format(this.collectionDateTime.getTime());
    }

    /**
     * Flags the persistent message as flushed into the Database store.
     */
    public void setStateToFlushed() {
        this.state = PersistentMessageState.FLUSHED;
    }

    /**
     * @return the type of the message, which is the name of the message class.
     */
    public String getMessageType() {
        if (this.messageType == null) {
            String[] simpleNames = this.getDspMessage().getClass().getSimpleName().split("\\.");
            this.messageType = simpleNames[simpleNames.length - 1];
        }
        return this.messageType;
    }

    /**
     * @return the name of the content type, which is the name of the class on the body of the message.
     */
    public String getMessageContentType() {
        if (this.messageContentType == null) {
            String[] simpleNames = this.getDspMessage().getBody().getAny().getClass().getSimpleName().split("\\.");
            this.messageContentType = simpleNames[simpleNames.length - 1];
        }
        return this.messageContentType;
    }

    /**
     * @return the milliconds values of the collection time (transaction time) when the message was stored.
     */
    public long getCollectionTimeMilliseconds() {
        return this.collectionDateTime.getTimeInMillis();
    }

    public String toString() {
        return "Persistence Unit: "+this.getDspMessage().getMessageID() + " (" + this.getMessageType() + ") "; 
    }
}
