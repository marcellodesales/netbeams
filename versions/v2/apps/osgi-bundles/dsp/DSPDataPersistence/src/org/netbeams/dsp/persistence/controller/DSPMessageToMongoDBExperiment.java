package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    /**
     * Inserts the Message Content's instance into the mongoDB.
     * 
     * @param tranMsg is a persistent message unit from a DSP message transmitted by a sensor.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static void insertMessageUnit(PersistentMessageUnit tranMsg) throws UnknownHostException, MongoException {
        long start = System.currentTimeMillis();
        long startMemoryUse = getMemoryUse();
        System.out.println("Started saving netbeams samples as mongodb objects at "
                + DATE_FORMATTER.format(new Date(start)));

        long watchTransaction = Stopwatch.start("Transaction time for inserting", "sonde-2-mongodb");
        insertPersistentUnitMessageContents(tranMsg);
        Stopwatch.stop(watchTransaction);

        long end = System.currentTimeMillis();
        long result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Finished tranaction and saving netbeams samples to mongodb objects in "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");
    }

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
    public static void insertPersistentUnitMessageContents(PersistentMessageUnit tranMsg) throws UnknownHostException,
            MongoException {
        DBCollection netbeamsDbCollection = DSPMongoCRUDService.getPersistenceStorage(tranMsg);
        MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();
        System.out.println("Starting mongodb transaction at " + DATE_FORMATTER.format(new Date()));
        DSPMongoCRUDService.getNetbeamMongoDb().requestStart();
        if (messageContent instanceof SondeDataContainer) {
            SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
            for (SondeDataType sondeData : sondeContainer.getSondeData()) {
                long watchSondeId = Stopwatch.start("Sonde Data Transformation and Persistence", "sonde-2-mongodb");
                // build the document value
                BasicDBObject docValue = DSPMongoCRUDService.buildValueSegment(sondeData);
                // extract the fact time from the message, adding to the key
                long factTime = sondeData.getDateTime().getTimeInMillis();
                // build the document key
                BasicDBObject docKey = DSPMongoCRUDService.buildKeySegment(tranMsg, factTime, docValue);
                // insert the final collection
                netbeamsDbCollection.insert(docKey);
                Stopwatch.stop(watchSondeId);
            }
        }
        DSPMongoCRUDService.getNetbeamMongoDb().requestDone();
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

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                    "You need to provide the number of netbeams samples to generate: \nExample: DSPMongoCRUDService 5");
        }
        Stopwatch.setActive(true);
        long watchExperimentId = Stopwatch.start("Complete Experiene", "entire-experience");

        int NUMBER_SAMPLES = Integer.valueOf(args[0]);

        long startMemoryUse = getMemoryUse();
        System.out.println("Experiment started at " + DATE_FORMATTER.format(new Date()));
        long start = System.currentTimeMillis();
        System.out.println("Starting to generate " + NUMBER_SAMPLES + " sonde samples at "
                + DATE_FORMATTER.format(new Date(start)));

        SondeDataContainer container = SondeTestData.generateRandomSondeDataContainer(NUMBER_SAMPLES);

        long end = System.currentTimeMillis();
        float result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Finished Generating " + container.getSondeData().size() + " sonde samples on "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");

        startMemoryUse = getMemoryUse();

        System.out.println("Producing a random DSP Message with " + container.getSondeData().size()
                + " items on the content message");
        long watchMessageCreationId = Stopwatch.start("DSP Message Creation", "dsp-creation");
        String hostIpAddr = "192.168.0." + ((int) (Math.random() * 253) + 2);
        ComponentIdentifier producerId = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("persistent",
                hostIpAddr, "sonde-data-type");
        Header header = DSPMessagesFactory.INSTANCE
                .makeDSPMessageHeader(UUID.randomUUID().toString(), producerId, null);
        Message msg = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, container);
        Stopwatch.stop(watchMessageCreationId);

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
        Stopwatch.stop(watchExperimentId);
        end = System.currentTimeMillis();
        result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Experiment finished saving " + container.getSondeData().size()
                + " sonde samples on MongoDB on " + getTimeDifference(start, end) + " consuming ~" + result + " Kb");

        System.out.println("\nProcessing times for sections");
        for (Report report : Stopwatch.getAllReports()) {
            System.out.println(report);
        }
    }
}
