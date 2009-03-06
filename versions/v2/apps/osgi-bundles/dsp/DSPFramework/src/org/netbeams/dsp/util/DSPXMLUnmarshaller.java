package org.netbeams.dsp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.data.property.Values;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.AbstractNodeAddress;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;
import org.xml.sax.InputSource;

/**
 * DSP XML Unmarshaller is the centralized unmarshaller for the DSP platform.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public enum DSPXMLUnmarshaller {

    INSTANCE;

    /**
     * Unmarshall a messages container in XML format to the POJO format. Please verify messages.xsd for more details on
     * the MessagesContainer value.
     * 
     * @param messagesContainerXml is the MessagesContainer xml instance.
     * @return the MessagesContainer instance that represents the entire set of messages. Note that the BODY (Content
     *         Type) is automatically populated with the POJO identified.
     * 
     * @throws DSPException if any problem related to the XML schema occurs.
     */
    public MessagesContainer unmarshallStream(String messagesContainerXml) throws Exception {
        SAXBuilder parser = new SAXBuilder();
        Document domDoc = parser.build(new InputSource(new StringReader(messagesContainerXml)));

        Element messagesContainerNode = domDoc.getRootElement();

        MessagesContainer container = this.parseMessagesContainerElement(messagesContainerNode);
        List<AbstractMessage> messagesContained = this.parseMessagesOnContainer(messagesContainerNode);
        container.getMessage().addAll(messagesContained);
        return container;
    }

    public MessagesContainer unmarshallStream(File dspMessagesContainerFile) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(dspMessagesContainerFile));
        String line = null, fileLines = "";

        while ((line = reader.readLine()) != null) {
            fileLines = fileLines + line;
        }

        return this.unmarshallStream(fileLines);
    }

    public AbstractMessage makeDSPMessage(Element messageElement) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        String dspMessageClassName = messageElement.getName();
        dspMessageClassName = Message.class.getPackage().getName() + "." + dspMessageClassName;
        ClassLoader loader = Message.class.getClassLoader();
        Class messageType = loader.loadClass(dspMessageClassName);
        return (AbstractMessage) messageType.newInstance();
    }

    private List<AbstractMessage> parseMessagesOnContainer(Element messagesContainerNode)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        List<AbstractMessage> dspMessages = new LinkedList<AbstractMessage>();
        List<Element> messagesNodes = messagesContainerNode.getChildren();
        for (Element messageElement : messagesNodes) {
            AbstractMessage dspMessage = this.makeDSPMessage(messageElement);
            dspMessage = this.parseDSPMessage(dspMessage, messageElement);
            dspMessages.add(dspMessage);
        }
        return dspMessages;
    }

    private AbstractMessage parseDSPMessage(AbstractMessage dspMessage, Element dspMessageElement)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        String contentType = dspMessageElement.getAttributeValue("ContentType");
        dspMessage.setContentType(contentType != null ? contentType.trim() : null);

        String msgId = dspMessageElement.getAttributeValue("messageID");
        dspMessage.setMessageID(msgId != null ? msgId.trim() : null);

        Element dspHeaderElement = dspMessageElement.getChild("Header");
        if (dspHeaderElement != null) {
            Header messageHeader = this.parseMessageHeader(dspHeaderElement);
            dspMessage.setHeader(messageHeader);
        } else {
            throw new IllegalArgumentException("The header is not present!");
        }

        Element dspBodyElement = dspMessageElement.getChild("Body");
        if (dspBodyElement != null) {
            Body messageBody = this.parseMessageBody((Element) dspBodyElement.getChildren().get(0), contentType);
            dspMessage.setBody(messageBody);
        } else {
            throw new IllegalArgumentException("The header is not present!");
        }
        return dspMessage;
    }

    private Body parseMessageBody(Element dspBodyElement, String contentType) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        Body dspBody = new Body();

        if (contentType == null) {
            throw new IllegalArgumentException("Content Type must be provided in order to unmashall the body!");
        }

        String dspMessageContentClassName = dspBodyElement.getName();
        String fullyQualifiedContentType = contentType + "." + dspMessageContentClassName;
        ClassLoader loader = DSPXMLUnmarshaller.class.getClassLoader();
        Class messageType = loader.loadClass(fullyQualifiedContentType);

        MessageContent content = (MessageContent) messageType.newInstance();
        if (content instanceof DSProperties) {
            content = this.parseDSProperties(content, dspBodyElement);
        } else if (content instanceof SondeDataContainer) {
            content = this.parseSondeDataContainer(content, dspBodyElement);
        }

        dspBody.setAny(content);
        return dspBody;
    }

    private Header parseMessageHeader(Element messageHeaderElement) {
        Header header = new Header();

        Element creationTimeElement = messageHeaderElement.getChild("CreationTime");
        if (creationTimeElement != null) {
            String creationTime = creationTimeElement.getValue();
            header.setCreationTime(new Long(creationTime != null ? creationTime.trim() : "-1"));
        }
        String correlationId = messageHeaderElement.getChildText("correlationID");
        header.setCorrelationID(correlationId != null ? correlationId.trim() : null);

        Element producerElement = messageHeaderElement.getChild("Producer");
        if (producerElement != null) {
            header.setProducer(this.makeDSPComponentIdentifier(producerElement));
        }

        Element consumerElement = messageHeaderElement.getChild("Consumer");
        if (consumerElement != null) {
            header.setConsumer(this.makeDSPComponentIdentifier(consumerElement));
        }
        return header;
    }

    private ComponentIdentifier makeDSPComponentIdentifier(Element producerElement) {
        ComponentIdentifier componentIdent = new ComponentIdentifier();

        String compType = producerElement.getChildText("ComponentType");
        componentIdent.setComponentType(compType != null ? compType.trim() : null);

        Element compLocatorElem = producerElement.getChild("ComponentLocator");
        if (compLocatorElem != null) {
            ComponentLocator locator = new ComponentLocator();
            String compNodeId = compLocatorElem.getChildText("ComponentNodeId");
            locator.setComponentNodeId(compNodeId != null ? compNodeId.trim() : null);

            Element nodeAddressElem = compLocatorElem.getChild("NodeAddress");
            if (nodeAddressElem != null) {
                AbstractNodeAddress ndAddr = new NodeAddress();
                String nodeAddr = nodeAddressElem.getValue();
                ndAddr.setValue(nodeAddr != null ? nodeAddr.trim() : null);
                locator.setNodeAddress(ndAddr);
            }
            componentIdent.setComponentLocator(locator);
        }
        return componentIdent;
    }

    private MessagesContainer parseMessagesContainerElement(Element messagesContainerNode) {
        MessagesContainer container = new MessagesContainer();
        if (messagesContainerNode.getName().equals("MessagesContainer")) {

            String value = messagesContainerNode.getAttributeValue("uudi");
            container.setUudi(value != null ? value.trim() : null);

            String host = messagesContainerNode.getAttributeValue("destinationHost");
            container.setDestinationHost(host != null ? host.trim() : null);

            String creationTime = messagesContainerNode.getAttributeValue("creationTime");
            container.setCreationTime(creationTime != null ? creationTime.trim() : null);

            String ack = messagesContainerNode.getAttributeValue("acknowledgeUntil");
            container.setAcknowledgeUntil(new Integer(ack != null ? ack.trim() : "-1"));

            return container;

        } else {
            throw new IllegalArgumentException("DSP XML Unmarshaller could not unmarshall MessagesContainer");
        }
    }

    private MessageContent parseSondeDataContainer(MessageContent content, Element sondeDataContainerElement) {
        SondeDataContainer sondeContainer = (SondeDataContainer) content;
        List<Element> sondeDataElements = sondeDataContainerElement.getChildren("soundeData");
        for (Element sondeDataElem : sondeDataElements) {
            SondeDataType sondeData = new SondeDataType();

            String date = sondeDataElem.getAttributeValue("date");
            sondeData.setDate(date != null ? date.trim() : null);

            String time = sondeDataElem.getAttributeValue("time");
            sondeData.setTime(time != null ? time.trim() : null);

            String temp = sondeDataElem.getChildText("Temp");
            sondeData.setTime(temp != null ? temp.trim() : null);

            String SpCond = sondeDataElem.getChildText("SpCond");
            sondeData.setSpCond(SpCond != null ? SpCond.trim() : null);

            String Cond = sondeDataElem.getChildText("Cond");
            sondeData.setCond(Cond != null ? Cond.trim() : null);

            String Resist = sondeDataElem.getChildText("Resist");
            sondeData.setResist(Resist != null ? Resist.trim() : null);

            String Sal = sondeDataElem.getChildText("Sal");
            sondeData.setSal(Sal != null ? Sal.trim() : null);

            String Press = sondeDataElem.getChildText("Press");
            sondeData.setPress(Press != null ? Press.trim() : null);

            String Depth = sondeDataElem.getChildText("Depth");
            sondeData.setDepth(Depth != null ? Depth.trim() : null);

            String pH = sondeDataElem.getChildText("pH");
            sondeData.setPH(temp != null ? pH.trim() : null);

            String phmV = sondeDataElem.getChildText("phmV");
            sondeData.setPhmV(phmV != null ? phmV.trim() : null);

            String ODOSat = sondeDataElem.getChildText("ODOSat");
            sondeData.setODOSat(ODOSat != null ? ODOSat.trim() : null);

            String ODOConc = sondeDataElem.getChildText("ODOConc");
            sondeData.setODOConc(ODOConc != null ? ODOConc.trim() : null);

            String Turbid = sondeDataElem.getChildText("Turbid");
            sondeData.setTurbid(Turbid != null ? Turbid.trim() : null);

            String Battery = sondeDataElem.getChildText("Battery");
            sondeData.setBattery(Battery != null ? Battery.trim() : null);

            sondeContainer.getSondeData().add(sondeData);
        }
        return sondeContainer;
    }

    private MessageContent parseDSProperties(MessageContent content, Element dspPropertiesElement) {
        DSProperties properties = (DSProperties) content;

        List<Element> props = dspPropertiesElement.getChildren("Property");
        for (Element propElement : props) {
            DSProperty property = new DSProperty();
            String name = propElement.getAttributeValue("name");
            property.setName(name != null ? name.trim() : null);

            String type = propElement.getAttributeValue("type");
            property.setType(type != null ? type.trim() : null);

            String format = propElement.getAttributeValue("format");
            property.setFormat(format != null ? format.trim() : null);

            String unit = propElement.getAttributeValue("unit");
            property.setUnit(unit != null ? unit.trim() : null);

            String instruction = propElement.getAttributeValue("instruction");
            property.setInstruction(instruction != null ? instruction.trim() : null);

            String singleValue = propElement.getChildText("Value");
            property.setValue(singleValue != null ? singleValue.trim() : null);

            Element valuesElement = propElement.getChild("Values");
            if (valuesElement != null) {
                Values values = new Values();

                List<Element> possibleElements = valuesElement.getChildren("value");
                for (Element possibleValueElement : possibleElements) {
                    String value = possibleValueElement.getValue();
                    if (value != null && !value.equals("")) {
                        values.getValue().add(value);
                    }
                }
            }
            properties.getProperty().add(property);
        }
        return properties;
    }

//    public static void main(String[] args) throws IOException {
//
//        File a = new File("/media/disk/development/workspaces/sfsu/netBEAMS2 OSGi General/docs/messagesExample-dsp.xml");
//
//        BufferedReader reader = new BufferedReader(new FileReader(a));
//        String line = null, fileLines = "";
//
//        while ((line = reader.readLine()) != null) {
//            fileLines = fileLines + line;
//        }
//
//        try {
//            MessagesContainer container = DSPXMLUnmarshaller.INSTANCE.unmarshallStream(fileLines);
//            container.getMessage().size();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
