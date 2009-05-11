package org.netbeams.dsp.persistence.model.component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.netbeams.dsp.DSPComponent;

/**
 * Defines the information about a component based on the config.xml.
 * 
 * @author marcello
 */
public class ComponentInfo {

    /**
     * It's the name of the DSP component, as represented in the config.xml
     */
    private String name;
    /**
     * The file system path to the file name
     */
    private String filePath;
    /**
     * The message content of the file
     */
    private String messageContentType;
    /**
     * The version of the component
     */
    private String version;

    private Map<String, String> propertiesList;

    public ComponentInfo(List<String> propertiesList) {
        this.propertiesList = new LinkedHashMap<String, String>();
        for (String propertyName : propertiesList) {
            this.propertiesList.put(propertyName, null);
        }
    }

    @Override
    public int hashCode() {
        return 30 * this.name.hashCode() + 31 * this.messageContentType.hashCode() + 32 * this.version.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComponentInfo) {
            return ((ComponentInfo) obj).name.equals(this.name)
                    && ((ComponentInfo) obj).messageContentType.equals(this.messageContentType)
                    && ((ComponentInfo) obj).version.equals(this.version);
        } else
            return false;
    }

    /**
     * @param dspComponent is the DSP component
     * @return the instance of the component info based on the DSP component
     */
    public static ComponentInfo buildFromDSPComponent(DSPComponent dspComponent) {
        List<String> props = new LinkedList<String>();
        ComponentInfo cInf = new ComponentInfo(props);
        dspComponent.getComponentType();
        return cInf;
    }

    /**
     * @return the simple name of the DSP component as used on the config.xml
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the message content package name.
     */
    public String getMessageContentType() {
        return this.messageContentType;
    }

    /**
     * @return the version of the bundle
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @return the Bundle name based on the name and version provided.
     */
    public String getBundleName() {
        return this.name + "-" + this.version + ".jar";
    }
}
