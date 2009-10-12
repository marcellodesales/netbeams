package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
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
public class DSPMongoCRUDService {
    
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
    private static Map<String, DBCollection> netBeamscollectionsCache = new HashMap<String, DBCollection>();

    /**
     * @param persistentMessage is the DSP Message that was persisted after transmission.
     * @return the DBCollection instance based on the Message Content Type from the DSP Message that transmitted from
     *         the PersistentMessageUnit instance.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static DBCollection getPersistenceStorage(PersistentMessageUnit persistentMessage)
            throws UnknownHostException, MongoException {
        DBCollection dbCollection = netBeamscollectionsCache.get(persistentMessage.getMessageContentType());
        if (dbCollection == null) {
            DB netbeamsDb = getNetbeamMongoDb();
            dbCollection = netbeamsDb.getCollection(persistentMessage.getMessageContentType());
        }
        return dbCollection;
    }

    /**
     * @return the default Mongo instance based on the default NETBEAMS_DATASTORE_NAME.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static DB getNetbeamMongoDb() throws UnknownHostException, MongoException {
        Mongo admin = new Mongo("127.0.0.1");
        return admin.getDB(NETBEAMS_DATASTORE_NAME);
    }

    /**
     * @param tranMsg is the persistent message unit holding an instance of a transmitted DSP message.
     * @param factTime is the time in which the data was collected.
     * @param docValue is the value of the document that will be added into the "data" attribute.
     * @return the Key Segment for the given Persistent Message Unit's DSP Message instance. It extracts the sensor's IP
     *         address, the Message ID and the Transaction time from the Unit, which constitutes the Key.
     */
    public static BasicDBObject buildKeySegment(PersistentMessageUnit tranMsg, long factTime, BasicDBObject docValue) {
        BasicDBObject doc = new BasicDBObject();
        doc.put("sensor_ip_address", tranMsg.getSensorLocation().getIpAddress());
        //doc.put("latitude", tranMsg.getSensorLocation().getLatitude());
        //doc.put("longitude", tranMsg.getSensorLocation().getLongitude());
        doc.put("latitude", Double.valueOf(THREE_DECIMALS_FORMATTER.format((Math.random() * 180 + 1) * (Math.random() > .5 ? 1 : -1))));
        doc.put("longitude", Double.valueOf(THREE_DECIMALS_FORMATTER.format((Math.random() * 180 + 1) * (Math.random() > .5 ? 1 : -1))));
        doc.put("message_id", tranMsg.getDspMessage().getMessageID());
        doc.put("transaction_time", tranMsg.getCollectionTimeMilliseconds());
        doc.put("fact_time", factTime);
        doc.put("data", docValue);
        return doc;
    }

    /**
     * Inserts the DSP Message Content into the mongoDB as it is extracted and converted from the given
     * PersistentMessageUnit.
     * 
     * @param tranMsg is the PersistentMessageUnit containing information about the sensor location and the message.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static void insertPersistentUnitMessageContents(PersistentMessageUnit tranMsg) throws UnknownHostException,
            MongoException {
        DBCollection netbeamsDbCollection = getPersistenceStorage(tranMsg);
        MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();
        // Start the mongoDB Transaction mechanism
        getNetbeamMongoDb().requestStart();
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
        // Terminate the mongoDB Transaction
        getNetbeamMongoDb().requestDone();
    }

    /**
     * @param sondeData is the instance of the sonde data.
     * @return the basic DB Object representation of the sonde data type with all the properties.
     */
    public static BasicDBObject buildValueSegment(SondeDataType sondeData) {
        BasicDBObject docValue = new BasicDBObject();
        docValue.put("temperature", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getTemp())));
        docValue.put("sp_condition", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getSpCond())));
        docValue.put("condition", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getCond())));
        docValue.put("resistence", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getResist())));
        docValue.put("salinity", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getSal())));
        docValue.put("pressure", Double.valueOf(THREE_DECIMALS_FORMATTER.format(sondeData.getPress())));
        docValue.put("depth", Double.valueOf(THREE_DECIMALS_FORMATTER.format(sondeData.getDepth())));
        docValue.put("ph", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getPH())));
        docValue.put("pH_mv", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getPhmV())));
        docValue.put("odo_sat", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getODOSat())));
        docValue.put("odo_condition", Double.valueOf(TWO_DECIMALS_FORMATTER.format(sondeData.getODOConc())));
        docValue.put("turbidity", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getTurbid())));
        docValue.put("battery", Double.valueOf(ONE_DECIMAL_FORMATTER.format(sondeData.getBattery())));
        return docValue;
    }
}
