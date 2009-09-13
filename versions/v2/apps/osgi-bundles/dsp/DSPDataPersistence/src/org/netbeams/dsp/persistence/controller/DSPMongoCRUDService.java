package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessageContent;
import org.netbeams.dsp.persistence.model.component.data.PersistentMessageUnit;
import org.netbeams.dsp.persistence.model.location.SensorLocation;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;
import org.netbeams.dsp.ysi.SondeTestData;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoAdmin;
import com.mongodb.MongoException;

public class DSPMongoCRUDService {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SS");
    /**
     * The default mongoDB database name for the project.
     */
    private static final String NETBEAMS_DATASTORE_NAME = "netbeams";
    /**
     * The default mongoDB collection name for the datastore.
     */
    private static final String NETBEAMS_DATASTORE_COLLECTION_NAME = "ysi";
    /**
     * Sleep interval for garbage collection
     */
    private static long fSLEEP_INTERVAL = 100;
    /**
     * The local cache for the collection names based on the content type
     */
    private static Map<String, DBCollection> netBeamscollectionsCache = new HashMap<String, DBCollection>();

    /**
     * @param persistentMessage is the DSP Message that was persisted after transmission.
     * @return the DBCollection instance based on the Message Content Type from the DSP Message that transmitted from
     * the PersistentMessageUnit instance.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static DBCollection getPersistenceStorage(PersistentMessageUnit persistentMessage) throws UnknownHostException, MongoException {
        DBCollection dbCollection = netBeamscollectionsCache.get(persistentMessage.getMessageContentType());
        if (dbCollection == null) {
            Mongo netbeamsDb = getNetbeamMongoDb();
            dbCollection = netbeamsDb.getCollection(persistentMessage.getMessageContentType());
        }
        return dbCollection;
    }

    /**
     * @return the default Mongo instance based on the default NETBEAMS_DATASTORE_NAME.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static Mongo getNetbeamMongoDb() throws UnknownHostException, MongoException {
        MongoAdmin admin = new MongoAdmin();
        return admin.getDatabase(NETBEAMS_DATASTORE_NAME);
    }

    /**
     * Inserts the Message Content's instance into the mongoDB.
     * @param tranMsg is a persistent message unit from a DSP message transmitted by a sensor.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static void insertMessageUnit(PersistentMessageUnit tranMsg) throws UnknownHostException, MongoException {
        long start = System.currentTimeMillis();
        long startMemoryUse = getMemoryUse();
        System.out.println("Started saving netbeams samples as mongodb objects at " + DATE_FORMATTER.format(new Date(start)));
        insertPersistentUnitMessageContents(tranMsg);
        long end = System.currentTimeMillis();
        float endMemoryUse = getMemoryUse();
        float approximateSize = (endMemoryUse - startMemoryUse) / 100f;
        float result = Math.round(approximateSize / 1024);
        System.out.println("Finished saving netbeams samples to mongodb objects in "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");
    }

    /**
     * @param tranMsg is the persistent message unit holding an instance of a transmitted DSP message.
     * @return the Key Segment for the given Persistent Message Unit's DSP Message instance. It extracts the sensor's
     * IP address, the Message ID and the Transaction time from the Unit, which constitutes the Key.
     */
    public static BasicDBObject buildKeySegment(PersistentMessageUnit tranMsg) {
        BasicDBObject doc = new BasicDBObject();
        doc.put("sensor_ip_address", tranMsg.getSensorLocation().getIpAddress());
        doc.put("message_id", tranMsg.getDspMessage().getMessageID());
        doc.put("transaction_time", tranMsg.getCollectionTimeMilliseconds());
        return doc;
    }

