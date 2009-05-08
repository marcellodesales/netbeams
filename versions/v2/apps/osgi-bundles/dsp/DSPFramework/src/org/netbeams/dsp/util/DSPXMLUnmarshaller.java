package org.netbeams.dsp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.data.property.Values;
import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
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

    private static final Logger log = Logger.getLogger(DSPXMLUnmarshaller.class);

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
        log.debug("Unmarshalling stream " + messagesContainerXml);
        SAXBuilder parser = new SAXBuilder();
        Document domDoc = parser.build(new InputSource(new StringReader(messagesContainerXml)));

        Element messagesContainerNode = domDoc.getRootElement();

        MessagesContainer container = this.parseMessagesContainerElement(messagesContainerNode);
        List<AbstractMessage> messagesContained = this.parseMessagesOnContainer(messagesContainerNode);
        container.getMessage().addAll(messagesContained);
        log.debug("Unmarshalled stream success: container ID " + container.getUudi());
        return container;
    }

    public MessagesContainer unmarshallStream(File dspMessagesContainerFile) throws Exception {
        log.debug("Unmarshalling stream from file " + dspMessagesContainerFile);
        BufferedReader reader = new BufferedReader(new FileReader(dspMessagesContainerFile));
        String line = null, fileLines = "";

        while ((line = reader.readLine()) != null) {
            fileLines = fileLines + line;
        }
        return this.unmarshallStream(fileLines);
    }

    public AbstractMessage makeDSPMessage(Element messageElement) throws DSPException {

        String dspMessageClassName = messageElement.getName();
        dspMessageClassName = Message.class.getPackage().getName() + "." + dspMessageClassName;
        ClassLoader loader = Message.class.getClassLoader();
        Class messageType;
        try {
            messageType = loader.loadClass(dspMessageClassName);
            return (AbstractMessage) messageType.newInstance();
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new DSPException(e);
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
            throw new DSPException(e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new DSPException(e);
        }
    }

    private List<AbstractMessage> parseMessagesOnContainer(Element messagesContainerNode) throws DSPException {

        List<AbstractMessage> dspMessages = new LinkedList<AbstractMessage>();
        List<Element> messagesNodes = messagesContainerNode.getChildren();
        for (Element messageElement : messagesNodes) {
            AbstractMessage dspMessage = this.makeDSPMessage(messageElement);
            dspMessage = this.parseDSPMessage(dspMessage, messageElement);
            dspMessages.add(dspMessage);
        }
        return dspMessages;
    }

    private AbstractMessage parseDSPMessage(AbstractMessage dspMessage, Element dspMessageElement) throws DSPException {

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
            Body messageBody;
            try {
                messageBody = this.parseMessageBody((Element) dspBodyElement.getChildren().get(0), contentType);
            } catch (ClassNotFoundException e) {
                throw new DSPException(e);
            } catch (InstantiationException e) {
                throw new DSPException(e);
            } catch (IllegalAccessException e) {
                throw new DSPException(e);
            }
            dspMessage.setBody(messageBody);
        } else {
            throw new IllegalArgumentException("The header is not present!");
        }
        return dspMessage;
    }

    private Body parseMessageBody(Element dspBodyElement, String contentType) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, DSPException {
        Body dspBody = new Body();

        if (contentType == null) {
            throw new IllegalArgumentException("Content Type must be provided in order to unmashall the body!");
        }

        ClassLoader loader = DSPXMLUnmarshaller.class.getClassLoader();
        Class messageType = loader.loadClass(contentType);

        MessageContent content = (MessageContent) messageType.newInstance();
        if (content instanceof DSProperties) {
            content = this.parseDSProperties(dspBodyElement);
        } else if (content instanceof SondeDataContainer) {
            content = this.parseSondeDataContainer(dspBodyElement);
        } else if (content instanceof MouseActionsContainer) {
            content = this.parseMouseActions(dspBodyElement);
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
        Element correlationIdElement = messageHeaderElement.getChild("CorrelationID");
        if (correlationIdElement != null) {
            String correlationId = correlationIdElement.getValue();
            header.setCorrelationID(correlationId != null ? correlationId.trim() : null);
        }

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

            String window = messagesContainerNode.getAttributeValue("windowSize");
            container.setWindowSize(window != null ? new Integer(window.trim()) : null);
            
            String ack = messagesContainerNode.getAttributeValue("acknowledgeUntil");
            container.setAcknowledgeUntil(ack != null ? new Integer(ack.trim()) : null);

            return container;

        } else {
            log.error("DSP XML Unmarshaller could not unmarshall MessagesContainer");
            throw new IllegalArgumentException("DSP XML Unmarshaller could not unmarshall MessagesContainer");
        }
    }

    private MessageContent parseSondeDataContainer(Element sondeDataContainerElement) {

        SondeDataContainer sondeContainer = new SondeDataContainer();
        List<Element> sondeDataElements = sondeDataContainerElement.getChildren("sondeData");

        for (Element sondeDataElem : sondeDataElements) {
            try {
                SondeDataType sondeData = new SondeDataType();

                String dateTime = sondeDataElem.getAttributeValue("samplingTimeStamp");
                if (dateTime != null) {
                    sondeData.setDateTime(dateTime.substring(0, dateTime.indexOf(" ")), dateTime.substring(dateTime
                            .indexOf(" ") + 1, dateTime.length()));
                }

                String SpCond = sondeDataElem.getChildText("SpCond");
                if (SpCond != null) {
                    sondeData.setSpCond(Float.parseFloat(SpCond.trim()));
                }

                String Cond = sondeDataElem.getChildText("Cond");
                if (Cond != null) {
                    sondeData.setCond(Float.parseFloat(Cond.trim()));
                }

                String Resist = sondeDataElem.getChildText("Resist");
                if (Resist != null) {
                    sondeData.setResist(Float.parseFloat(Resist.trim()));
                }

                String Sal = sondeDataElem.getChildText("Sal");
                if (Sal != null) {
                    sondeData.setSal(Float.parseFloat(Sal.trim()));
                }

                String Press = sondeDataElem.getChildText("Press");
                if (Press != null) {
                    sondeData.setPress(Float.parseFloat(Press.trim()));
                }

                String Depth = sondeDataElem.getChildText("Depth");
                if (Depth != null) {
                    sondeData.setDepth(Float.parseFloat(Depth.trim()));
                }

                String pH = sondeDataElem.getChildText("pH");
                if (pH != null) {
                    sondeData.setPH(Float.parseFloat(pH.trim()));
                }

                String phmV = sondeDataElem.getChildText("phmV");
                if (phmV != null) {
                    sondeData.setPhmV(Float.parseFloat(phmV.trim()));
                }

                String ODOSat = sondeDataElem.getChildText("ODOSat");
                if (ODOSat != null) {
                    sondeData.setODOSat(Float.parseFloat(ODOSat.trim()));
                }

                String ODOConc = sondeDataElem.getChildText("ODOConc");
                if (ODOConc != null) {
                    sondeData.setODOConc(Float.parseFloat(ODOConc.trim()));
                }

                String Turbid = sondeDataElem.getChildText("Turbid");
                if (Turbid != null) {
                    sondeData.setTurbid(Float.parseFloat(Turbid.trim()));
                }

                String Battery = sondeDataElem.getChildText("Battery");
                if (Battery != null) {
                    sondeData.setBattery(Float.parseFloat(Battery.trim()));
                }
                
                String temp = sondeDataElem.getChildText("Temp");
                if (Battery != null) {
                    sondeData.setTemp(Float.parseFloat(temp.trim()));
                }
                
                sondeContainer.getSondeData().add(sondeData);

            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
                continue;
            }
        }
        return sondeContainer;
    }

    private MessageContent parseDSProperties(Element dspPropertiesElement) {
        DSProperties properties = new DSProperties();
        
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

    private MessageContent parseMouseActions(Element mouseActionsElement) {
        MouseActionsContainer actionsContainer = new MouseActionsContainer();

        List<Element> props = mouseActionsElement.getChildren("mouseActionsContainer");
        for (Element mouseActionElement : props) {
            MouseAction mouseAction = new MouseAction();
            mouseAction.setButton(ButtonName.valueOf(mouseActionElement.getAttributeValue("button")));
            mouseAction.setEvent(EventName.valueOf(mouseActionElement.getAttributeValue("event")));
            mouseAction.setX(new Integer(mouseActionElement.getAttributeValue("x")));
            mouseAction.setY(new Integer(mouseActionElement.getAttributeValue("y")));
            actionsContainer.getMouseAction().add(mouseAction);
        }
        return actionsContainer;
    }

    public static void main(String[] args) throws IOException {

        File a = new File("/media/backups/development/workspaces/sfsu/netBEAMS2 OSGi General/messagesExample-dsp.xml");

        BufferedReader reader = new BufferedReader(new FileReader(a));
        String line = null, fileLines = "";

        while ((line = reader.readLine()) != null) {
            fileLines = fileLines + line;
        }

        try {
            MessagesContainer container = DSPXMLUnmarshaller.INSTANCE.unmarshallStream(fileLines);
            System.out.println(container.getMessage().size());
            
            for(AbstractMessage msg : container.getMessage()) {
                System.out.println(msg.getContentType());
                System.out.println(msg.getBody().getAny());
            }
                   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
