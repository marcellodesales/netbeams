package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchCriteria {

    private String producerComponentType;
    private ComponentLocator locator;

    /**
     * Creates a new Match Criteria with the producer, consumer and the locator of the DSP component.
     * 
     * @param producerComponentType is the component type of the consumer.s
     * @param locator identifies the location of the component.
     */
    public MatchCriteria(String producerComponentType, ComponentLocator locator) {
        super();
        this.producerComponentType = producerComponentType;
        this.locator = locator;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MatchCriteria)) {
            return false;
        }
        return this.producerComponentType.equals(((MatchCriteria) obj).producerComponentType)
                && this.locator.getNodeAddress().getValue().equals(
                        ((MatchCriteria) obj).locator.getNodeAddress().getValue());
    }

    public int hashCode() {
        return 10 * this.producerComponentType.hashCode() + 20 *
               this.locator.getNodeAddress().getValue().hashCode();
    }

    public String getProducerComponentType() {
        return this.producerComponentType;
    }

    public void setProducerComponentType(String prodcomponentType) {
        this.producerComponentType = prodcomponentType;
    }

    public ComponentLocator getLocator() {
        return locator;
    }

    public void setLocator(ComponentLocator locator) {
        this.locator = locator;
    }
}
