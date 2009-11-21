package org.netbeams.dsp.persistence.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private static Set<PersistentMessageUnit> persistentUnits;

    private static boolean inWriteLock = true;

    private static int insertedDocuments = 0;

    public static int getNumberOfInsertedDocuments() {
        return insertedDocuments;
    }

    public static boolean isInWriteLock() {
        return inWriteLock;
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

    /**
     * Try to force the garbage collection to clean up the memory
     */
    private static void putOutTheGarbage() {
        collectGarbage();
        collectGarbage();
    }

    /**
     * Try to call garbage collection
     */
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

    /**
     * Generates a given number of persistent message units
     * 
     * @param numberItems is the number of persistent messages unit. Must be bigger than 0
     * @return the set of unique persistent message Unit items
     */
    private static Set<PersistentMessageUnit> generateRandomMessages(int numberItems) {

        System.out.println("Producing " + numberItems + " random PersistentUnits with one DSP Message with a random "
                + " sample frequence from the YSI Sonde");

        persistentUnits = new HashSet<PersistentMessageUnit>(numberItems);
        final Calendar TRANSACTION_TIME = Calendar.getInstance();
        for (int i = 0; i < numberItems; i++) {
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
            PersistentMessageUnit pmu = new PersistentMessageUnit(msg, locationBuider.build(), TRANSACTION_TIME);
            persistentUnits.add(pmu);
        }
        return persistentUnits;
    }

    /**
     * Inserts the DSP Message Content into the mongoDB as it is extracted and converted from the given
     * PersistentMessageUnit.
     * 
     * @param tranMsg is the PersistentMessageUnit containing information about the sensor location and the message.
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static void insertPersistentUnitMessageContents(Set<PersistentMessageUnit> tranMsgs)
            throws UnknownHostException, MongoException {

        DSPMongoCRUDService.INSTANCE.getNetbeamMongoDb().requestStart();
        for (PersistentMessageUnit tranMsg : tranMsgs) {
            DBCollection netbeamsDbCollection = DSPMongoCRUDService.INSTANCE.getPersistenceStorage(tranMsg);
            MessageContent messageContent = tranMsg.getDspMessage().getBody().getAny();
            SondeDataContainer sondeContainer = (SondeDataContainer) messageContent;
            SondeDataType sondeData = sondeContainer.getSondeData().get(0);
            // build the document value
            BasicDBObject docValue = DSPMongoCRUDService.INSTANCE.buildValueSegment(sondeData);
            // extract the fact time from the message, adding to the key
            long factTime = sondeData.getDateTime().getTimeInMillis();
            // build the document key
            BasicDBObject docKey = DSPMongoCRUDService.INSTANCE.buildKeySegment(tranMsg, factTime, docValue);
            // insert the final collection
            netbeamsDbCollection.insert(docKey);
            ++insertedDocuments;
        }
        inWriteLock = false;
        DSPMongoCRUDService.INSTANCE.getNetbeamMongoDb().requestDone();
    }

    public static void main(String[] args) throws UnknownHostException, MongoException, InterruptedException {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "You need to provide the server IP address and number of netbeams samples to generate: "
                            + "\nExample: DSPMongoCRUDService 127.0.0.1 5");
        }

        putOutTheGarbage();
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

        Thread status = new Thread("Messages-Generator") {
            public void run() {
                int i = 0;
                int min = 0;
                System.out.println();
                while (persistentUnits == null || persistentUnits.size() < NUMBER_SAMPLES) {
                    try {
                        i++;
                        if (i < 60) {
                            System.out.print(".");
                        }
                        if (i % 20 == 0) {
                            System.out.println();
                            System.out.println("Generated Messages = " + persistentUnits.size());
                            System.out.println();
                        }
                        if (i == 60) {
                            i = 0;
                            ++min;
                            System.out.println();
                            System.out.println(" " + min + " min #= " + persistentUnits.size());
                            System.out.println();
                        }
                        this.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        status.start();
        long watchTransaction = Stopwatch.start("Time for creating random PersistentMessage Units", "all-messages");
        Set<PersistentMessageUnit> messageUnits = generateRandomMessages(NUMBER_SAMPLES);
        putOutTheGarbage();
        Stopwatch.stop(watchTransaction);
        System.out.println("Persistent Message Units: " + persistentUnits.size());

        status = new Thread("Status-Initializer") {
            public void run() {
                int i = 0;
                int min = 0;
                while (!DSPMongoCRUDService.hasFinished()) {
                    try {
                        i++;
                        if (i < 60) {
                            System.out.print(".");
                        }
                        if (i == 60) {
                            i = 0;
                            ++min;
                            System.out.println();
                            System.out.println(" " + min + " min on Initializing Persistence Service");
                            System.out.println();
                        }
                        this.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        status.start();
        watchTransaction = Stopwatch.start("Time for initializing the mongo service", "mongo-service");
        DSPMongoCRUDService.INSTANCE.initialize(SERVER_IP_ADDRESS, randomLocationsCache, PROPERTIES_LIST);
        Stopwatch.stop(watchTransaction);

        long end = System.currentTimeMillis();
        float result = getMemoryUsedSinceGivenMemorySample(startMemoryUse);
        System.out.println("Finished Generating " + NUMBER_SAMPLES + " sonde samples on "
                + getTimeDifference(start, end) + " consuming ~" + result + "Kb");

        status = new Thread("Status-Insertion") {
            public void run() {
                int i = 0;
                int min = 0;
                while (isInWriteLock()) {
                    try {
                        i++;
                        if (i < 60) {
                            System.out.print(".");
                        }
                        if (i % 20 == 0) {
                            System.out.println();
                            System.out.println("Inserted Units = " + getNumberOfInsertedDocuments());
                            System.out.println();
                        }
                        if (i == 60) {
                            i = 0;
                            ++min;
                            System.out.println();
                            System.out.println(" " + min + " min on Inserting Data");
                            System.out.println();
                        }
                        this.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        };
        status.start();
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
