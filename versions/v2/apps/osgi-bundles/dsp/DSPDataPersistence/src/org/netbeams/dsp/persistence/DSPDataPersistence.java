package org.netbeams.dsp.persistence;

import java.util.List;

/**
 * DSPDataPersistence is the main interface for the data
 * @author marcello
 */
public interface DSPDataPersistence {

	/**
	 * Inserts a new data collection into the persistence container.
	 * @param newDataCollection a collection of data.
	 */
	public void insertData(List<String> newDataCollection);
	
	/**
	 * Inserts a new data into the persistence container
	 * @param newData the new data.
	 */
	public void insertData(String ... newData);

   /**
     * Inserts a new data into the persistence container
     * @param newData the new data.
     */
    public void insertData(String newData);
    
	/**
	 * @return the collection of data in a current persistence container
	 */
	public List<String> retrieveData();
}
