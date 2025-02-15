package org.netbeams.dsp.message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * This is a factory method of the DSP messages
 * 
 * @author marcello
 */
public enum DSPMessagesFactory {

    /**
     * The singleton version of this factory
     */
    INSTANCE;

    /**
     * @param compId is the identification of the component
     * @param location is the location of the component. It must be a machine name or ip address.
     * @param compType is the complete name of the
     * @return a new instance of a Component Identifier with the specified data.
     */
    public ComponentIdentifier makeDSPComponentIdentifier(String compId, String location, String compType) {
        ComponentLocator loc = new ComponentLocator();
        loc.setComponentNodeId(compId);
        AbstractNodeAddress nadd = new AbstractNodeAddress();
        nadd.setValue(location);
        loc.setNodeAddress(nadd);
        ComponentIdentifier pIdent = new ComponentIdentifier();
        pIdent.setComponentLocator(loc);
        pIdent.setComponentType(compType);
        return pIdent;
    }

    /**
     * @param correlationId
     * @param creationTime is 
     * @param producer is the ComponentIdentifier instance for the producer.
     * @param consumer is the ComponentIdentifier instance for the consumer.
     * @return a new instance of the Header for the DSP Message
     */
    public Header makeDSPMessageHeader(String correlationId, ComponentIdentifier producer, ComponentIdentifier consumer) {
        Header header = new Header();
        header.setCorrelationID(correlationId);
        header.setCreationTime(System.currentTimeMillis());
        header.setProducer(producer);
        header.setConsumer(consumer);
        return header;
    }

    /**
     * The ISO time specification is as follows:
     * 
     * <br>
     * 2002-05-30T09:30:10-06:00 shows the GMT -6 time or <br>
     * 2002-05-30T09:30:10Z shows the UTC/GMT default time
     * 
     * @return make a new current ISO time format used to be interoperable with other programming languages.
     */
    public String makeCurrentIsoDateTime() {
        // final int msInMin = 60000;
        // final int minInHr = 60;
        // Date date = new Date();
        // int Hours, Minutes;
        // DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG );
        // TimeZone zone = dateFormat.getTimeZone();
        // System.out.println( "BST Time: " + dateFormat.format( date ) );
        // Minutes =zone.getOffset( date.getTime() ) / msInMin;
        // Hours = Minutes / minInHr;
        // zone = zone.getTimeZone( "GMT Time" +(Hours>=0?"+":"")+Hours+":"+ Minutes);
        // dateFormat.setTimeZone( zone );
        // System.out.println( "GMT: " + dateFormat.format( date ) );
        //        
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        // System.out.println("ISO 8601: "+sdf.format( now ));
        return sdf.format(now);
        //        
        // Date now2 = sdf.parse(sdf.format( now ));
        // System.out.println("ISO 8601: "+sdf.format( now2 ));
    }

    /**
     * @return a new instance of the DSP Messages Container with the correct new values of the UUID and the creation
     *         time of the message.
     */
    public MessagesContainer makeDSPMessagesContainer(String destIpAddress) {
        MessagesContainer messages = new MessagesContainer();
        messages.setCreationTime(this.makeCurrentIsoDateTime());
        messages.setUudi(UUID.randomUUID().toString());
        messages.setDestinationHost(destIpAddress);
        return messages;
    }

    /**
     * @param messageId is the message ID
     * @param contentType is the description of the main class, with the complete name (org.netbeans.dsp.demo.Stick)
     * @param header is the information of the header.
     * @param bodyNode is the instance of a JAXB generated object
     * @return an instance of the Measure Message with the given Id, header and the body with the given contentType.
     */
    private Message makeDSPMessage(Message m1, Header header, MessageContent bodyPayload) {
        //m1.setContentType(bodyPayload.getContentContextForJAXB());
    	m1.setContentType(bodyPayload.getContentType());
        m1.setMessageID(UUID.randomUUID().toString());
        m1.setHeader(header);

        Body dspMsgBody = new Body();
        dspMsgBody.setAny(bodyPayload);
        m1.setBody(dspMsgBody);

        return m1;
    }

    public MeasureMessage makeDSPMeasureMessage(Header header, MessageContent body) {
        MeasureMessage m1 = new MeasureMessage();
        return (MeasureMessage) this.makeDSPMessage(m1, header, body);
    }

    public EventMessage makeDSPEventMessage(Header header, MessageContent body) {
        EventMessage m1 = new EventMessage();
        return (EventMessage) this.makeDSPMessage(m1, header, body);
    }
    
    public AcknowledgementMessage makeDSPAcknowledgementMessage(Header header, MessageContent body)
        {
        AcknowledgementMessage m1 = new AcknowledgementMessage();
        return (AcknowledgementMessage) this.makeDSPMessage(m1, header, body);
    }

    public ActionMessage makeDSPActionMessage(Header header, MessageContent body) {
        ActionMessage m1 = new ActionMessage();
        return (ActionMessage) this.makeDSPMessage(m1, header, body);
    }

    public QueryMessage makeDSPQueryMessage(Header header, MessageContent body) {
        QueryMessage m1 = new QueryMessage();
        return (QueryMessage) this.makeDSPMessage(m1, header, body);
    }

    public CreateMessage makeDSPCreateMessage(Header header, MessageContent body) {
        CreateMessage m1 = new CreateMessage();
        return (CreateMessage) this.makeDSPMessage(m1, header, body);
    }

    public DeleteMessage makeDSPDeleteMessage(Header header, MessageContent body) {
        DeleteMessage m1 = new DeleteMessage();
        return (DeleteMessage) this.makeDSPMessage(m1, header, body);
    }

    public InsertMessage makeDSPInsertMessage(Header header, MessageContent body) {
        InsertMessage m1 = new InsertMessage();
        return (InsertMessage) this.makeDSPMessage(m1, header, body);
    }

    public UpdateMessage makeDSPUpdateMessage(Header header, MessageContent body) {
        UpdateMessage m1 = new UpdateMessage();
        return (UpdateMessage) this.makeDSPMessage(m1, header, body);
    }
}
