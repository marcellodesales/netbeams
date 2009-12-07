package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
import org.netbeams.dsp.persistence.model.location.SensorLocation;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * This class is responsible for the CRUD services with the mongoDB. Although a regular CRUD service includes the Update
 * (U) of the CRUD, it will NOT be implemented as data in sensor networks DOES NOT CHANGE.
 * 
 * C - Create: Any document created will be created <li>sensor_ip_address - the ip of the sensor <li>message_id - the
 * identification of the DSP message that transferred the message <li>transaction_time - the time when the data was
 * received by the server. <li>fact_time - the time when the data was transferred from the sensor. <li>data - the actual
 * sample data
 * 
 * R - Retrieve: Retrieve any created documents from the mongoDB given the message type U - Update: NOT Implemented as
 * data can't be updated. D - Delete: Deletes an item from a collection.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 * 
 */
public enum DSPMongoCRUDService {

    INSTANCE();

    /**
     * Default logger
     */
    private static final Logger log = Logger.getLogger(DSPMongoCRUDService.class);

    private static final DecimalFormat ONE_DECIMAL_FORMATTER = new DecimalFormat("###0.0");
    private static final DecimalFormat TWO_DECIMALS_FORMATTER = new DecimalFormat("###0.00");
    private static final DecimalFormat THREE_DECIMALS_FORMATTER = new DecimalFormat("###0.000");

    private static final boolean SETUP_INDEXES = true;
    /**
     * The default mongoDB database name for the project.
     */
    private static final String NETBEAMS_DATASTORE_NAME = "netbeams";
    /**
     * The local cache for the collection names based on the content type
     */
    private static final Map<String, DBCollection> netBeamscollectionsCache = new HashMap<String, DBCollection>();
    /**
     * The IP address of the mongo server. Either the single server of the cluster head when in sharded environment
     */
    private String serverIpAddress;
    private int serverPortNumber;
    /**
     * Tracks if the service was initialized before being used. After getting the INSTANCE instance, the method
     * initialize must be called.
     */
    private static boolean initialized;
    /**
     * The list of properties to be included. temperature = true salinity = true
     */
    private Map<String, Boolean> includeProperty = new HashMap<String, Boolean>(15);

    private static boolean finishedInit;

    /**
     * Initializes the mongo CRUD service, with the given IP address from the mongo server, the current list of known
     * sensors and the list of desired properties to be persisted for the YSI sonde data.
     * 
     * @param serverIpAddress is the IP address of the mongo server. Must be numerical IP address or machine name.
     *            (127.0.0.1, localhost).
     * @param sensorsLocation is the list of known sensor locations
     * @param propertiesList is the list of attributes to be persisted in the database, as a string delimited by a comma
     *            ",". For instance "salinity,temperature,turbinity".
     * @throws UnknownHostException
     * @throws MongoException
     */
    public void initialize(String serverIpAddress, int serverPortNumber, Set<SensorLocation> sensorsLocation, String propertiesList)
            throws UnknownHostException, MongoException {
        initialized = true;
        this.serverIpAddress = serverIpAddress;
        this.serverPortNumber = serverPortNumber;
        this.setupSelectedProperties(propertiesList);
        finishedInit = true;
        log.info("Persistence Service Initialized...");
    }

    /**
     * @return if the service has been initialized.
     */
    public static boolean hasFinished() {
        return finishedInit;
    }

    /**
     * Sets the selected properties to be selected when making data persistent.
     * 
     * @param propertiesList is the list of properties delimited by comma ",".
     */
    private void setupSelectedProperties(String propertiesList) {
        if (propertiesList == null || propertiesList.equals("") && !propertiesList.contains(",")) {
            // all properties will be selected.
            return;
        }
        propertiesList = propertiesList.trim().replace(" ", "").toLowerCase();
        StringTokenizer tokenizer = new StringTokenizer(propertiesList, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            this.includeProperty.put(token, true);
        }
        log.info("Setup Properties List: " + propertiesList);
    }

