package org.netbeams.dsp.platform.matcher;

import org.netbeams.dsp.message.ComponentLocator;

public class MatchCriteria {
	
	private String componentType;
	private ComponentLocator locator;
	
	public MatchCriteria(String componentType, ComponentLocator locator) {
		super();
		this.componentType = componentType;
		this.locator = locator;
	}

        public boolean equals(Object obj) {
            if (!(obj instanceof MatchCriteria)) {
                return false;
            }
            return this.componentType.equals(((MatchCriteria)obj).componentType) && 
              this.locator.getNodeAddress().getValue().equals(((MatchCriteria)obj).locator.getNodeAddress().getValue());
        }
        
        public int hashCode() {
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

}
