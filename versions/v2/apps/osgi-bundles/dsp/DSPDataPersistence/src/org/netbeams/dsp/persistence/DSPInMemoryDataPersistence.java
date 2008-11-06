package org.netbeams.dsp.persistence;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The DSPInMemoryDataPersistence is an a representation of the in-memory data repository
 * that stores information about a given producer, by its identification, and a list
 * its data produced. 
 * 
 * The data is indexed to each producer, and the data is organized in a FIFO structure (linked list)
 * @author marcello
 */
public enum DSPInMemoryDataPersistence {

    /**
     * Singleton instance of the Memory
     */
    INSTANCE;

    /**
     * Data from resources
     */
    private Map<String, List<String>> values;

    private DSPInMemoryDataPersistence() {
        values = new LinkedHashMap<String, List<String>>();
        List<String> value = this.makeNewListForProducer();
        value.add("core-value");
        this.values.put("Core", value);
    }

    /**
     * @return The complete repository containing the references to producers and
     * the list of values they produced.
     */
    public synchronized Map<String, List<String>> getRepositoryData() {
        return this.values;
    }

    /**
     * Adds a new data produced;
     * @param producerReference is the producer reference
     * @param value the value produced.
     */
    public void addData(String producerReference, String ... data) {
        List<String> obList = this.values.get(producerReference);
        if (obList == null) {
            this.addValuesToNewProducer(producerReference, data);
        } else {
            List<String> completeData = this.makeNewListForProducer();
            for (String d : data) {
                completeData.add(d);
            }
            obList.addAll(completeData);
        }
    }
    
    /**
     * Helper method to add values to a new producer
     * @param producer the producer reference
     * @param values the values to be used
     */
    private void addValuesToNewProducer(String producer, String ... values) {
        List<String> newObList = this.makeNewListForProducer();
        for (String d : values) {
            newObList.add(d);
        }

        this.values.put(producer, newObList);
    }

    /**
     * Helper method to add values to a new producer reference
     * @param producer the refence to a producer.
     * @param values the values produced by the producer.
     */
    private void addValuesToNewProducer(String producer, List<String> values) {
        this.values.put(producer, values);
    }

    /**
     * Helper factory method with the implementation of the type of list for the 
     * messages.
     * @return
     */
    private List<String> makeNewListForProducer() {
        return new LinkedList<String>();
    }

    /**
     * Adds new values from a given producer reference.
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public void addData(String producerReference, List<String> valuesList) {
        List<String> obList = this.values.get(producerReference);
        if (obList == null) {
            List<String> newList = this.makeNewListForProducer();
            newList.addAll(valuesList);
            this.addValuesToNewProducer(producerReference, newList);
        } else {
            obList.addAll(valuesList);
        }
    }
}