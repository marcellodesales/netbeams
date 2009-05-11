package org.netbeams.dsp.persistence.model.location;

/**
 * Sensor location defines the position of a sensor based on different information such as IP address and Geo location.
 * 
 * @author Marcello de Sales
 */
public class SensorLocation {

    /**
     * It's the latitude location of the sensor device
     */
    private Float latitude;
    /**
     * It's the longitude location of the sensor device
     */
    private Float longitude;
    /**
     * The IP address string representation of four octets divided by a (dot).
     */
    private String ipAddress;

    private SensorLocation(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * Local builder to build an instance of the Sensor Location
     * @author marcello de sales (marcello.sales@gmail.com)
     */
    public static class Builder {
        private float lat;
        private float lon;
        private String ip;
        
        public Builder setLatitude(Float latitude) {
            this.lat = latitude;
            return this;
        }
        
        public Builder setLongitude(Float longitude) {
            this.lon = longitude;
            return this;
        }
        
        public Builder setIpAddress(String ipAddress) {
            this.ip = ipAddress;
            return this;
        }
        
        public SensorLocation build() {
            if (this.ip == null) {
                throw new IllegalStateException("The IP address must be provided to build a Sensor Location");
            }
            //TODO: right now, the IP address is the only required property. In the future, the coordinates
            //could be also enforced.
            return new SensorLocation(this.ip);
        }
    }
    
    /**
     * @return the latitude of the sensor location.
     */
    public float getLatitude() {
        return this.latitude;
    }

    /**
     * @return the longitude of the sensor location
     */
    public float getLongitude() {
        return this.longitude;
    }

    /**
     * @return the ip adddress of sensor location.
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.ipAddress + ":" + this.latitude + "," + this.longitude;
    }
    
    public int hashCode() {
        return 30 * this.ipAddress.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof SensorLocation) {
            return ((SensorLocation)obj).ipAddress.equals(this.ipAddress);
            
        } else return false;
    }
}
