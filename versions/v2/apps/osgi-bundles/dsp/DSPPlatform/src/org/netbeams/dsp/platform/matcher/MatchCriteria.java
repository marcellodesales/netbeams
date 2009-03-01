package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchCriteria {

    private String producerComponentType;
    private String consumerComponentType;
    private ComponentLocator locator;

    /**
     * Creates a new Match Criteria with the producer, consumer and the locator of the DSP component.
     * 
     * @param producerComponentType is the component type of the consumer.
     * @param consumerComponentType is the component type of the final consumer which is located at the given locator.
     * @param locator identifies the location of the component.
     */
    public MatchCriteria(String producerComponentType, String consumerComponentType, ComponentLocator locator) {
        super();
        this.producerComponentType = producerComponentType;
        this.consumerComponentType = consumerComponentType;
        this.locator = locator;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MatchCriteria)) {
            return false;
        }
        return this.producerComponentType.equals(((MatchCriteria) obj).producerComponentType)
                && this.consumerComponentType.equals(((MatchCriteria) obj).consumerComponentType)
                && this.locator.getNodeAddress().getValue().equals(
                        ((MatchCriteria) obj).locator.getNodeAddress().getValue());
    }

    public int hashCode() {
        return 10 * this.producerComponentType.hashCode() + 20 * this.consumerComponentType.hashCode() + 30
                * this.locator.getNodeAddress().getValue().hashCode();
    }

    public String getProducerComponentType() {
        return this.producerComponentType;
    }

    public void setProducerComponentType(String prodcomponentType) {
        this.producerComponentType = prodcomponentType;
    }

    public String getConsumerComponentType() {
        return this.consumerComponentType;
    }

    public void setConsumerComponentType(String conComponentType) {
        this.consumerComponentType = conComponentType;
    }

    public ComponentLocator getLocator() {
        return locator;
    }

    public void setLocator(ComponentLocator locator) {
        this.locator = locator;
    }
}
