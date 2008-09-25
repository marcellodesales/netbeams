package edu.sfsu.netbeams.web;

/*
 * Created on Jun 3, 2005
 *
 */


import javax.servlet.http.*;
import javax.servlet.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import org.python.core.*;
import edu.sfsu.dmp.*;
import edu.sfsu.dmp.event.*;
import edu.sfsu.dmp.data.sensordata.SensorData;
import edu.sfsu.netbeams.web.dmpstate.*;
import javax.xml.bind.*;
import edu.sfsu.dmp.data.configuration.*;
import edu.sfsu.dmp.data.configuration.Parameter;


public class StartupServlet extends HttpServlet implements PluginListener, PropertyChangeListener {

	public void init() throws ServletException {
		System.out.println("I AM HERE");
				
		// start KPI jddac client
//		JddacClient.main(new String[]{});

		
		DataManagementPlatform platform = DataManagementPlatform.getInstance();
		platform.addPluginListener(this);
		
		this.loadDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
		this.loadSensors(NetBEAMSConstants.SENSORS_CONFIGURATIONS);
		this.loadKPIs(NetBEAMSConstants.KPIS_CONFIGURATIONS);		
	}
	

	
	
	protected void loadDMPState (String dmpConfigFile) {
		try {
			File file = new File(dmpConfigFile);
			if (file.exists()) {
				JAXBContext jc = JAXBContext.newInstance(DMPState.class);
				Unmarshaller ms = jc.createUnmarshaller();
				
				DataManagementPlatform platform = DataManagementPlatform.getInstance();
				DMPState dmpState = (DMPState)ms.unmarshal(file);
				
				if (dmpState.getPluginTypes() != null) {
					for (PluginType type : dmpState.getPluginTypes()) {
						try {
							platform.addPluginType(type.getClassName());
						} catch (Exception e) {
							NetBeamsLogger.logErrorMessages(e);
						}
					}
				}


				if (dmpState.getPlugInstances() != null) {
					for (PluginInstance instance : dmpState.getPlugInstances()) {
						try {
							Class pluginClass = Class.forName(instance.getType());
							Plugin plugin = (Plugin)pluginClass.getConstructor(String.class).newInstance(instance.getId());
							
							platform.addPluginInstance(plugin);
							
							if (instance.getConfiguration() != null) {
								ConfigurationData config = new ConfigurationData();
								for (edu.sfsu.netbeams.web.dmpstate.Parameter param : instance.getConfiguration().getParameters()) {
									edu.sfsu.dmp.data.configuration.Parameter parameter = new edu.sfsu.dmp.data.configuration.Parameter();
									parameter.setName(param.getName());
									parameter.setValue(param.getValue());
									config.getParameters().add(parameter);
								}
								plugin.setConfiguration(config);
							}
							
							plugin.setName(instance.getName());
							plugin.setDescription(instance.getDescription());
						} catch (Exception e) {
							NetBeamsLogger.logErrorMessages(e);
						}
					}
				}
			}
		} catch (Exception e) {
			NetBeamsLogger.logErrorMessages(e);
		}
	}
	
	
	
	protected void storeDMPState (String dmpConfigFile) {
		try {
			File file = new File(dmpConfigFile);
			

			DataManagementPlatform platform = DataManagementPlatform.getInstance();
			DMPState dmpState = new DMPState();
			
			for (Class c : platform.getAllPluginTypes()) {
				PluginType type = new PluginType();
				type.setClassName(c.getCanonicalName());
				dmpState.getPluginTypes().add(type);
			}
			
			
			for (Plugin plugin : platform.getAllPluginInstances()) {
				PluginInstance instance = new PluginInstance();
				instance.setId(plugin.getId());				
				instance.setName(plugin.getName());
				instance.setDescription(plugin.getDescription());
				instance.setType(plugin.getClass().getCanonicalName());
				
				if (plugin.getConfiguration() != null) {
					Configuration config = new Configuration();
					for (edu.sfsu.dmp.data.configuration.Parameter p : plugin.getConfiguration().getParameters()) {
						edu.sfsu.netbeams.web.dmpstate.Parameter param = new edu.sfsu.netbeams.web.dmpstate.Parameter();
						param.setName(p.getName());
						param.setValue(p.getValue());
						config.getParameters().add(param);
					}
					instance.setConfiguration(config);
				}
				dmpState.getPlugInstances().add(instance);
			}

			
/*
			PluginType type = new PluginType();
			type.setClassName(NRSSDataProducer.class.getCanonicalName());
			dmpState.getPluginTypes().add(type);
			
			PluginInstance instance = new PluginInstance();
			instance.setId("12345");
			instance.setType(type.getClassName());
			Configuration con = new Configuration();
			con.getParameters().add(new edu.sfsu.netbeams.web.dmpstate.Parameter());
			con.getParameters().add(new edu.sfsu.netbeams.web.dmpstate.Parameter());
			instance.setConfiguration(con);
			dmpState.getPlugInstances().add(instance);
*/			
			
			JAXBContext jc = JAXBContext.newInstance(DMPState.class);
			Marshaller ms = jc.createMarshaller();
			ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			ms.marshal(dmpState, new FileOutputStream(file));
		} catch (Exception e) {
			NetBeamsLogger.logErrorMessages(e);
		}
	}
	
	
	
