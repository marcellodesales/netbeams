package org.netbeams.dsp.persistence.model.setup;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.netbeams.dsp.persistence.model.component.ComponentInfo;
import org.netbeams.dsp.persistence.model.location.SensorLocation;

/**
 * The configuration class is responsible for configuring a given sensor in a given 
 * location. 
 * @author marcello
 */
public class RemoteSensorConfiguration {

    /**
     * The title of the configuration is an easy way to remember about the configuration.
     */
    private String title;
    /**
     * The location of the configuration based on the sensor device location.
     */
    private SensorLocation sensorLocation;
    /**
     * The list of components on the configuration.
     */
    List<ComponentInfo> components;
    /**
     * Defines the priorities for each of the component info in the configuration.
     */
    Map<ComponentInfo, Integer> priorities;
    
    /**
     * Creates a new sensor configuration based on a title and a sensor location.
     * @param title is the title of the configuration. It's an easy way to remember about the configuration.
     * @param sensorLocation is the configuration for the location.
     */
    public RemoteSensorConfiguration(String title, SensorLocation sensorLocation) {
        this.title = title;
        this.sensorLocation = sensorLocation;
        this.components = new LinkedList<ComponentInfo>();
        this.priorities = new LinkedHashMap<ComponentInfo, Integer>();
    }
    
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Adds a new component info into the list of components
     * @param newComponentInfo
     */
    public void addComponentInfo(ComponentInfo newComponentInfo, Integer priority) {
        this.components.add(newComponentInfo);
        this.priorities.put(newComponentInfo, priority);
    }
    
    /**
     * @param existingComponentPriority
     * @return the priority of a given ComponentInfo. It returns -1 if the component Info does not exist.
     */
    public Integer getComponentInfoPriority(ComponentInfo existingComponentPriority) {
        Integer cInf = this.priorities.get(existingComponentPriority);
        return cInf == null ? -1 : cInf;
    }
    
    /**
     * Sets a new sensor device location to the configuration. It replaces the existing one, if any.
     * @param newSensorLocation is the new sensor location.
     */
    public void setSensorLocation(SensorLocation newSensorLocation) {
        this.sensorLocation = newSensorLocation;
    }
    
    /**
     * @return the sensor location of this remote sensor configuration.
     */
    public SensorLocation getSensorLocation() {
        return this.sensorLocation;
    }
}