    /**
     * @param persistentMessage is the DSP Message that was persisted after transmission.
     * @return the DBCollection instance based on the Message Content Type from the DSP Message that transmitted from
     *         the PersistentMessageUnit instance.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public DBCollection getPersistenceStorage(PersistentMessageUnit persistentMessage) throws UnknownHostException,
            MongoException {
        DBCollection dbCollection = netBeamscollectionsCache.get(persistentMessage.getMessageContentType());
        if (dbCollection == null) {
            DB netbeamsDb = this.getNetbeamMongoDb();
            dbCollection = netbeamsDb.getCollection(persistentMessage.getMessageContentType());
            netBeamscollectionsCache.put(persistentMessage.getMessageContentType(), dbCollection);
            if (SETUP_INDEXES) {
                this.setupMetadataIndexes(dbCollection);
                this.setupDataIndexes(dbCollection);
            }
        }
        return dbCollection;
    }

    /**
     * @return the default Mongo instance based on the default NETBEAMS_DATASTORE_NAME.
     * @param serverIpAddress is the IP address of the server where the mongo DB is running. It is the main DB server,
     *            on a single db environment, or the main head of the mongo DB cluster in a sharded environment.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public DB getNetbeamMongoDb() throws UnknownHostException, MongoException {
        Mongo dbInstance = new Mongo(this.serverIpAddress, this.serverPortNumber);
        return dbInstance.getDB(NETBEAMS_DATASTORE_NAME);
    }

    /**
     * Adds the needed indexes for the metadata used for any collected data.
     * 
     * @param dataCollection
     */
    private void setupMetadataIndexes(DBCollection dataCollection) {
        dataCollection.ensureIndex(new BasicDBObject("message_id", "1"), true);
        dataCollection.ensureIndex(new BasicDBObject("sensor.ip_address", "1"), true);
        dataCollection.ensureIndex(new BasicDBObject("sensor.location.latitude", "1"), true);
        dataCollection.ensureIndex(new BasicDBObject("sensor.location.longitude", "1"), true);
        dataCollection.ensureIndex(new BasicDBObject("time.valid", "1"), true);
        dataCollection.ensureIndex(new BasicDBObject("time.transaction", "1"), true);
    }

