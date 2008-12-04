package org.netbeams.dsp.platform.management.component;

/**
 * Provide bootstrap configuratio messages.
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageFactory;
import org.netbeams.dsp.NodeAddressHelper;
import org.netbeams.dsp.message.*;

import org.netbeams.dsp.util.Log;

public class BootstrapConfigurator {
	
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
		Log.log("BootstrapConfigurator.init");
		
		File dir = new File(DSP_HOME + File.separator + BOOTSTRAP_MESSAGE_DIR);
		File[] files = dir.listFiles();
		if(files != null){
			Arrays.sort(files);
			createTypeMap(files);
			messageFiles = files;
		}else{
			Log.log("No files in " + dir.getAbsolutePath());
		}
	}
	
	public List<Message> createMessages(ComponentManager componentManager, String componentType) throws DSPException {
		Log.log("BootstrapConfigurator.createMessages(): " + componentType);
		
		List<Message> messages = new ArrayList<Message>();
		
		List<File> files = contentDataFiles.get(componentType);
		if(files != null){
			for(File file: messageFiles){
				try{
					JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message", 
							org.netbeams.dsp.message.ObjectFactory.class.getClassLoader());
					Unmarshaller unmarshaller = jc.createUnmarshaller();
					Message message = 
							(MeasureMessage)unmarshaller.unmarshal(file);
					messages.add(message);
				}catch(JAXBException e){
					Log.log(e);
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
		Log.log("BootstrapConfigurator.createMessages(): #" + messages.size());
		return messages;
	}


//	private Class<? extends Message> getMessageClass(String[] nameParts) {
//		return prefixToCategory.get(nameParts[3]);
//	}

	private byte[] readContentData(File file) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			int n = 0;
			while((n=fis.read(buff)) != -1){
				baos.write(buff, 0, n);
			}
		}catch(IOException e){
			Log.log(e);
		}finally{
			if(fis != null){
				try { fis.close(); } catch (IOException e) {}
			}
		}
		return baos.toByteArray();
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
				String componentType = nameParts[0];
				List<File> dataFiles = contentDataFiles.get(componentType);
				if(dataFiles == null){
					dataFiles = new ArrayList<File>();
					contentDataFiles.put(componentType, dataFiles);
				}
				Log.log("Add " + name + " to " + componentType);
				dataFiles.add(file);
			}else{
				Log.log("BootstrapConfigurator.Invalid content file name " + name);
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
