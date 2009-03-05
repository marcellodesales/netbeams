package org.netbeams.dsp.message;

public abstract class MessageContent implements XmlDecoratable /* extends AbstractMessageContent*/ {

    // TODO: Find way to turn this into an abstract class
    public String getContentType() {
        return getClass().getName();
    }
    
    public String getContentContextForJAXB() {
        return getClass().getPackage().getName();
    }
}
