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
    private Double latitude;
    /**
     * It's the longitude location of the sensor device
     */
    private Double longitude;
    /**
     * The IP address string representation of four octets divided by a (dot).
     */
    private String ipAddress;

    /**
     * Creates a new instance of the sensor location. However, this constructor is only accessable through the
     * Builder instance.
     * @param ipAddress the ip address of the sensor location.
     * @param lat the latitude value
     * @param lon the longitude value 
     */
    private SensorLocation(String ipAddress, Double lat, Double lon) {
        this.ipAddress = ipAddress;
        this.latitude = lat;
        this.longitude = lon;
    }

    /**
     * Local builder to build an instance of the Sensor Location
     * 
     * @author marcello de sales (marcello.sales@gmail.com)
     */
    public static class Builder {
        private Double lat;
        private Double lon;
        private String ip;

        public Builder setLatitude(Double latitude) {
            this.lat = latitude;
            return this;
        }

        public Builder setLongitude(Double longitude) {
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
            if (this.lat == 0 || this.lon == null) {
                throw new IllegalStateException(
                        "Both the latitude and longitude must be provided to build a Sensor Location");
            }
            return new SensorLocation(this.ip, this.lat, this.lon);
        }
    }

    /**
     * @return the latitude of the sensor location.
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * @return the longitude of the sensor location
     */
    public double getLongitude() {
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
            return ((SensorLocation) obj).ipAddress.equals(this.ipAddress);

        } else
            return false;
    }
}
