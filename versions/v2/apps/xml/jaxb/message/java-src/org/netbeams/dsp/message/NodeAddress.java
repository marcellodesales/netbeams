package org.netbeams.dsp.message;

public class NodeAddress extends AbstractNodeAddress {

    public NodeAddress() {
    }

    public NodeAddress(String nodeAddress) {
        setValue(nodeAddress);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NodeAddress) {
            NodeAddress o = (NodeAddress) obj;
            return value.equals(o.getValue());
        }
        return false;
    }

    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<NodeAddress>" + this.value + "</NodeAddress>");
        return builder.toString();
    }
}
