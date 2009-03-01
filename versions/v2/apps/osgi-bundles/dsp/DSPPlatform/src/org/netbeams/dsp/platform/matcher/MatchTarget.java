package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

/**
 * Specifies the match target with a component type, a node address and an optional default gateway. If the gateway
 * component is not specified, an empty value is considered.
 * 
 * @author Kleber Sales Marcello de Sales
 * 
 */
public class MatchTarget {

    private String componentType;
    private ComponentLocator locator;
    private String gatewayComponentType;

    /**
     * Creates a new MatchTarget with the component type target, component locator and the gateway component type.
     * 
     * @param componentType is the target component type on the destination. This is the final component that will
     *        matches as the receiving component.
     * @param locator is the component locator identification.
     * @param gatewayComponentType is the default gateway component that will transfer the message to an external DSP.
     *        This gateway component might use any sort of DSP message serialization mechanism.
     */
    public MatchTarget(String componentType, ComponentLocator locator, String gatewayComponentType) {
        super();
        this.componentType = componentType;
        this.locator = locator;
        this.gatewayComponentType = gatewayComponentType == null ? "" : gatewayComponentType;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MatchTarget)) {
            return false;
        }
        if (this.gatewayComponentType != null)
            return this.componentType.equals(((MatchTarget) obj).componentType)
                    && this.locator.getNodeAddress().getValue().equals(
                            ((MatchTarget) obj).locator.getNodeAddress().getValue())
                    && this.gatewayComponentType.equals(((MatchTarget) obj).gatewayComponentType);
        else
            return this.componentType.equals(((MatchTarget) obj).componentType)
                    && this.locator.getNodeAddress().getValue().equals(
                            ((MatchTarget) obj).locator.getNodeAddress().getValue());
    }

    public int hashCode() {
        if (this.gatewayComponentType != null)
            return 10 * this.componentType.hashCode() + 20 * this.locator.getNodeAddress().getValue().hashCode() + 30
                    * this.gatewayComponentType.hashCode();
        else
            return 10 * this.componentType.hashCode() + 20 * this.locator.getNodeAddress().getValue().hashCode();
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public ComponentLocator getLocator() {
        return locator;
    }

    public void setLocator(ComponentLocator locator) {
        this.locator = locator;
    }

    public void setGatewayComponentType(String componentType) {
        this.gatewayComponentType = componentType;
    }

    public String getGatewayComponentType() {
        return this.gatewayComponentType;
    }
}
