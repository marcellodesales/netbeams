package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchCriteria {

    private String producerType;
    private ComponentLocator producerLocator;
    private String consumerType;
    private ComponentLocator consumerLocator;

    public MatchCriteria(String producerType, ComponentLocator producerLocator,
			String consumerType, ComponentLocator consumerLocator) {
		super();
		this.producerType = producerType;
		this.producerLocator = producerLocator;
		this.consumerType = consumerType;
		this.consumerLocator = consumerLocator;
	}
 
	public boolean equals(Object obj) {
        if (!(obj instanceof MatchCriteria)) {
            return false;
        }
        return this.producerType.equals(((MatchCriteria) obj).producerType)
                && this.producerLocator.getNodeAddress().getValue().equals(
                        ((MatchCriteria) obj).producerLocator.getNodeAddress().getValue());
    }

    public int hashCode() {
        return 10 * this.producerType.hashCode() + 20 *
               this.producerLocator.getNodeAddress().getValue().hashCode();
    }

    public String getProducerType() {
        return this.producerType;
    }

    public void setProducerType(String producerType) {
        this.producerType = producerType;
    }

	public ComponentLocator getProducerLocator() {
        return producerLocator;
    }

    public void setProducerLocator(ComponentLocator producerLocator) {
        this.producerLocator = producerLocator;
    }
    
    public String getConsumerType() {
		return consumerType;
	}

	public void setConsumerType(String consumerType) {
		this.consumerType = consumerType;
	}

	public ComponentLocator getConsumerLocator() {
		return consumerLocator;
	}

	public void setConsumerLocator(ComponentLocator consumerLocator) {
		this.consumerLocator = consumerLocator;
	}
    
}
