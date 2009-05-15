package org.netbeams.dsp.ysi;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.MessageCategory;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.AcknowledgementMessage;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.QueryMessage;
import org.netbeams.dsp.message.UpdateMessage;
import org.netbeams.dsp.util.NetworkUtil;
import org.w3c.dom.Node;


/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 *
 */
public class SondeDSPComponent implements DSPComponent {

    private static final Logger log = Logger.getLogger(SondeDSPComponent.class); 
    private static final ComponentDescriptor componentDescriptor;
    public static final String COMPONENT_TYPE = "org.netbeams.dsp.ysi";
    public static final String MSG_CONTENT_TYPE_SONDE_DATA = "sonde.data";
    public static boolean hasSamplingFrequencyChanged = false;
    public static String samplingFrequency = "5";

    private DSPContext context;
    private String componentNodeId;
    private SondeProducer producer;

    static {
        componentDescriptor = new ComponentDescriptor();

        Collection<MessageCategory> producedMessageCategories = new ArrayList<MessageCategory>();
        Collection<MessageCategory> consumedMessageCategories = new ArrayList<MessageCategory>();
        MessageCategory messageCategory = new MessageCategory(SondeDSPComponent.class.getName(),
                MSG_CONTENT_TYPE_SONDE_DATA);

        producedMessageCategories.add(messageCategory);
        componentDescriptor.setMsgCategoryProduced(producedMessageCategories);
        componentDescriptor.setMsgCategoryConsumed(consumedMessageCategories);
    }


    public SondeDSPComponent() {
        log.debug("Instantiating the Sonde DSP component");
    };



    private void processMessage(Message message) {
        log.debug("message class = " + message.getClass().getName());

        if(message instanceof UpdateMessage){
            try {
                processUpdate(message);
            } catch (DSPException e) {
                log.error(e.getMessage(), e);
            }
        }else if(message instanceof QueryMessage){
            try {
                processQuery(message);
            } catch (DSPException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private void processUpdate(Message message) throws DSPException {
        log.debug("Update message received: configuring DSP Sonde Producer with new sampling frequency.");
        DSProperties dspProperties = null;
        UpdateMessage updateMessage = (UpdateMessage) message;
        MessageContent propertiesNode = updateMessage.getBody().getAny();
        dspProperties = (DSProperties) propertiesNode;

        ArrayList<DSProperty> propertyList = (ArrayList<DSProperty>) dspProperties.getProperty();
        DSProperty property = propertyList.get(0);
        String oldSamplingFrequency = samplingFrequency;
        samplingFrequency = property.getValue();
        if (!oldSamplingFrequency.equals(samplingFrequency) && !hasSamplingFrequencyChanged) {
            hasSamplingFrequencyChanged = true;
        }
        log.debug("Updated Property: " + property.getName() + "=" + property.getValue());
        sendBackAcknowledgement(updateMessage);
    }


    private void sendBackAcknowledgement(UpdateMessage updateMessage) {
        DSProperties props = new DSProperties();

        // Obtain original producer
        ComponentIdentifier origProducer = updateMessage.getHeader().getProducer();
        String originalMessageId = updateMessage.getMessageID();

        // Create reply message
        String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(getComponentNodeId(),
                                       localIPAddress, getComponentType());

        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
        header.setCorrelationID(originalMessageId);

        Message replyMsg = DSPMessagesFactory.INSTANCE.makeDSPAcknowledgementMessage(header, props);

        sendMessage(replyMsg);
    }



    private void processQuery(Message message)throws DSPException {
        log.debug("Query message delivered to this component. Begin sending a response...");
        DSProperties dspProperties = null;
        QueryMessage queryMessage = (QueryMessage) message;
        MessageContent content = queryMessage.getBody().getAny();

        log.debug("Content class " + content.getClass().getName());

        if (content instanceof DSProperties) {
            log.debug("Got query configuration");
            dspProperties = new DSProperties();

            DSProperty freq = new DSProperty();
            freq.setName("SAMPLING_FREQUENCY");
            freq.setValue(samplingFrequency);
            dspProperties.getProperty().add(freq);
        }
        sendReply(queryMessage, dspProperties);
    }

    private void sendMessage(Message message) {
        MessageBrokerAccessor messageBroker;
        try {
            messageBroker = context.getDataBroker();
            if(messageBroker != null){
                messageBroker.send(message);
                log.debug("Reply message sent back");
            }else{
                log.debug("Message broker not available");
            }
        } catch (DSPException e) {
            log.warn("Cannot send reply", e);
        }
    }

    private void sendReply(QueryMessage queryMessage, DSProperties dspProperties) {
        // Obtain original producer
        ComponentIdentifier origProducer = queryMessage.getHeader().getProducer();
        String originalMessageId = queryMessage.getMessageID();

        // Create reply message
        Message replyMsg = null;
        String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier(getComponentNodeId(),
                                       localIPAddress, getComponentType());

        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, origProducer);
        header.setCorrelationID(originalMessageId);

        replyMsg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, dspProperties);

        MessageBrokerAccessor messageBroker;
        try {
            messageBroker = context.getDataBroker();
            if(messageBroker != null){
                messageBroker.send(replyMsg);
                log.debug("Reply message sent back");
            }else{
                log.debug("Message broker not available");
            }
        } catch (DSPException e) {
            log.warn("Cannot send reply", e);
        }
    }


 // Implemented methods of the DSPComponent interface.



    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        log.info("Initializing Sonde DSP component: " + componentNodeId);
        this.context = context;
        this.componentNodeId = componentNodeId;
    }

    public String getComponentNodeId() {
        return componentNodeId;
    }

    public String getComponentType() {
        return COMPONENT_TYPE;
    }


    public ComponentDescriptor getComponentDescriptor() {
        return componentDescriptor;
    }


    public void deliver(Message message) throws DSPException {
        log.debug("Delivering Message....");
        processMessage(message);
    }


    public Message deliverWithReply(Message message) throws DSPException {
        log.debug("Delivering message with reply.");
        return null;
    }


    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        log.debug("Delivering message with reply w/wait.");
        return null;
    }


    public void startComponent() throws DSPException {
        log.info("Starting Sonde DSP component");
        producer = new SondeProducer(context);
        producer.startProducer();
    }


    public void stopComponent() throws DSPException {
        log.info("Stopping Sonde DSP component");
        producer.stopProducer();
    }

}
