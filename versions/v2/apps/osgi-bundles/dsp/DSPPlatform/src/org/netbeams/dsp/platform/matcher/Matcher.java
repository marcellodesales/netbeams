package org.netbeams.dsp.platform.matcher;

/**
 * This is a basic implementation of the Matcher.
 * 
 * This initial verions is meant only for the initial prototype. It re-creates the matching information everytime is
 * starts. There is no persistence.
 * 
 * A better implementation would be persist the matching information and ask the register the latest updates since last
 * update.
 * 
 * I still need to think a little bit more on how the callback mechanism will work with this update.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.AbstractNodeAddress;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.NodeAddress;
import org.netbeams.dsp.util.NetworkUtil;

public class Matcher implements BaseComponent {

    private static final Logger log = Logger.getLogger(Matcher.class);

    private static final String CONFIG_FILE_NAME = "matcher_config.xml";
    
    private static final String LOCAL_IP = NetworkUtil.getCurrentEnvironmentNetworkIp();

    private String componentNodeId;
    private DSPContext context;

    Set<MatchRule> rules;

    public Matcher() {
        rules = new HashSet<MatchRule>();
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

    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {

        log.info("initComponent() invoked. Component node id: " + componentNodeId);

        this.componentNodeId = componentNodeId;
        this.context = context;

        try {
            readConfiguration();
        } catch (JDOMException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Collection<MatchRule> match(Message message) {
        log.debug("Find match for Message ID " + message.getMessageID());
        
        ComponentIdentifier producer = message.getHeader().getProducer();
        ComponentIdentifier consumer = message.getHeader().getConsumer();
        
        Collection<MatchRule> matchedRules = new HashSet<MatchRule>();
        
        for (MatchRule mr : rules) {
            log.debug("Rule: " + mr.getRuleID());
            
            boolean isMatch = false;
            
            if  (isMatchForProducerType(producer, mr)) {
            	if (isMatchForProducerAddress(producer, mr)){
            		if (isMatchForConsumerType(consumer, mr)){
            			if (isMatchForConsumerAddress(consumer, mr)){
            				 isMatch = true;
            			}
            		}
            	}
            } 
            if (isMatch){
            	matchedRules.add(mr);
            }else {
                log.debug("NO Match found for rule...");
            }
        }       
        return matchedRules;
    }

    /////////////////////////////////////
    ////////// Private Section //////////
    /////////////////////////////////////

    private boolean isMatchForProducerType(ComponentIdentifier producer, MatchRule mr) {
        String prodType = mr.getCriteria().getProducerType();
        // Test for ALL
        boolean isMatch = false;
        if ("ALL".equals(prodType) || "ANY".equals(prodType)) {
            isMatch = true;
        } 
        if (!isMatch){
	        // replace . by \\.
        	prodType = prodType.replace(".", "\\.");        
	        java.util.regex.Matcher prodMatcher = Pattern.compile(prodType).matcher(producer.getComponentType());
	
	        isMatch = isMatch || prodMatcher.matches();
        }
        if(isMatch){
        	log.debug("Producer type matches " + prodType);
        }else{
        	log.debug("Producer type does not match " + prodType);
        }
        return isMatch;
    }

    // TODO: Fix add proper logic or delete it
    private boolean isMatchForProducerAddress(ComponentIdentifier producer, MatchRule mr) {
        String prodAddress = mr.getCriteria().getProducerLocator().getNodeAddress().getValue();
        // Test for ALL
        boolean isMatch = false;
        if ("ALL".equals(prodAddress) || "ANY".equals(prodAddress)) {
            isMatch = true;
            log.debug("Producer address matches " + prodAddress);
        }else{
        	log.debug("Producer address does not match " + prodAddress);
        }
        return isMatch;
    }  
  
    private boolean isMatchForConsumerType(ComponentIdentifier consumer, MatchRule mr) {
        String consType = mr.getCriteria().getConsumerType();
        // Test for ALL
        boolean isMatch = false;
        if ("ANY".equals(consType) && consumer != null) {
            isMatch = true;
        } 
        
        if(!isMatch){
	        if ("NONE".equals(consType) && consumer == null) {
	            isMatch = true;
	        }  
        }
        
        if(!isMatch){
        	if(consumer != null){
    	        // replace . by \\.
        		consType = consType.replace(".", "\\.");        
    	        java.util.regex.Matcher regMatcher = Pattern.compile(consType).matcher(consumer.getComponentType());
    	
    	        isMatch = isMatch || regMatcher.matches();       		
        	}
        }
        
        if(isMatch){
        	log.debug("Consumer type matches " + consType);
        }else{
        	log.debug("Consumer type does not match " + consType);
        }
        return isMatch;
    }    

    
    private boolean isMatchForConsumerAddress(ComponentIdentifier consumer, MatchRule mr) {
    	String consAddress = mr.getCriteria().getConsumerLocator().getNodeAddress().getValue();
        // Test for ALL
        boolean isMatch = false;
        if ("NONE".equals(consAddress) && consumer == null) {
            isMatch = true;
        } 
        
        if(!isMatch){
        	// ANY
	        if ("ANY".equals(consAddress) && consumer != null) {
	            isMatch = true;
	        }  
        }
        
        if(!isMatch){
        	// LOCAL
	        if ("LOCAL".equals(consAddress) &&consumer != null && 
	        		isLocal(consumer.getComponentLocator().getNodeAddress().getValue())) 
	        {
	            isMatch = true;
	        }  
        }
 
        if(!isMatch){
        	// REMOTE
	        if ("REMOTE".equals(consAddress) && consumer != null && !isLocal(consumer.getComponentLocator().getNodeAddress().getValue())) {
	            isMatch = true;
	        }  
        }
        if(!isMatch){
        	if(consumer != null){
    	        // replace . by \\.
        		consAddress = consAddress.replace(".", "\\.");        
    	        java.util.regex.Matcher regMatcher = Pattern.compile(consAddress).matcher(consumer.getComponentType());
    	
    	        isMatch = isMatch || regMatcher.matches();       		
        	}
        }
        
        if(isMatch){
        	log.debug("Consumer address matches " + consAddress);
        }else{
        	log.debug("Consumer address does not match " + consAddress);
        }
        return isMatch;
    }       

	private void readConfiguration() throws JDOMException, IOException {

        Document doc = readConfigurationFile();
        Element eConfig = doc.getRootElement();
        // matchRule
        List rulesConfig = eConfig.getChildren("matchRule");
        for (Object o : rulesConfig) {
            Element eMatchRule = (Element) o;
            // Rule ID
            String ruleID = eMatchRule.getChildTextTrim("ruleid");

            // Setting up the criteria
            Element matchCriteriaElement = eMatchRule.getChild("matchCriteria");
            
            String producerType = matchCriteriaElement.getChildTextTrim("producerType");          
            AbstractNodeAddress producerAddress = obtainNodeAddress(matchCriteriaElement.getChild("producerAddress"));
            
            ComponentLocator producerLocator = new ComponentLocator();
            producerLocator.setComponentNodeId(null);
            producerLocator.setNodeAddress(producerAddress);
            
            String consumerType = matchCriteriaElement.getChildTextTrim("consumerType");          
            AbstractNodeAddress consumerTypeAddress = obtainNodeAddress(matchCriteriaElement.getChild("consumerAddress"));
            
            ComponentLocator consumerLocator = new ComponentLocator();
            consumerLocator.setComponentNodeId(null);
            consumerLocator.setNodeAddress(consumerTypeAddress);
           

            MatchCriteria criteria = new MatchCriteria(producerType, producerLocator, consumerType, consumerLocator);

            // Setting up the target
            Element matchTargetElement = eMatchRule.getChild("matchTarget");
            String consumerTypeTarget = matchTargetElement.getChildTextTrim("consumerType");
            NodeAddress consumerAddressTarget = obtainNodeAddress(matchTargetElement.getChild("consumerAddress"));

            ComponentLocator consumerLocatorTarget = new ComponentLocator();
            consumerLocatorTarget.setComponentNodeId(null);
            consumerLocatorTarget.setNodeAddress(consumerAddressTarget);
            
            String gateway = matchTargetElement.getChildTextTrim("gateway");

            MatchTarget target = new MatchTarget(consumerTypeTarget, consumerLocatorTarget, gateway);

            // Create Rule
            MatchRule rule = new MatchRule(ruleID, criteria, target);
            rules.add(rule);
        }
        log.debug(rulesConfig.size() + " rules successfully parsed from match_config.xml... ");
        log.debug(rules.size() + " different rules recognized as valid, non-repeated rules... ");
    }

    private NodeAddress obtainNodeAddress(Element nodeAddress) {
        String nodeStr = nodeAddress.getTextTrim();
        if (nodeStr.equals("LOCAL")) {
            return NodeAddressHelper.LOCAL_NODEADDRESS;
        } else if (nodeStr.equals("ANY")) {
            return NodeAddressHelper.ANY_ADDRESS;
        } else if (nodeStr.equals("NONE")) {
            return NodeAddressHelper.NONE_ADDRESS;
        } else {
            NodeAddress nodeAddressDsp = new NodeAddress();
            nodeAddressDsp.setValue(nodeStr);
            return nodeAddressDsp;
        }
    }

    private boolean isLocal(String address) {
		
		return "LOCAL".equalsIgnoreCase(address) || "LOCALHOST".equalsIgnoreCase(address) ||
        	"127.0.0.1".equals(address) || address.equalsIgnoreCase(LOCAL_IP);
	}

    private Document readConfigurationFile() throws JDOMException, IOException {
        String configFilePath = obtainConfigFilePath();

        log.info("Reading rules configuration file: " + configFilePath);

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
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {}
            }
        }
        return config;
    }

    private String obtainConfigFilePath() {

        String deploymentDir = System.getProperty("dsp.deploymentdir");
        if (deploymentDir == null) {
            deploymentDir = "deployment";
        }

        return context.getHomePlatformDirectoryPath() + File.separator + deploymentDir + File.separator
                + CONFIG_FILE_NAME;
    }
    
    
}
