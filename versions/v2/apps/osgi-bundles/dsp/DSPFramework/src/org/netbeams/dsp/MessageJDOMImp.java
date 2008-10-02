package org.netbeams.dsp;

/**
 * <?xml verion="1.0" ?>
 * <dmp:message xmlns:dmp="http://www.dmp.org/message.xsd">
 *   <dmp:header>
 *     <dmp:message_id></dmp:message_id>
 *     <dmp:correlation_id></dmp:correlation_id> <!-- Optional -->
 *     <dmp:priority></dmp:priority> <!-- Optional -->
 *     <!-- ACTION, CONFIGURATION, STATE, MEASUREMENT -->
 *     <dmp:type>
 *       <dmp:category></dmp:category>
 *     	 <dmp:hierarchy></dmp:hierarchy>
 *     </dmp:type>
 *     <dmp:producer>
 *       <dmp:locator>
 *         <dmp:component_id></dmp:component_id>
 *         <dmp:node_address></dmp:node_address>
 *       </dmp:locator>
 *     </dmp:producer>
 *     <dmp:consumer>
 *       <dmp:locator>
 *         <dmp:component_id></dmp:component_id>
 *         <dmp:node_address></node_address>
 *       </dmp:locator>
 *     </dmp:consumer>
 *     ...
 *     <dmp:consumer></dmp:consumer>
 *   </dmp:header>
 *   <dmp:body>
 *     <reg:register xmlns:reg="http://org.dmp.org/register.xsd">
 *       <dmp:locator>
 *         <dmp:component_id></dmp:component_id>
 *         <dmp:component_type></dmp:component_type>
 *         <dmp:node_address></node_address>
 *       </dmp:locator>
 *       <!-- Optional -->
 *       <reg:produced_types>
 *         <reg:message_type></reg:message_type>
 *         ...
 *         <reg:message_type></reg:message_type> 
 *       </reg:produced_types>
 *     </reg:register
 *       <!-- Optional -->
 *       <reg:consumed_types>
 *         <reg:message_type></reg:message_type>
 *         ...
 *         <reg:message_type></reg:message_type> 
 *       </reg:consumed_types>
 *     </reg:register
 *   </dmp:body>
 * </dmp:message>
 * 
 */

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.netbeams.dsp.xml.Converter;

public class MessageJDOMImp implements Message {

	Document document;
	Namespace msgNamespace;

	public MessageJDOMImp(Message.Category category, String type, ComponentLocator producer){
		this(category, type, producer, null);
	}

	public MessageJDOMImp(Message.Category category, String type, ComponentLocator producer, List<ComponentLocator> consumers){
		this.document = new Document();
		createMessageControlElements(category, type, producer, consumers);
	}
	
	public void output(Writer writer) throws DMPException{
		
	}

	@Override
	public MessageType getType() throws DMPException {
		Element eMessage = document.getRootElement();
		Element eHeader = eMessage.getChild(MessageXMLConstant.ELEMENT_HEADER, msgNamespace);
		Element eType  = eHeader.getChild(MessageXMLConstant.ELEMENT_MESSAGE_TYPE, msgNamespace);
		Element eCategory = eType.getChild(MessageXMLConstant.ELEMENT_CATEGORY, msgNamespace);
		Element eHierarchy = eType.getChild(MessageXMLConstant.ELEMENT_TYPE_HIERARCHY, msgNamespace);
		
		Message.Category cat = Message.Category.valueOf(eCategory.getTextTrim());
		return new MessageType(cat, eHierarchy.getTextTrim());
		
		
	}

	@Override
	public ComponentIdentifier getProducer() throws DMPException{
		Element eMessage = document.getRootElement();
		Element eHeader = eMessage.getChild(MessageXMLConstant.ELEMENT_HEADER, msgNamespace);
		Element eProducer = eHeader.getChild(MessageXMLConstant.ELEMENT_PRODUCER, msgNamespace);
//		List ePs = eHeader.getChildren(MessageXMLConstant.ELEMENT_PRODUCER, msgNamespace);
		return Converter.toComponentLocator(eProducer.getChild(MessageXMLConstant.ELEMENT_LOCATOR, msgNamespace), 
				msgNamespace);
	}

	@Override
	public List<ComponentIdentifier> getConsumers() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getBodyContent() {
		Element message = this.document.getRootElement();
		Element body = message.getChild(MessageXMLConstant.ELEMENT_BODY, message.getNamespace());
		List children = body.getChildren();
		return (!children.isEmpty())? (Element)children.get(0) : null;
	}
	

	@Override
	public void setBodyContent(Element content) {
		Element message = this.document.getRootElement();
		Element body = message.getChild(MessageXMLConstant.ELEMENT_BODY, message.getNamespace());
		body.addContent(content);		
	}

	@Override
	public void setConsumers(List<ComponentLocator> consumers) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////

	
	private void createMessageControlElements(Message.Category category, String hierarchy,
			ComponentLocator producer, List<ComponentLocator> consumers) 
	{
		// <message>
		Element message = new Element(MessageXMLConstant.ELEMENT_MESSAGE, MessageXMLConstant.NAMESPACE_PREFIX, MessageXMLConstant.NAMESPACE_URI);
		msgNamespace = message.getNamespace();
		this.document.addContent(message);
		// <header>
		Element header = new Element(MessageXMLConstant.ELEMENT_HEADER, msgNamespace);
		message.addContent(header);
		// <type>
		Element type = new Element(MessageXMLConstant.ELEMENT_MESSAGE_TYPE, msgNamespace);
		header.addContent(type);
		// <category>
		Element cat = new Element(MessageXMLConstant.ELEMENT_CATEGORY, msgNamespace);
		cat.setText(category.name());
		type.addContent(cat);
		//<message_type>
		Element hierachy = new Element(MessageXMLConstant.ELEMENT_TYPE_HIERARCHY, msgNamespace);	
		hierachy.setText(hierarchy);
		type.addContent(hierachy);
		// <producer>
		Element prod = new Element(MessageXMLConstant.ELEMENT_PRODUCER, msgNamespace);	
		header.addContent(prod);
		addLocator(prod, producer);
		// <body>
		Element body = new Element(MessageXMLConstant.ELEMENT_BODY, msgNamespace);	
		message.addContent(body);		
		
	}

	private void addLocator(Element prod, ComponentLocator producer) {
		// <locator>
		Element locator = new Element(MessageXMLConstant.ELEMENT_LOCATOR, msgNamespace);	
		prod.addContent(locator);
		// <component_id>
		Element id = new Element(MessageXMLConstant.ELEMENT_COMPONENT_ID, msgNamespace);	
		id.setText(producer.getUUID().toString());
		locator.addContent(id);
		// <node_address>
		Element nodeAddress = new Element(MessageXMLConstant.ELEMENT_NODE_ADDRESS, msgNamespace);	
		nodeAddress.setText(producer.getNodeAddress().getAddress());
		locator.addContent(nodeAddress);
	}

	@Override
	public Object getNativeRepresentation() {
		return document;
	}

	@Override
	public Object getBody() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCorrelationId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getCreationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDeliveryMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBody(Object body) throws DMPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCorrelationId() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeliveryMode(String deliveryMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPriority() {
		// TODO Auto-generated method stub
		
	}

	
}
