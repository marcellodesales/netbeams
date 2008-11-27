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
import java.util.UUID;
import java.util.regex.Pattern;

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
import org.netbeams.dsp.util.Log;

public class Matcher implements BaseComponent {
	
	private static final String CONFIG_FILE_NAME = "matcher_config.xml";

	private String componentNodeId;
	private DSPContext context;
	private MessageBrokerAccessor brokerAccessor;
	
	private String HOME_PATH;
	
	private Object lock;
	
	List<MatchRule> rules;
	
	public Matcher(String homePath){
		HOME_PATH = homePath;
		lock = new Object();
		rules = new ArrayList<MatchRule>();
	}

	@Override
	public String getComponentType() {
		return GlobalComponentTypeName.MATCHER;
	}

	@Override
	public String getComponentNodeId() {
		return componentNodeId;
	}
	
	public void initComponent(String componentNodeId, DSPContext context) throws DSPException{
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
		
		Collection<ComponentIdentifier> consumers = new ArrayList<ComponentIdentifier>();
		
		ComponentIdentifier producer = message.getHeader().getProducer();
		
		for(MatchRule mr: rules){
			if(isMatchForNode(producer, mr)){
				if(isMatchForComponentType(producer, mr)){
					MatchTarget matchtTarget = mr.getTarget();
					ComponentIdentifier target = new ComponentIdentifier();
					target.setComponentType(matchtTarget.getComponentType());
					target.setComponentLocator(matchtTarget.getLocator());
					consumers.add(target);
				}
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
		if(NodeAddressHelper.NO_ADDRESS.equals(criteriaLocator.getNodeAddress())){
			matchNode = true;
		}
		// Are the nodes equals?
		else if( producerLocator.getNodeAddress().equals(criteriaLocator.getNodeAddress())){
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
		
		Log.log("criteriaType=" + criteriaType);
		Log.log("component type=" + producer.getComponentType());
		
		Pattern pattern = Pattern.compile(criteriaType);
		java.util.regex.Matcher m = pattern.matcher(producer.getComponentType());
		return m.matches();
	}


	private void readConfiguration() throws JDOMException, IOException {
		Document doc = readConfigurationFile();
		Element eConfig = doc.getRootElement();
		// matchRule
		List components = eConfig.getChildren("matchRule");
		for (Object o : components) {
			Element eMatchRule = (Element) o;
			Element eMatchCriteria = eMatchRule.getChild("matchCriteria");
			Element eMatchTarget = eMatchRule.getChild("matchTarget");
			
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
			locatorCriteria.setComponentNodeId(null);
			locatorCriteria.setNodeAddress(nodeTarget);
			
			MatchTarget target = new MatchTarget(eComponentTypeTarget.getTextTrim(), locatorTarget);
			
			// Create Rule
			MatchRule rule = new MatchRule(criteria, target);
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
		String configFilePath = HOME_PATH + File.separator + CONFIG_FILE_NAME;
		File configFile = new File(configFilePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			Log.log(e);
			throw e;
		}
		SAXBuilder parser = new SAXBuilder();
		Document config = null;
		try {
			config = parser.build(fis);
		} catch (JDOMException e) {
			Log.log(e);
			throw e;
		} catch (IOException e) {
			Log.log(e);
			throw e;
		}finally{
			if(fis != null){
				try{fis.close();}catch(IOException e){};
			}
		}
		return config;
	}
}

