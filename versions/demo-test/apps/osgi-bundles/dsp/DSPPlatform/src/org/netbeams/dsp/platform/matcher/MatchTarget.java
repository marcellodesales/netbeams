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

    private String consumerType;
    private ComponentLocator locator;
    private String gatewayType;

    /**
     * Creates a new MatchTarget with the component type target, component locator and the gateway component type.
     * 
     * @param componentType is the target component type on the destination. This is the final component that will
     *        matches as the receiving component.
     * @param locator is the component locator identification.
     * @param gatewayComponentType is the default gateway component that will transfer the message to an external DSP.
     *        This gateway component might use any sort of DSP message serialization mechanism.
     */
    public MatchTarget(String consumerType, ComponentLocator locator, String gatewayType) {
        super();
        this.consumerType = consumerType;
        this.locator = locator;
        this.gatewayType = gatewayType;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MatchTarget)) {
            return false;
        }
        if (this.gatewayType != null)
            return this.consumerType.equals(((MatchTarget) obj).consumerType)
                    && this.locator.getNodeAddress().getValue().equals(
                            ((MatchTarget) obj).locator.getNodeAddress().getValue())
                    && this.gatewayType.equals(((MatchTarget) obj).gatewayType);
        else
            return this.consumerType.equals(((MatchTarget) obj).consumerType)
                    && this.locator.getNodeAddress().getValue().equals(
                            ((MatchTarget) obj).locator.getNodeAddress().getValue());
    }

    public int hashCode() {
    	int value = (this.gatewayType != null) ? 10 * this.consumerType.hashCode() : 0;
    	value += this.gatewayType != null ? 30 * this.gatewayType.hashCode() : 0;
    	value += this.locator != null ? 20 * this.locator.getNodeAddress().getValue().hashCode() : 0;
        return value;
    }

    public String getConsumerType() {
        return this.consumerType;
    }

    public void setConsumerType(String consumerComponentType) {
        this.consumerType = consumerComponentType;
    }

    public ComponentLocator getLocator() {
        return locator;
    }

    public void setLocator(ComponentLocator locator) {
        this.locator = locator;
    }

    public void setGatewayType(String componentType) {
        this.gatewayType = componentType;
    }

    public String getGatewayType() {
        return this.gatewayType;
    }
}
