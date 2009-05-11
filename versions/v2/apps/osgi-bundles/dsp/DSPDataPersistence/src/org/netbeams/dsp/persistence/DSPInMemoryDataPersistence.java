package org.netbeams.dsp.persistence;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private Map<UUID, List<String>> repositoryData;

    private DSPInMemoryDataPersistence() {
        this.repositoryData = new LinkedHashMap<UUID, List<String>>();
        List<String> value = this.makeNewListForProducer();
        value.add("core-value");
        this.repositoryData.put(UUID.randomUUID(), value);
    }

    /**
     * @return The complete repository containing the references to producers and
     * the list of values they produced.
     */
    public synchronized Map<UUID, List<String>> getRepositoryData() {
        return this.repositoryData;
    }

    /**
     * Helper method to add values to a new producer
     * @param producer the producer reference
     */
    private void addValuesToNewProducer(UUID producer, String ... values) {
        List<String> newObList = makeNewListForProducer(values);
        this.repositoryData.put(producer, newObList);
    }

    /**
     * Helper method to add values to a new producer reference
     * @param producer the reference to a producer.
     * @param values the values produced by the producer.
     */
    private void addValuesToNewProducer(UUID producer, List<String> values) {
        this.repositoryData.put(producer, values);
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
     * @param values set of values
     * @return a new List<String> based on the array
     */
    private List<String> makeNewListForProducer(String... values) {
        List<String> newObList = this.makeNewListForProducer();
        for (String d : values) {
            newObList.add(d);
        }
        return newObList;
    }

    /**
     * Adds new values from a given producer reference.
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    //@Override
    public void insertData(UUID producerReference, List<String> newDataList) {
        List<String> obList = this.repositoryData.get(producerReference);
        if (obList == null) {
            List<String> newList = this.makeNewListForProducer();
            newList.addAll(newDataList);
            this.addValuesToNewProducer(producerReference, newList);
        } else {
            obList.addAll(newDataList);
        }
    }

    /**
     * Adds a new data produced;
     * @param producerReference is the producer reference
     * @param value the value produced.
     */
    //@Override
    public void insertData(UUID producerReference, String... newDataList) {
        List<String> obList = this.repositoryData.get(producerReference);
        if (obList == null) {
            List<String> newList = this.makeNewListForProducer(newDataList);
            this.addValuesToNewProducer(producerReference, newList);
        } else this.addValuesToNewProducer(producerReference, newDataList);
    }

    //@Override
    public void insertData(UUID producerReference, String data) {
        List<String> obList = this.repositoryData.get(producerReference);
        if (obList == null) {
            List<String> newList = this.makeNewListForProducer();
            newList.addAll(newList);
            this.addValuesToNewProducer(producerReference, newList);
        } else this.addValuesToNewProducer(producerReference, new String[]{data});
    }

    //@Override
    public List<String> retrieveData(UUID producerReference) {
        return this.repositoryData.get(producerReference);
    }

    //@Override
    public Set<UUID> retrieveProducersIdSet() {
        return this.repositoryData.keySet();
    }
    
    public static void main(String[] args) {
        
        DSPInMemoryDataPersistence memRepository = DSPInMemoryDataPersistence.INSTANCE;
        System.out.println("<BR><BR>Producers and their observable values.");
        Map<UUID, List<String>> memRepo = memRepository.getRepositoryData();
        for (UUID observer : memRepo.keySet()) {
            System.out.println("<BR><b>Producer = " + observer + "</b>");
            for (String value : memRepo.get(observer)) {
                System.out.println("<BR><b>value = " + value);
            }
        }
    }
}