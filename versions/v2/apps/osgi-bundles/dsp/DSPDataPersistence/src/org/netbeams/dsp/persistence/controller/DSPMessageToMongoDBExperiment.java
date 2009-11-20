package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

import com.commsen.stopwatch.Report;
import com.commsen.stopwatch.Stopwatch;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

/**
 * The experiment to transfer DSP Messages to mongoDB. It uses random data of DSP Message Contents.
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 * 
 */
public class DSPMessageToMongoDBExperiment {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SS");
    /**
     * Sleep interval for garbage collection
     */
    private static long fSLEEP_INTERVAL = 100;

    private static final Set<SensorLocation> randomLocationsCache = new HashSet<SensorLocation>();

    /**
     * @param startMemoryUse is the number of bytes previously measured
     * @return the amount of memory in bytes between the start number measurement and the current.
     */
    public static long getMemoryUsedSinceGivenMemorySample(long startMemoryUse) {
        float endMemoryUse = getMemoryUse();
        return Math.round((endMemoryUse - startMemoryUse) / 1024);
    }

    /**
     * Inserts the DSP Message Content into the mongoDB as it is extracted and converted from the given
     * PersistentMessageUnit.
     * 
     * @param tranMsg is the PersistentMessageUnit containing information about the sensor location and the message.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static void insertPersistentUnitMessageContents(Set<PersistentMessageUnit> tranMsgs) throws 
            UnknownHostException, MongoException {

        DSPMongoCRUDService.INSTANCE.getNetbeamMongoDb().requestStart();
        for (PersistentMessageUnit tranMsg : tranMsgs) {
            DBCollection netbeamsDbCollection = DSPMongoCRUDService.INSTANCE.getPersistenceStorage(tranMsg);
            MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();
            if (messageContent instanceof SondeDataContainer) {
                SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
                for (SondeDataType sondeData : sondeContainer.getSondeData()) {
                    long watchSondeId = Stopwatch.start("Sonde Data Transformation and Persistence", "sonde-2-mongodb");
                    // build the document value
                    BasicDBObject docValue = DSPMongoCRUDService.INSTANCE.buildValueSegment(sondeData);
                    // extract the fact time from the message, adding to the key
                    long factTime = sondeData.getDateTime().getTimeInMillis();
                    // build the document key
                    BasicDBObject docKey = DSPMongoCRUDService.INSTANCE.buildKeySegment(tranMsg, factTime, docValue);
                    // insert the final collection
                    netbeamsDbCollection.insert(docKey);
                    Stopwatch.stop(watchSondeId);
                }
            }
        }
        DSPMongoCRUDService.INSTANCE.getNetbeamMongoDb().requestDone();
    }

    /**
     * @param start time in milliseconds
     * @param end time in milliseconds
     * @return the difference between the end and start values formatted in string
     */
    public static String getTimeDifference(long start, long end) {
        long diff = end - start;
        return (float) (((float) diff / (float) 1000) / 60) + " minutes (" + diff + " milliseconds) at "
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

    private static Set<PersistentMessageUnit> generateRandomMessages(int numberItems) {

        System.out.println("Producing " + numberItems + " random PersistentUnits with one DSP Message with a random "
                + " sample frequence from the YSI Sonde");

        Set<PersistentMessageUnit> persistentUnits = new HashSet<PersistentMessageUnit>(numberItems);
        for (int i = 0; i < numberItems; i++) {
            long messageUnitTime = Stopwatch.start("Each PersistentMessageUnit", "persistent-msgs-unit");
            // build a random sensor location with fix GPS coordination from the RTC pier.
            SensorLocation.Builder locationBuider = new SensorLocation.Builder();
            locationBuider.setIpAddress("192.168.0." + ((int) (Math.random() * 253) + 2));
            locationBuider.setLatitude(Double.valueOf(37.89155));
            locationBuider.setLongitude(Double.valueOf(-122.4464));
            SensorLocation location = locationBuider.build();
            randomLocationsCache.add(location);

            // build the producer identifier
            ComponentIdentifier producerId = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("experiment_comp",
                    location.getIpAddress(), "sonde-data-type");

            // build the message header
            Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producerId, null);

            // build just one sample observation for the message
            SondeDataContainer container = SondeTestData.generateRandomSondeDataContainer(1);
            Message msg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, container);

            // persistence unit
            PersistentMessageUnit pmu = new PersistentMessageUnit(msg, locationBuider.build());
            Stopwatch.stop(messageUnitTime);
            persistentUnits.add(pmu);
        }
        return persistentUnits;
    }

    public static void main(String[] args) throws UnknownHostException, MongoException {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "You need to provide the server IP address and number of netbeams samples to generate: "
                            + "\nExample: DSPMongoCRUDService 127.0.0.1 5");
        }
        Stopwatch.setActive(true);
        long watchExperimentId = Stopwatch.start("Complete Experiene", "entire-experience");

        // the needed 3 arguments
        final String SERVER_IP_ADDRESS = args[0];
        final int NUMBER_SAMPLES = Integer.valueOf(args[1]);
        String PROPERTIES_LIST = null;
        if (args.length == 3) {
            PROPERTIES_LIST = args[2];
        }

        long startMemoryUse = getMemoryUse();
        System.out.println("Experiment started at " + DATE_FORMATTER.format(new Date()));
        long start = System.currentTimeMillis();
        System.out.println("Starting to generate " + NUMBER_SAMPLES + " sonde samples at "
                + DATE_FORMATTER.format(new Date(start)) + "; MEM USED: " + startMemoryUse);

        long watchTransaction = Stopwatch.start("Time for creating random PersistentMessage Units", "all-messages");
        Set<PersistentMessageUnit> messageUnits = generateRandomMessages(NUMBER_SAMPLES);
        Stopwatch.stop(watchTransaction);

        watchTransaction = Stopwatch.start("Time for initializing the mongo service", "mongo-service");
        DSPMongoCRUDService.INSTANCE.initialize(SERVER_IP_ADDRESS, randomLocationsCache, PROPERTIES_LIST);
        Stopwatch.stop(watchTransaction);

        long end = System.currentTimeMillis();
        float result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Finished Generating " + NUMBER_SAMPLES + " sonde samples on "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");

        try {
            watchTransaction = Stopwatch.start("Time for inserting all PersistentMessage Units", "insert-all");
            insertPersistentUnitMessageContents(messageUnits);
            Stopwatch.stop(watchTransaction);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
        Stopwatch.stop(watchExperimentId);
        end = System.currentTimeMillis();
        result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Experiment finished saving " + messageUnits.size() + " sonde samples on MongoDB on "
                + getTimeDifference(start, end) + " consuming ~" + result + " Kb");

        System.out.println("\nProcessing times for sections");
        for (Report report : Stopwatch.getAllReports()) {
            System.out.println(report);
        }
    }
}
