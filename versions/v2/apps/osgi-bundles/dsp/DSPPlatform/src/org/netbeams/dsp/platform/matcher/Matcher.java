package org.netbeams.dsp.platform.matcher;

/**
 * This is a basic implementation of the Matcher.
 * 
 *  This initial verions is meant only for the initial prototype. It re-creates the matching information
 *  everytime is starts. There is no persistence. 
 *  
 *  A better implementation would be persist the matching information and ask the register the latest updates
 *  since last update.
 *  
 *  I still need to think a little bit more on how the callback mechanism will work with this update.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.message.Message;

public class Matcher implements BaseComponent {
	
	private static final Logger log = Logger.getLogger(Matcher.class);
	
	private static final String CONFIG_FILE_NAME = "matcher_config.xml";

	private String componentNodeId;
	private DSPContext context;
	private MessageBrokerAccessor brokerAccessor;
		
	private Object lock;
	
	List<MatchRule> rules;
	
	public Matcher(){
		lock = new Object();
		rules = new ArrayList<MatchRule>();
	}

	/**
	 * @Override
	 */
	public String getComponentType() {
		return GlobalComponentTypeName.MATCHER;
	}

	/**
	 * @Override
	 */
	public String getComponentNodeId() {
		return componentNodeId;
	}
	
	public void initComponent(String componentNodeId, DSPContext context) throws DSPException{
		
		log.info("initComponent() invoked. Component node id: " + componentNodeId );
		
		this.componentNodeId = componentNodeId;
		this.context = context;
		
		try {
			readConfiguration();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public Collection<ComponentIdentifier> match(Message message){
		
		log.debug("match() invoked for message" + message.getMessageID());
		
		Collection<ComponentIdentifier> consumers = new ArrayList<ComponentIdentifier>();
		
		ComponentIdentifier producer = message.getHeader().getProducer();
		
		for(MatchRule mr: rules){
			
			log.debug("try match for rule" + mr.getRuleID());
			if(isMatchForNode(producer, mr)){
				if(isMatchForComponentType(producer, mr)){
					log.debug("**MATCH**");
					
					MatchTarget matchtTarget = mr.getTarget();
					ComponentIdentifier target = new ComponentIdentifier();
					target.setComponentType(matchtTarget.getComponentType());
					target.setComponentLocator(matchtTarget.getLocator());
					consumers.add(target);
				}else{
					log.debug("no match for type");
				}
			}else{
				log.debug("no match for node.");
			}
		}
		return consumers;
	}

	/////////////////////////////////////
	////////// Private Section //////////
	/////////////////////////////////////
	
	
	private boolean isMatchForNode(ComponentIdentifier producer, MatchRule mr) {
				
		boolean matchNode = false;
		boolean matchUUID = false;
		
		ComponentLocator producerLocator = producer.getComponentLocator();
		ComponentLocator criteriaLocator = mr.getTarget().getLocator();
		
		// Is the Criteria for any node?
		if(NodeAddressHelper.NO_ADDRESS.getValue().equals(criteriaLocator.getNodeAddress().getValue())){
			log.debug("isMatchForNode(): no node address match");
			matchNode = true;
		}
		// Are the nodes equals?
		else if( producerLocator.getNodeAddress().getValue().equals(criteriaLocator.getNodeAddress().getValue())){
			matchNode = true;
		}
		// If there is a match for node, check if the UUID matches
		if(matchNode){
			// Is there NO  restriction on UUID?
			if(criteriaLocator.getComponentNodeId() == null){
				matchUUID = true;
			}
			// Do we have a match
			else if( producerLocator.getComponentNodeId().equals(criteriaLocator.getComponentNodeId())){
				matchUUID = true;
			}
		}
		
		return matchNode && matchUUID;
	}

	private boolean isMatchForComponentType(ComponentIdentifier producer, MatchRule mr) {
		String criteriaType = mr.getCriteria().getComponentType();
		//Test for ALL
		if("ALL".equals(criteriaType)){
			return true;
		}
		// replace . by \\.
		criteriaType = criteriaType.replace(".", "\\.");
				
		Pattern pattern = Pattern.compile(criteriaType);
		java.util.regex.Matcher m = pattern.matcher(producer.getComponentType());
		return m.matches();
	}


	private void readConfiguration() throws JDOMException, IOException {
		
		log.debug("readConfiguration() invoked.");
		
		Document doc = readConfigurationFile();
		Element eConfig = doc.getRootElement();
		// matchRule
		List components = eConfig.getChildren("matchRule");
		for (Object o : components) {
			Element eMatchRule = (Element) o;
			Element eMatchCriteria = eMatchRule.getChild("matchCriteria");
			Element eMatchTarget = eMatchRule.getChild("matchTarget");
			
			// Rule ID
			String ruleID = eMatchRule.getChildTextTrim("ruleid");
			
			// matchCriteria
			Element eNodeAddressCriteria = eMatchCriteria.getChild("nodeAddress");
			Element eComponentTypeCriteria = eMatchCriteria.getChild("componentType");
			
			NodeAddress nodeCriteria = obtainNodeAddress(eNodeAddressCriteria);
			
			ComponentLocator locatorCriteria = new ComponentLocator();
			locatorCriteria.setComponentNodeId(null);
			locatorCriteria.setNodeAddress(nodeCriteria);
			
			MatchCriteria criteria = new MatchCriteria(eComponentTypeCriteria.getTextTrim(), locatorCriteria);
			
			// matchTarget
			Element eNodeAddressTarget = eMatchTarget.getChild("nodeAddress");
			Element eComponentTypeTarget = eMatchTarget.getChild("componentType");
			
			NodeAddress nodeTarget = obtainNodeAddress(eNodeAddressTarget);
			
			ComponentLocator locatorTarget = new ComponentLocator();
			locatorTarget.setComponentNodeId(null);
			locatorTarget.setNodeAddress(nodeTarget);
			
			MatchTarget target = new MatchTarget(eComponentTypeTarget.getTextTrim(), locatorTarget);
			
			// Create Rule
			MatchRule rule = new MatchRule(ruleID, criteria, target);
			rules.add(rule);			
		}		
	}
	
	private NodeAddress obtainNodeAddress(Element nodeAddressCriteria) {
		String nodeStr = nodeAddressCriteria.getTextTrim();
		if(nodeStr.equals("LOCAL")){
			return NodeAddressHelper.LOCAL_NODEADDRESS;
		}else if(nodeStr.equals("*")){
			return NodeAddressHelper.NO_ADDRESS;
		}else{
			NodeAddress nodeAddress = new NodeAddress();
			nodeAddress.setValue(nodeStr);
			return nodeAddress;
		}
	}

	private Document readConfigurationFile() throws JDOMException, IOException{
		String configFilePath = obtainConfigFilePath();
		
		log.info("Reading configuration file: " + configFilePath);
		
		File configFile = new File(configFilePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			log.error("Could not read configuration file", e);
			throw e;
		}
		SAXBuilder parser = new SAXBuilder();
		Document config = null;
		try {
			config = parser.build(fis);
		} catch (JDOMException e) {
			log.error("Could not parse configuration file", e);
			throw e;
		} catch (IOException e) {
			log.error("Could not parse configuration file", e);
			throw e;
		}finally{
			if(fis != null){
				try{fis.close();}catch(IOException e){};
			}
		}
		return config;
	}
	
	private String obtainConfigFilePath(){
		String path;
		
		String deploymentDir = System.getProperty("dsp.deploymentdir");
		if(deploymentDir == null){
			deploymentDir = "deployment";
		}
		
		return context.getHomePlatformDirectoryPath() + File.separator + 
		deploymentDir +  File.separator + CONFIG_FILE_NAME;
	}
}

