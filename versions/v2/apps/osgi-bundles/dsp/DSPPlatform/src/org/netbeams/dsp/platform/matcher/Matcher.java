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
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeams.dsp.BaseComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.GlobalComponentTypeName;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.ComponentLocator;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.NodeAddress;

public class Matcher implements BaseComponent {

    private static final Logger log = Logger.getLogger(Matcher.class);

    private static final String CONFIG_FILE_NAME = "matcher_config.xml";

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Collection<MatchRule> match(Message message) {

        log.debug("Finding a matcher for Message Content Type " + message.getContentType());
        log.debug("message ID: " + message.getMessageID());

        Collection<MatchRule> consumers = new HashSet<MatchRule>();

        ComponentIdentifier producer = message.getHeader().getProducer();
        ComponentIdentifier consumer = message.getHeader().getConsumer();
        for (MatchRule mr : rules) {
            log.debug("Verifying matcher for rule: " + mr.getRuleID() + " Is Default? " + mr.isDefault());
            log.debug("Message's producer: " + producer.getComponentType());
            log.debug("Message's consumer: " + consumer.getComponentType());
            log.debug("component type matches rule criteria?");
            log.debug("Rule Criteria: Producer=" + mr.getCriteria().getProducerComponentType());
            log.debug("Rule Criteria: Consumer=" + mr.getCriteria().getConsumerComponentType());
            if (isMatchForComponentType(producer, consumer, mr)) {
                log.debug("Producer matches for producer and consumer types...");
                consumers.add(mr);
            } else {
                log.debug("Message doesn't match on Component Type");
            }
        }
        return consumers;
    }

    // ///////////////////////////////////
    // //////// Private Section //////////
    // ///////////////////////////////////

    private boolean isMatchForComponentType(ComponentIdentifier producer, ComponentIdentifier consumer, MatchRule mr) {
        String prodCriteriaType = mr.getCriteria().getProducerComponentType();
        // Test for ALL
        boolean producerValidation = false;
        if ("ALL".equals(prodCriteriaType) || "ANY".equals(prodCriteriaType)) {
            log.debug("Producer Criteria is ALL/ANY, so matches criteria!");
            producerValidation = true;
        }
        String consCriteriaType = mr.getCriteria().getConsumerComponentType();
        boolean consumerValidation = false;
        if ("ALL".equals(consCriteriaType) || "ANY".equals(consCriteriaType)) {
            log.debug("Consumer Criteria is ALL/ANY, so matches criteria!");
            consumerValidation = true;
        }
        if (consumerValidation && producerValidation) {
            return true;
        }
        // replace . by \\.
        prodCriteriaType = prodCriteriaType.replace(".", "\\.");
        consCriteriaType = consCriteriaType.replace(".", "\\.");
        
        java.util.regex.Matcher prodMatcher = Pattern.compile(prodCriteriaType).matcher(producer.getComponentType());
        java.util.regex.Matcher consMatcher = Pattern.compile(consCriteriaType).matcher(consumer.getComponentType());
        
        return prodMatcher.matches() && consMatcher.matches();
    }

    private void readConfiguration() throws JDOMException, IOException {

        log.debug("Reading matcher_config.xml to register matching rules...");

        Document doc = readConfigurationFile();
        Element eConfig = doc.getRootElement();
        // matchRule
        List rulesConfig = eConfig.getChildren("matchRule");
        for (Object o : rulesConfig) {
            Element eMatchRule = (Element) o;
            Attribute isDefaultAttr = eMatchRule.getAttribute("default");
            boolean isDefaultRule = false;
            if (isDefaultAttr != null) {
                isDefaultRule = isDefaultAttr.getBooleanValue();
            }
            Element eMatchCriteria = eMatchRule.getChild("matchCriteria");
            Element eMatchTarget = eMatchRule.getChild("matchTarget");

            // Rule ID
            String ruleID = eMatchRule.getChildTextTrim("ruleid");

            // matchCriteria
            Element eNodeAddressCriteria = eMatchCriteria.getChild("nodeAddress");
            Element eProdComponentTypeCriteria = eMatchCriteria.getChild("producerComponentType");
            Element eConsComponentTypeCriteria = eMatchCriteria.getChild("consumerComponentType");

            NodeAddress nodeCriteria = obtainNodeAddress(eNodeAddressCriteria);

            ComponentLocator locatorCriteria = new ComponentLocator();
            locatorCriteria.setComponentNodeId(null);
            locatorCriteria.setNodeAddress(nodeCriteria);

            MatchCriteria criteria = new MatchCriteria(eProdComponentTypeCriteria.getTextTrim(),
                    eConsComponentTypeCriteria.getTextTrim(), locatorCriteria);

            // matchTarget
            Element eNodeAddressTarget = eMatchTarget.getChild("nodeAddress");
            Element eComponentTypeTarget = eMatchTarget.getChild("componentType");
            Element eGatewayComponentTypeTarget = eMatchTarget.getChild("gatewayComponentType");

            NodeAddress nodeTarget = obtainNodeAddress(eNodeAddressTarget);

            ComponentLocator locatorTarget = new ComponentLocator();
            locatorTarget.setComponentNodeId(null);
            locatorTarget.setNodeAddress(nodeTarget);

            String gatewayCompTyp = eGatewayComponentTypeTarget == null ? null : eGatewayComponentTypeTarget
                    .getTextTrim();
            MatchTarget target = new MatchTarget(eComponentTypeTarget.getTextTrim(), locatorTarget, gatewayCompTyp);

            // Create Rule
            MatchRule rule = new MatchRule(ruleID, isDefaultRule, criteria, target);
            rules.add(rule);
        }
        log.debug(rulesConfig.size() + " rules successfully parsed from match_config.xml... ");
        log.debug(rules.size() + " different rules recognized as valid, non-repeated rules... ");
    }

    private NodeAddress obtainNodeAddress(Element nodeAddressCriteria) {
        String nodeStr = nodeAddressCriteria.getTextTrim();
        if (nodeStr.equals("LOCAL")) {
            return NodeAddressHelper.LOCAL_NODEADDRESS;
        } else if (nodeStr.equals("*")) {
            return NodeAddressHelper.NO_ADDRESS;
        } else {
            NodeAddress nodeAddress = new NodeAddress();
            nodeAddress.setValue(nodeStr);
            return nodeAddress;
        }
    }

    private Document readConfigurationFile() throws JDOMException, IOException {
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
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
                ;
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
