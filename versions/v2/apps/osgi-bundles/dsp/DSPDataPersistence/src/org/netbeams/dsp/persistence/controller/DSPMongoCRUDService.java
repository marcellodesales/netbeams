package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
import org.netbeams.dsp.persistence.model.location.SensorLocation;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
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

    private static final DecimalFormat ONE_DECIMAL_FORMATTER = new DecimalFormat("###0.0");
    private static final DecimalFormat TWO_DECIMALS_FORMATTER = new DecimalFormat("###0.00");
    private static final DecimalFormat THREE_DECIMALS_FORMATTER = new DecimalFormat("###0.000");

    /**
     * The default mongoDB database name for the project.
     */
    private static final String NETBEAMS_DATASTORE_NAME = "netbeams";
    /**
     * The local cache for the collection names based on the content type
     */
    private static final Map<String, DBCollection> netBeamscollectionsCache = new HashMap<String, DBCollection>();
    /**
     * The local cache for the references to the sensor IPs registered in the database.
     */
    private static final Map<String, Object> sensorsIpReferences = new HashMap<String, Object>();
    /**
     * The IP address of the mongo server. Either the single server of the cluster head when in sharded environment 
     */
    private String serverIpAddress;
    /**
     * Tracks if the service was initialized before being used. After getting the INSTANCE instance, the method
     * initialize must be called.
     */
    private boolean initialized;
    /**
     * The list of properties to be included.
     * temperature = true
     * salinity = true
     */
    private Map<String, Boolean> includeProperty = new HashMap<String, Boolean>(15);

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
    public void initialize(String serverIpAddress, Set<SensorLocation> sensorsLocation, String propertiesList)
            throws UnknownHostException, MongoException {
        this.initialized = true;
        this.serverIpAddress = serverIpAddress;
        this.setUpSensors(sensorsLocation);
        this.setupSelectedProperties(propertiesList);
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
            this.setupDataIndexes(dbCollection);
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
        Mongo dbInstance = new Mongo(this.serverIpAddress);
        return dbInstance.getDB(NETBEAMS_DATASTORE_NAME);
    }

    /**
     * Sets up the list of known sensor locations.
     * 
     * @param sensors is the set of unique sensor locations identified by an IP address.
     * @throws UnknownHostException
     * @throws MongoException
     */
    private void setUpSensors(Set<SensorLocation> sensors) throws UnknownHostException, MongoException {
        DB netbeamsDb = this.getNetbeamMongoDb();
        DBCollection dbCollection = netBeamscollectionsCache.get("sensors");
        if (dbCollection == null) {
            dbCollection = netbeamsDb.getCollection("sensors");
        }

        // Add indexes to the keys
        dbCollection.ensureIndex(new BasicDBObject("ip_address", "1"), true, true);

        // Add all the knowledge about the sensor location in the database
        // Start the mongoDB Transaction mechanism
        this.getNetbeamMongoDb().requestStart();
        for (SensorLocation location : sensors) {
            DBObject sensorDoc = new BasicDBObject();
            sensorDoc.put("ip_address", location.getIpAddress());

            DBObject locationDoc = new BasicDBObject();
            locationDoc.put("latitude", location.getLatitude());
            locationDoc.put("longitude", location.getLongitude());
            sensorDoc.put("location", locationDoc);
            BasicDBObject insertedSensorDoc = (BasicDBObject) dbCollection.insert(sensorDoc);
            sensorsIpReferences.put(location.getIpAddress(), insertedSensorDoc.getString("_id"));
        }
        // Terminate the mongoDB Transaction
        this.getNetbeamMongoDb().requestDone();
    }
    
    /**
     * Adds the needed indexes for the sonde data collection
     * @param dbCollection
     */
    private void setupDataIndexes(DBCollection sondeDataCollection) {
        if (this.includeProperty.size() == 0 || this.includeProperty.get("watertemperature") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("WaterTemperature", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("specificconductivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("SpecificConductivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("conductivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Conductivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("resistivity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Resistivity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("salinity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Salinity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("pressure") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Pressure", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("depth") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Depth", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("ph") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("pH", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("phmv") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("pHmV", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("turbidity") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Turbidity", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odosaturation") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("ODOSaturation", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("odo") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("ODO", "1"), true);
        }
        if (this.includeProperty.size() == 0 || this.includeProperty.get("battery") == Boolean.TRUE) {
            sondeDataCollection.ensureIndex(new BasicDBObject("Battery", "1"), true);
        }
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

        Object sensorReference = sensorsIpReferences.get(tranMsg.getSensorLocation().getIpAddress());
        doc.put("sensor", new DBRef(this.getNetbeamMongoDb(), "sensors", sensorReference));
        doc.put("message_id", tranMsg.getDspMessage().getMessageID());

        Calendar validTimeCal = Calendar.getInstance();
        validTimeCal.setTimeInMillis(validTime);
        doc.put("valid_time", validTimeCal.getTime());

        Calendar transTime = Calendar.getInstance();
        transTime.setTimeInMillis(tranMsg.getCollectionTimeMilliseconds());
        doc.put("transaction_time", transTime.getTime());

        doc.put("data", docValue);
        return doc;
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
    public void insertPersistentUnitMessageContents(Set<PersistentMessageUnit> tranMsgs) throws UnknownHostException,
            MongoException {
        if (!this.initialized) {
            throw new IllegalStateException("Initialize the service before inserting");
        }
        // Start the mongoDB Transaction mechanism
        this.getNetbeamMongoDb().requestStart();
        for (PersistentMessageUnit tranMsg : tranMsgs) {
            DBCollection netbeamsDbCollection = this.getPersistenceStorage(tranMsg);
            MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();

            if (messageContent instanceof SondeDataContainer) {
                SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
                for (SondeDataType sondeData : sondeContainer.getSondeData()) {
                    // build the document value
                    BasicDBObject docValue = buildValueSegment(sondeData);
                    // extract the fact time from the message, adding to the key
                    long factTime = sondeData.getDateTime().getTimeInMillis();
                    // build the document key
                    BasicDBObject docKey = buildKeySegment(tranMsg, factTime, docValue);
                    // insert the final collection
                    netbeamsDbCollection.insert(docKey);
                }
            }
        }
        // Terminate the mongoDB Transaction
        this.getNetbeamMongoDb().requestDone();
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
}
