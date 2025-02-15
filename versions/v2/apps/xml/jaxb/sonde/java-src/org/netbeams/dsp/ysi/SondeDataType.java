//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.03.04 at 06:20:22 PM PST 
//


package org.netbeams.dsp.ysi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <p>Java class for sondeDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sondeDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Time" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Temp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SpCond" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Cond" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Resist" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Sal" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Press" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Depth" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pH" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="phmV" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ODOSat" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ODOConc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Turbid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Battery" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class SondeDataType {
    
    public static final DateFormat dateTimeFormat = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss");

//    @XmlElement(name = "Date", required = true)
    protected Calendar samplingTimeStamp;
//    @XmlElement(name = "Temp", required = true)
    protected Float temp;
//    @XmlElement(name = "SpCond", required = true)
    protected Float spCond;
//    @XmlElement(name = "Cond", required = true)
    protected Float cond;
//    @XmlElement(name = "Resist", required = true)
    protected Float resist;
//    @XmlElement(name = "Sal", required = true)
    protected Float sal;
//    @XmlElement(name = "Press", required = true)
    protected Float press;
//    @XmlElement(name = "Depth", required = true)
    protected Float depth;
//    @XmlElement(name = "pH", required = true)
    protected Float ph;
//    @XmlElement(required = true)
    protected Float phmV;
//    @XmlElement(name = "ODOSat", required = true)
    protected Float odoSat;
//    @XmlElement(name = "ODOConc", required = true)
    protected Float odoConc;
//    @XmlElement(name = "Turbid", required = true)
    protected Float turbid;
//    @XmlElement(name = "Battery", required = true)
    protected Float battery;
    
    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getDateTime() {
        return this.samplingTimeStamp;
    }
    
    /**
     * @param format is an instance of a SimpleDateFormat. If you want to obtain
     * the information about time or date in separate, just build an instance with
     * the parameters you want. The instance used is the dateTime captured.
     * @return the string representation of the dateTime instance based on the formatter.
     */
    public String getDateAsString(SimpleDateFormat format) {
        return format.format(this.samplingTimeStamp.getTime());
    }
    
    public String getDateString() {
        return new SimpleDateFormat("yyyy/MM/dd").format(this.samplingTimeStamp.getTime());
    }
    
    public String getTimeString() {
        return new SimpleDateFormat("HH:mm:ss").format(this.samplingTimeStamp.getTime());
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateTime(String date, String time) {
        this.samplingTimeStamp = Calendar.getInstance();
        try {
            this.samplingTimeStamp.setTime(dateTimeFormat.parse(date + " " + time));
        } catch (ParseException e) {
        }
    }

    /**
     * Gets the value of the temp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getTemp() {
        return temp;
    }

    /**
     * Sets the value of the temp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemp(Float value) {
        this.temp = value;
    }

    /**
     * Gets the value of the spCond property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getSpCond() {
        return spCond;
    }

    /**
     * Sets the value of the spCond property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpCond(Float value) {
        this.spCond = value;
    }

    /**
     * Gets the value of the cond property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getCond() {
        return cond;
    }

    /**
     * Sets the value of the cond property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCond(Float value) {
        this.cond = value;
    }

    /**
     * Gets the value of the resist property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getResist() {
        return resist;
    }

    /**
     * Sets the value of the resist property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResist(Float value) {
        this.resist = value;
    }

    /**
     * Gets the value of the sal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getSal() {
        return sal;
    }

    /**
     * Sets the value of the sal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSal(Float value) {
        this.sal = value;
    }

    /**
     * Gets the value of the press property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getPress() {
        return press;
    }

    /**
     * Sets the value of the press property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPress(Float value) {
        this.press = value;
    }

    /**
     * Gets the value of the depth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getDepth() {
        return depth;
    }

    /**
     * Sets the value of the depth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepth(Float value) {
        this.depth = value;
    }

    /**
     * Gets the value of the ph property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getPH() {
        return ph;
    }

    /**
     * Sets the value of the ph property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPH(Float value) {
        this.ph = value;
    }

    /**
     * Gets the value of the phmV property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getPhmV() {
        return phmV;
    }

    /**
     * Sets the value of the phmV property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhmV(Float value) {
        this.phmV = value;
    }

    /**
     * Gets the value of the odoSat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getODOSat() {
        return odoSat;
    }

    /**
     * Sets the value of the odoSat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setODOSat(Float value) {
        this.odoSat = value;
    }

    /**
     * Gets the value of the odoConc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getODOConc() {
        return odoConc;
    }

    /**
     * Sets the value of the odoConc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setODOConc(Float value) {
        this.odoConc = value;
    }

    /**
     * Gets the value of the turbid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getTurbid() {
        return turbid;
    }

    /**
     * Sets the value of the turbid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTurbid(Float value) {
        this.turbid = value;
    }

    /**
     * Gets the value of the battery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Float getBattery() {
        return battery;
    }

    /**
     * Sets the value of the battery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBattery(Float value) {
        this.battery = value;
    }

    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<sondeData");
        if (this.samplingTimeStamp != null) {
            builder.append(" samplingTimeStamp=\"" + dateTimeFormat.format (this.samplingTimeStamp.getTime()) + "\"");
        }
        builder.append(">");
        builder.append("<Temp>" + this.temp + "</Temp>");
        builder.append("<SpCond>" + this.spCond + "</SpCond>");
        builder.append("<Cond>" + this.cond + "</Cond>");
        builder.append("<Resist>" + this.resist + "</Resist>");
        builder.append("<Sal>" + this.sal + "</Sal>");
        builder.append("<Press>" + this.press + "</Press>");
        builder.append("<Depth>" + this.depth + "</Depth>");
        builder.append("<pH>" + this.ph + "</pH>");
        builder.append("<phmV>" + this.phmV + "</phmV>");
        builder.append("<ODOSat>" + this.odoSat + "</ODOSat>");
        builder.append("<ODOConc>" + this.odoConc + "</ODOConc>");
        builder.append("<Turbid>" + this.turbid + "</Turbid>");
        builder.append("<Battery>" + this.battery + "</Battery>");
        builder.append("</sondeData>");
        return builder.toString();
    }

}
