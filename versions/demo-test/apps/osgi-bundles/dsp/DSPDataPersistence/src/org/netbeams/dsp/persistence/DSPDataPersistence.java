package org.netbeams.dsp.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DSPDataPersistence is the main interface for the data
 * @author marcello
 */
public interface DSPDataPersistence {

	/**
	 * Inserts a new data collection into the persistence container.
	 * @param producerReference is the universal identifier of the producer
	 * @param newDataList a list of data.
	 */
	public void insertData(UUID producerReference, List<String> newDataList);
	
	/**
	 * Inserts a new data into the persistence container
	 * @param producerReference is the universal identifier of the producer
	 * @param newDataList is the new data to be added.
	 */
	public void insertData(UUID producerReference, String ... newDataList);

   /**
     * Inserts a new data into the persistence container
     * @param producerReference is the universal identifier of the producer
     * @param newData the new data.
     */
    public void insertData(UUID producerReference, String data);
    
	/**
	 * @return the collection of data in a current persistence container for a given producer
	 * @param producerReference is the universal identifier of the producer
	 */
	public List<String> retrieveData(UUID producerReference);
	
	/**
	 * @return The set of existing producers.
	 */
	public Set<UUID> retrieveProducersIdSet();
}
