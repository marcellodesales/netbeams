package org.netbeams.dsp.persistence.model.component.data;

/**
 * PersistentMessageState is designed to identify the state of the messages on the Persistence Layer.
 * They can be on the states of Transient or Flushed.
 * 
 * @author Marcello de Sales March 20, 2009 19:33
 * 
 */
public enum PersistentMessageState {

    /**
     * Transient Messages are message in main memory;
     */
    TRANSIENT,
    /**
     * Messages that have been flushed to the data store.
     */
    FLUSHED;
}