	protected void loadSensors (String sensorConfigurationsFile) {
		try {
/*
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(sensorConfigurationsFile));
		
			SensorManagementPlatformController controller = SensorManagementPlatform.getInstance().getController();

			Element root = document.getRootElement();
			Iterator i = root.getChildren().iterator();
			while(i.hasNext()) {
				try {
					Element element = (Element)i.next();
					
					String id = element.getAttributeValue("id");
					int updateFreq = Integer.parseInt(element.getAttributeValue("updateFrequency"));
					String dataProducerName = element.getAttributeValue("dataProducerName");
					boolean isPublic = Boolean.parseBoolean(element.getAttributeValue("public"));

					String name = element.getChildTextTrim("name");
					String description = element.getChildTextTrim("description");
					String configurations = element.getChildTextTrim("configurations");

					DataProducerPlugin dp = controller.getDataProducerPlugin(dataProducerName);
					if (dp != null) {
						while (true) {
							try {
								controller.addSensor(id, dp, configurations.split("\n"), name, description, updateFreq, isPublic);
								break;
							} catch (Exception e) {
								System.out.println("Unable to initialize sensor name '" + name + "'. Trying again...");
							}
						}
					} else {
						throw new Exception("Can not find data producer name '" + dataProducerName + "'. Make sure you load all the data producers configuration before loading the sensors configuration.");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					NetBEAMSConstants.logs(e.getMessage());
				}
			}

*/
		} catch (Exception e) {
			NetBeamsLogger.logErrorMessages(e);
		}
	}
	
	
	

	protected void loadKPIs (String kpisFile) {
/*		
		try {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(kpisFile));
		
			SensorManagementPlatform smp = SensorManagementPlatform.getInstance();
			KPIManager kpiManager = KPIManager.getInstance();
	
			Element root = document.getRootElement();
			Iterator i = root.getChildren().iterator();
			while (i.hasNext()) {
				try {
					Element element = (Element)i.next();
					String kpiName = element.getAttributeValue("name");
					String kpiDescription = element.getAttributeValue("description");
					int trigger = Integer.parseInt(element.getAttributeValue("trigger"));
					
					String sensorIDs = element.getAttributeValue("sensors").trim();
					Sensor[] sensors = null;
					if (sensorIDs.length() > 0) {
						String[] temp = sensorIDs.split(",");
						sensors = new Sensor[temp.length];
						for (int j = 0; j < temp.length; j++) {
							sensors[j] = smp.getSensor(temp[j]);
						}
					} else {
						sensors = new Sensor[0];
					}
					
					String kpiRule = element.getChild("rule").getTextTrim();
					
					// get metadata
					Hashtable<String, String> metadata = new Hashtable<String, String>();
					Iterator it = element.getChildren("metadata").iterator();
					while (it.hasNext()) {
						Element metadataElement = (Element)it.next();
						String name = metadataElement.getAttributeValue("name");
						String value = metadataElement.getAttributeValue("value");
						metadata.put(name, value);
					}
					
					try {
						KPI kpi = kpiManager.addKPI(kpiName, kpiDescription, trigger, sensors, metadata, kpiRule);
						(new Thread(kpi)).start();
					} catch (Exception e) {
						NetBEAMSConstants.logs("Error loading KPIs: " + e.getMessage());
					}
					

				} catch (Exception e) {
					System.out.println(e.getMessage());
					NetBEAMSConstants.logs(e.getMessage());
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			NetBEAMSConstants.logs(e.getMessage());
		}
*/
	}




	@Override
	public void pluginInstanceAdded(PluginEvent event) {
		event.getPluginInstance().addPropertyChangeListener(this);
		this.storeDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
	}


	@Override
	public void pluginInstanceRemoved(PluginEvent event) {
		event.getPluginInstance().removePropertyChangeListener(this);
		this.storeDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
	}


	@Override
	public void pluginTypeAdded(PluginEvent event) {
		this.storeDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
	}


	@Override
	public void pluginTypeRemoved(PluginEvent event) {
		this.storeDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
	}




	@Override
	public void propertyChange(PropertyChangeEvent event) {
		this.storeDMPState(NetBEAMSConstants.DMP_CONFIGURATION);
	}
}








