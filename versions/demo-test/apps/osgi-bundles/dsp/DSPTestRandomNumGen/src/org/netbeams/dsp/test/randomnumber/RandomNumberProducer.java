package org.netbeams.dsp.test.randomnumber;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.MessageBrokerAccessor;
import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.util.NetworkUtil;

/**
 * 
 * @author Teresa L. Johnson <gamma.particle@gmail.com>
 * 
 */
public class RandomNumberProducer {

    private static final Logger log = Logger.getLogger(RandomNumberProducer.class);

    private List<RandomNumber> numberList;
    private DSPContext context;
    private RandomNumberGenerator rng;

    public RandomNumberProducer(DSPContext context) {
        this.context = context;
        numberList = new ArrayList<RandomNumber>();
        log.trace("The DSPRandomNumberProducer initialized: sends every 5 seconds");
    };

    public void startProducer() {
        rng = new RandomNumberGenerator();
        rng.start();
    }

    public void stopProducer() {
        if (rng != null) {
            rng.stopGenerator();
            rng = null;
        }
    }

    private void send(RandomNumbers data) throws DSPException {

        String localIPAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();

        log.debug("Random Numbers to be sent from " + localIPAddress);
        log.debug("Total number of Randum Numbers: " + data.getRandomNumbers().size());

        ComponentIdentifier producer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("RandomNumberProducer",
                localIPAddress, data.getContentContextForJAXB());

        ComponentIdentifier consumer = DSPMessagesFactory.INSTANCE.makeDSPComponentIdentifier("DSPWireTransportClient",
                System.getProperty("WIRE_TRANSPORT_SERVER_IP"), "org.netbeams.dsp.wiretransport.client");

        log.debug("Random Numbers to be sent to " + consumer.getComponentLocator().getNodeAddress().getValue());
        Header header = DSPMessagesFactory.INSTANCE.makeDSPMessageHeader(null, producer, consumer);

        DSProperties props = new DSProperties();
        DSProperty prop = new DSProperty();
        prop.setName("Random Data");
        prop.setValue(data.getRandomNumbers().get(0).toString());

        try {
            Message message = DSPMessagesFactory.INSTANCE.makeDSPMeasureMessage(header, props);

            // Always check if there is a broker available
            MessageBrokerAccessor messageBroker = context.getDataBroker();
            if (messageBroker != null) {
                messageBroker.send(message);
            } else {
                log.debug("Message broker not available");
            }

        } catch (DSPException e) {
            log.error(e.getMessage(), e);
        }
    }

    private class RandomNumberGenerator extends Thread {

        private SecureRandom random;
        private ArrayList<RandomNumber> randomNumbers;
        private RandomNumber randomNumber;
        private boolean running = true;
        private byte bytes[];
        private float fData;

        public RandomNumberGenerator() {
            randomNumbers = new ArrayList<RandomNumber>();
            random = new SecureRandom();
            bytes = new byte[20];
        }

        private void getRandomNumber() {
            ByteArrayInputStream byte_in = new ByteArrayInputStream(bytes);
            DataInputStream data_in = new DataInputStream(byte_in);
            try {
                fData = data_in.readFloat();
                randomNumber = new RandomNumber(fData);
                randomNumbers.add(randomNumber);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
            }
        }

        public void run() {
            while (running) {
                random.nextBytes(bytes);
                getRandomNumber();
                try {
                    send(new RandomNumbers(randomNumbers));
                    // System.out.println("Random Number: " + fData );
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.err.println("ERROR: " + e.getMessage());
                } catch (DSPException e) {
                    System.err.println("ERROR: " + e.getMessage());
                }
            }
        }

        public void stopGenerator() {
            this.running = false;
        }
    }
}
