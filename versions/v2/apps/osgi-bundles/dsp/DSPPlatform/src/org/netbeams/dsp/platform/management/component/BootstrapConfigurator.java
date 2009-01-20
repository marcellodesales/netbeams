package org.netbeams.dsp.platform.management.component;

/**
 * Provide bootstrap configuration messages.
 * 
 * File names follow: <DSP component type>_<sequence>_<content data type>_<message category prefix>.xml
 * 
 * <DSP component type>: For instance, org.netbeams.dsp.demo.stocks.producer
 * <sequence>: '0' (zero) right padded sequence number. For instance, 001
 * <content data type>: For instance, stock.tick
 * <message category prefix>: A, C, D, E, I, M, Q, U 
 * #11/02/2008 - Kleber Sales - Creation
 *
 **/
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.AbstractMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;

public class BootstrapConfigurator {
	
	private static final Logger log = Logger.getLogger(BootstrapConfigurator.class);
	
	public static final String BOOTSTRAP_MESSAGE_DIR = "bootstrap";
	
//	private static Map<String, Class<? extends Message>> prefixToCategory = new HashMap<String, Class<? extends Message>>();
//	static{
//		prefixToCategory.put("A", ActionMessage.class);
//		prefixToCategory.put("C", CreateMessage.class);
//		prefixToCategory.put("D", DeleteMessage.class);
//		prefixToCategory.put("E", EventMessage.class);
//		prefixToCategory.put("I", InsertMessage.class);
//		prefixToCategory.put("M", MeasureMessage.class);
//		prefixToCategory.put("Q", QueryMessage.class);
//		prefixToCategory.put("U", UpdateMessage.class);
//	}
	
	private String DSP_HOME;
	
	// <DSPComponent type> ==> <list of contend data files>
	private Map<String, List<File>> contentDataFiles;
	private File[] messageFiles;
	
	
	public BootstrapConfigurator(String dspHome){
		DSP_HOME = dspHome;
		contentDataFiles = new HashMap<String, List<File>>();
	}
	
	public void init(){		
		
		String boostratpDir = DSP_HOME + File.separator + BOOTSTRAP_MESSAGE_DIR;
		
		log.info("init() invoked. Boostrap directory: " + boostratpDir);
	
		File dir = new File(boostratpDir);
		
		File[] files = dir.listFiles();
		if(files != null){
			Arrays.sort(files);
			createTypeMap(files);
			messageFiles = files;
		}
	}
	
	public List<Message> createMessages(ComponentManager componentManager, String componentType) throws DSPException {
		log.debug("BootstrapConfigurator.createMessages(): " + componentType);
		
		List<Message> messages = new ArrayList<Message>();
		
		List<File> files = contentDataFiles.get(componentType);
		if (files != null) {
		    for (File file: messageFiles){
		        //Just get messages whose file name includes "bootstrap". They contains the
		        //dsp messages container. A set of messages container can be divided by
		        //components, be sorted, etc...
		        if (file.getName().contains("bootstrap")) {
		            try {
                                JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message");
                                Unmarshaller unmarshaller = jc.createUnmarshaller();
                                MessagesContainer msgsCtn = (MessagesContainer)unmarshaller.unmarshal(file);
                                for(AbstractMessage abstrMsg: msgsCtn.getMessage()) {
                                    messages.add((Message)abstrMsg);
                                }
                            }catch(JAXBException e){
                                e.printStackTrace();
                            }
		        }
		    }
		}
		
		/*
		// Messages are supposed to be local. Create local consumers identifiers
		ComponentLocator locator = new ComponentLocator(null, NodeAddressHelper.LOCAL_NODEADDRESS);
		
		List<File> files = contentDataFiles.get(componentType);
		if(files != null){
			for(File file: files){
				String[] nameParts = parserFileName(file.getName());
				byte[] data = readContentData(file);
				if(data == null){
					Log.log("Size 0(zero) for " + file.getName());
					continue;
				}
				Class<? extends Message> messageClass = getMessageClass(nameParts);
				Message message = 
					MessageFactory.newMessage(messageClass, componentManager, nameParts[2], data);
				
				// Messages are supposed to be local. Create local consumers identifiers
				ComponentIdentifier identifier = new ComponentIdentifier(nameParts[0], locator);
				message.addConsumer(identifier);
				
				messages.add(message);
			}		
		}
		
		*/
		log.info("createMessages(): #" + messages.size());
		return messages;
	}

	/**
	 * Create a map from component type to File
	 * @param files
	 */
	private void createTypeMap(File[] files) {
		for(File file: files){
			String name = file.getName();
			// [<DSP component type>_<sequence>]
			String[] nameParts = parserFileName(name);
			// Make sure the file name is valid
			if(validateFileName(nameParts)){
				String componentName = nameParts[0];
				List<File> dataFiles = contentDataFiles.get(componentName);
				if(dataFiles == null){
					dataFiles = new ArrayList<File>();
					contentDataFiles.put(componentName, dataFiles);
				}
				log.info("Add file " + name + " to " + componentName);
				dataFiles.add(file);
			}else{
				log.warn("Invalid content file name " + name);
			}
		}		
	}

	private boolean validateFileName(String[] nameParts) {
		if(nameParts == null || nameParts.length != 2){
			return false;
		}
		for(int x=0; x<2; ++x){
			if(nameParts[x] == null){
				return false;
			}
		}
		return true;
	}

	private String[] parserFileName(String name) {
		String[] nameParts = new String[2];
		// Remove .xml extension
		int i = name.lastIndexOf('.');
		if(i > -1){
			name = name.substring(0, i);
		}
		StringTokenizer tokenizer = new StringTokenizer(name, "_");
		i=0;
		while(tokenizer.hasMoreTokens() && i < 4){
			nameParts[i++] = tokenizer.nextToken();
		}
		return nameParts;
	}

	

}