    public static void insertPersistentUnitMessageContents(PersistentMessageUnit tranMsg) throws UnknownHostException, MongoException {
        DBCollection netbeamsDbCollection = getPersistenceStorage(tranMsg);
        MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();
        System.out.println("Starting mongodb transaction at " + DATE_FORMATTER.format(new Date()));
        getNetbeamMongoDb().requestStart();
        if (messageContent instanceof SondeDataContainer) {
            SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
            for (SondeDataType sondeData : sondeContainer.getSondeData()) {
                BasicDBObject docValue = new BasicDBObject();
                docValue.put("temperature", "" + sondeData.getTemp().floatValue());
                docValue.put("sp_condition", "" + sondeData.getSpCond().floatValue());
                docValue.put("condition", "" + sondeData.getCond().floatValue());
                docValue.put("resistence", "" + sondeData.getResist().floatValue());
                docValue.put("salinitude", "" + sondeData.getSal().floatValue());
                docValue.put("pressure", "" + sondeData.getPress().floatValue());
                docValue.put("depth", "" + sondeData.getDepth().floatValue());
                docValue.put("ph", "" + sondeData.getPH().floatValue());
                docValue.put("pH_mv", "" + sondeData.getPhmV().floatValue());
                docValue.put("odo_sat", "" + sondeData.getODOSat().floatValue());
                docValue.put("odo_condition", "" + sondeData.getODOConc().floatValue());
                docValue.put("turbidity", "" + sondeData.getTurbid().floatValue());
                docValue.put("battery", "" + sondeData.getBattery().floatValue());

                BasicDBObject docKey = buildKeySegment(tranMsg);
                // extract the fact time from the message, adding to the key
                docKey.put("fact_time", sondeData.getDateTime().getTimeInMillis());
                docKey.put("data", docValue);
                // insert the final collection
                netbeamsDbCollection.insert(docKey);
            }
        }
        getNetbeamMongoDb().requestDone();
    }

    /**
     * @param start
     * @param end
     * @return the difference between the end and start values
     */
    public static String getTimeDifference(long start, long end) {
        long diff = end - start;
        return (float) ((float) diff / (float) 1000) + " seconds (" + diff + " milliseconds) at "
                + DATE_FORMATTER.format(new Date(end));
    }

    /**
     * @return the values of bytes used by the JVM
     */
    private static long getMemoryUse() {
        putOutTheGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();

        putOutTheGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();

        return (totalMemory - freeMemory);
    }

    private static void putOutTheGarbage() {
        collectGarbage();
        collectGarbage();
    }

    private static void collectGarbage() {
        try {
            System.gc();
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
            System.runFinalization();
            Thread.currentThread().sleep(fSLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "You need to provide the number of netbeams samples to generate: \nExample: DSPMongoCRUDService 5");
        }
        int NUMBER_SAMPLES = Integer.valueOf(args[0]);
        long startMemoryUse = getMemoryUse();
        System.out.println("Experiment started at " + DATE_FORMATTER.format(new Date()));
        long start = System.currentTimeMillis();
        System.out.println("Starting to generate " + NUMBER_SAMPLES + " sonde samples at "
                + DATE_FORMATTER.format(new Date(start)));
        SondeDataContainer container = SondeTestData.generateRandomSondeDataContainer(NUMBER_SAMPLES);
        long end = System.currentTimeMillis();
        float endMemoryUse = getMemoryUse();
        float approximateSize = (endMemoryUse - startMemoryUse) / 100f;
        long result = Math.round(approximateSize / 1024);
        System.out.println("Finished Generating " + container.getSondeData().size() + " sonde samples on "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");

        startMemoryUse = getMemoryUse();
        String hostIpAddr = "192.168.0." + ((int) (Math.random() * 253) + 2);
        ComponentIdentifier producerId = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("persistent",
                hostIpAddr, "sonde-data-type");
        Header header = DSPMessagesFactory.INSTANCE
                .makeDSPMessageHeader(UUID.randomUUID().toString(), producerId, null);
        Message msg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, container);

        SensorLocation.Builder builder = new SensorLocation.Builder();
        builder.setIpAddress(hostIpAddr).setLatitude((float) (Math.random() * 100 + 1)).setLongitude(
                (float) (Math.random() * 100 + 1));

        PersistentMessageUnit pmu = new PersistentMessageUnit(msg, builder.build());
        try {
            insertMessageUnit(pmu);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        endMemoryUse = getMemoryUse();
        approximateSize = (endMemoryUse - startMemoryUse) / 100f;
        result = Math.round(approximateSize / 1024);
        System.out.println("Experiment finished saving " + container.getSondeData().size()
                + " sonde samples on MongoDB on " + getTimeDifference(start, end) + " consuming ~" + result + " Kb");
    }
}