    /**
     * Adds the needed indexes for the sonde data collection
     * 
     * @param dbCollection
     */
    private void setupDataIndexes(DBCollection sondeDataCollection) {

        final String DATA_PREFIX = "observation.";
        if (this.includeProperty.size() == 0 || this.includeProperty.get("watertemperature") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "WaterTemperature", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("specificconductivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "SpecificConductivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("conductivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Conductivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("resistivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Resistivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("salinity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Salinity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("pressure") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Pressure", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("depth") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Depth", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("ph") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "pH", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("phmv") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "pHmV", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("turbidity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Turbidity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odosaturation") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "ODOSaturation", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odo") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "ODO", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("battery") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject(DATA_PREFIX + "Battery", "1"), true);
        }
    }

    /**
     * Inserts the DSP Message Content into the mongoDB as it is extracted and converted from the given
     * PersistentMessageUnit.
     * 
     * @param tranMsg is the PersistentMessageUnit containing information about the sensor location and the message.
     * @param serverIpAddress is the IP address of the server to insert this collection. It must be a single server or
     *            the master node of a mongo cluster in a sharded environment.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public synchronized void insertPersistentUnitMessageContents(Set<PersistentMessageUnit> tranMsgs)
            throws UnknownHostException, MongoException {
        if (!initialized) {
            throw new IllegalStateException("Initialize the service before inserting");
        }
        log.info("Inserting transient DSP Messages: " + tranMsgs.size() + " messages");
        // Start the mongoDB Transaction mechanism
        this.getNetbeamMongoDb().requestStart();
        for (PersistentMessageUnit tranMsg : tranMsgs) {
            DBCollection netbeamsDbCollection = this.getPersistenceStorage(tranMsg);
            MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();

            BasicDBObject docValue = null;
            long factTime = 0;
            if (messageContent instanceof SondeDataContainer) {
                SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
                for (SondeDataType sondeData : sondeContainer.getSondeData()) {
                    // build the document value
                    docValue = buildValueSegment(sondeData);
                    // extract the fact time from the message, adding to the key
                    factTime = sondeData.getDateTime().getTimeInMillis();
                }
            } else if (messageContent instanceof MouseActionsContainer) {
                MouseActionsContainer mouseActionsContainer = (MouseActionsContainer) messageContent;
                for (MouseAction data : mouseActionsContainer.getMouseAction()) {
                    // build the document value
                    docValue = buildValueSegment(data);
                    // extract the fact time from the message, adding to the key
                    factTime = data.getCollectionTime();
                }
            }
            // build the document key
            BasicDBObject docKey = buildKeySegment(tranMsg, factTime, docValue);
            // insert the final collection
            netbeamsDbCollection.insert(docKey);
            // flush the message from transient
        }
        // Terminate the mongoDB Transaction
        this.getNetbeamMongoDb().requestDone();
    }

    /**
     * @param tranMsg is the persistent message unit holding an instance of a transmitted DSP message.
     * @param validTime is the time in which the data was collected.
     * @param docValue is the value of the document that will be added into the "data" attribute.
     * @return the Key Segment for the given Persistent Message Unit's DSP Message instance. It extracts the sensor's IP
     *         address, the Message ID and the Transaction time from the Unit, which constitutes the Key.
     * @throws MongoException
     * @throws UnknownHostException
     */
    public BasicDBObject buildKeySegment(PersistentMessageUnit tranMsg, long validTime, BasicDBObject docValue)
            throws UnknownHostException, MongoException {
        BasicDBObject doc = new BasicDBObject();
        doc.put("message_id", tranMsg.getDspMessage().getMessageID());
        // doc.put("sensor", new DBRef(this.getNetbeamMongoDb(), "sensors", )); VERY SLOW

        BasicDBObject sensorDoc = new BasicDBObject();
        sensorDoc.put("ip_address", tranMsg.getSensorLocation().getIpAddress());
        BasicDBObject locDoc = new BasicDBObject();
        locDoc.put("latitude", tranMsg.getSensorLocation().getLatitude());
        locDoc.put("longitude", tranMsg.getSensorLocation().getLongitude());
        sensorDoc.put("location", locDoc);
        doc.put("sensor", sensorDoc);

        BasicDBObject dimensionDoc = new BasicDBObject();
        Calendar validTimeCal = Calendar.getInstance();
        validTimeCal.setTimeInMillis(validTime);
        dimensionDoc.put("valid", validTimeCal.getTime());
        Calendar transTime = Calendar.getInstance();
        transTime.setTimeInMillis(tranMsg.getCollectionTimeMilliseconds());
        dimensionDoc.put("transaction", transTime.getTime());
        doc.put("time", dimensionDoc);

        doc.put("observation", docValue);
        return doc;
    }

    /**
     * @param sondeData is the instance of the sonde data.
     * @return the basic DB Object representation of the sonde data type with all the properties.
     */
    public BasicDBObject buildValueSegment(SondeDataType sondeData) {
        BasicDBObject docValue = new BasicDBObject();
        if (this.includeProperty.size() == 0 || this.includeProperty.get("watertemperature") == Boolean.TRUE) {
            docValue.put("WaterTemperature", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getTemp())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("specificconductivity") == Boolean.TRUE) {
            docValue.put("SpecificConductivity", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getSpCond())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("conductivity") == Boolean.TRUE) {
            docValue.put("Conductivity", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getCond())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("resistivity") == Boolean.TRUE) {
            docValue.put("Resistivity", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getResist())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("salinity") == Boolean.TRUE) {
            docValue.put("Salinity", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getSal())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("pressure") == Boolean.TRUE) {
            docValue.put("Pressure", Double.valueOf(THREE_DECIMALS_FORMATTER.format(sondeData.getPress())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("depth") == Boolean.TRUE) {
            docValue.put("Depth", Double.valueOf(THREE_DECIMALS_FORMATTER.format(sondeData.getDepth())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("ph") == Boolean.TRUE) {
            docValue.put("pH", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getPH())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("phmv") == Boolean.TRUE) {
            docValue.put("pHmV", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getPhmV())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("turbidity") == Boolean.TRUE) {
            docValue.put("Turbidity", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getTurbid())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odosaturation") == Boolean.TRUE) {
            docValue.put("ODOSaturation", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getODOSat())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odo") == Boolean.TRUE) {
            docValue.put("ODO", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getODOConc())));
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("battery") == Boolean.TRUE) {
            docValue.put("Battery", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getBattery())));
        }
        return docValue;
    }

    /**
     * @param sondeData is the instance of the sonde data.
     * @return the basic DB Object representation of the sonde data type with all the properties.
     */
    public BasicDBObject buildValueSegment(MouseAction mouseData) {
        BasicDBObject docValue = new BasicDBObject();
        docValue.put("x", mouseData.getX());
        docValue.put("y", mouseData.getY());
        docValue.put("button", mouseData.getButton().toString());
        docValue.put("event", mouseData.getEvent().toString());
        return docValue;
    }
}
