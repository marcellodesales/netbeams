package org.netbeams.dsp.messagesdirectory.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.netbeams.dsp.ComponentDescriptor;
import org.netbeams.dsp.DSPComponent;
import org.netbeams.dsp.DSPContext;
import org.netbeams.dsp.DSPException;
import org.netbeams.dsp.message.EventMessage;
import org.netbeams.dsp.message.Message;
import org.netbeams.dsp.message.MessagesContainer;
import org.netbeams.dsp.message.ObjectFactory;
import org.netbeams.dsp.messagesdirectory.model.DirectoryData;
import org.netbeams.dsp.messagesdirectory.model.MessagesQueueState;
import org.netbeams.dsp.util.Log;

/**
 * The DSPMessagesDirectory represents the directory of messages being produced by DSP components.
 * 
 * All messages are indexed in a data structure called outbound Queue, that represents all the messages sorted by the
 * destination URL of the component. For each of them, a Queue of DirectoryData is used to hold information about a
 * given DSP Message, its state, the MessagesContainer UUDI and the URL itself.
 * 
 * The data is indexed to each producer, and the data is organized in a FIFO Queue.
 * 
 * More information at http://code.google.com/p/netbeams/issues/detail?id=24
 * 
 * @author Marcello de Sales (marcello.sales@gmail.com)
 */
public enum DSPMessagesDirectory implements DSPComponent {

    /**
     * Singleton instance of the Messages Directory
     */
    INSTANCE;

    private static final Logger log = Logger.getLogger(DSPMessagesDirectory.class);

    /**
     * This is the hash of the URL destination and the Queue of the DirectoryData of the outbound messages.
     */
    private Map<URL, Queue<DirectoryData>> outboundQueue;

    // DSP Component Type
    public static final String COMPONENTS_SERVER_URL = "http://localhost:8080";
    public static final String COMPONENT_TYPE = "org.netbeams.dsp.messagesdirectory";
    private static ComponentDescriptor componentDescriptor;
    private String componentNodeId;
    private DSPContext context;

    private DSPMessagesDirectory() {
        this.outboundQueue = new LinkedHashMap<URL, Queue<DirectoryData>>();
    }

    /**
     * @return the default implementation of the queue
     */
    private Queue<DirectoryData> makeMessageQueue() {
        return new LinkedList<DirectoryData>();
    }

    /**
     * Adds new values from a given producer reference.
     * 
     * @param producerReference is the identification of a producer
     * @param valuesList the list of values from this producer.
     */
    public synchronized void addMessageToOutboundQueue(URL componentDestinition, Message dspMessage) {
        Queue<DirectoryData> outMsgs = this.outboundQueue.get(componentDestinition);
        if (outMsgs == null) {
            outMsgs = this.makeMessageQueue();
        }
        outMsgs.add(DirectoryData.makeNewInstance(componentDestinition, dspMessage));
        this.outboundQueue.put(componentDestinition, outMsgs);
    }

    /**
     * @param componentDestinition is the address of the component.
     * @return a new instance of MessagesContainer of the current messages that are on the QUEUED state for the given
     *         component destination.
     */
    public synchronized MessagesContainer retrieveQueuedMessagesForTransmission(URL componentDestinition) {
        MessagesContainer container = new ObjectFactory().createMessagesContainer();
        UUID containerId = UUID.randomUUID();

        container.setUudi(containerId.toString());
        container.setTime(System.currentTimeMillis());

        for (DirectoryData data : this.outboundQueue.get(componentDestinition)) {
            if (data.getState().equals(MessagesQueueState.QUEUED)) {
                container.getMessage().add(data.getMessage());
                data.setMessagesContainerId(containerId);
            }
        }
        return container;
    }

    /**
     * @param componentDestinition is the address of the component
     * @param containerId is the identification of the MessagesContainer created during before the transmission
     */
    public synchronized void setMessagesToTransmitted(URL componentDestinition, UUID containerId) {
        for (DirectoryData data : this.outboundQueue.get(componentDestinition)) {
            if (data.getState().equals(MessagesQueueState.QUEUED) && data.getContainerId().equals(containerId)) {
                data.changeStateToTransmitted();
            }
        }
    }

    /**
     * @param componentDestinition is the address of the destination of the component.
     * @return the current list of messages on the TRANSMITTED state. The ones that were tried to be sent to the server.
     */
    public synchronized List<Message> retrieveTransmittedMessages(URL componentDestinition) {
        List<Message> messages = new ArrayList<Message>();
        for (DirectoryData data : this.outboundQueue.get(componentDestinition)) {
            if (data.getState().equals(MessagesQueueState.TRANSMITTED)) {
                messages.add(data.getMessage());
            }
        }
        return messages;
    }

    /**
     * @param componentDestinition is the component destination.
     * @param containerId is the identification of the container.
     */
    public synchronized void setMessagesToAcknowledged(URL componentDestinition, UUID containerId) {
        for (DirectoryData data : this.outboundQueue.get(componentDestinition)) {
            if (data.getState().equals(MessagesQueueState.QUEUED) && data.getContainerId().equals(containerId)) {
                data.changeStateToAcknowledged();
            }
        }
    }

    /**
     * @param componentDestinition is the component URL for the messages.
     * @return the current list of the messages on the state ACKNOWLEDGED.
     */
    public synchronized List<Message> retrieveAcknowledgedMessages(URL componentDestinition) {
        List<Message> messages = new ArrayList<Message>();
        for (DirectoryData data : this.outboundQueue.get(componentDestinition)) {
            if (data.getState().equals(MessagesQueueState.ACKNOWLEDGED)) {
                messages.add(data.getMessage());
            }
        }
        return messages;
    }

    @Override
    public void deliver(Message message) throws DSPException {
        log.debug("message class=" + message.getClass().getName());

        if (message instanceof EventMessage) {

            String messagesContainerUUID = (String) message.getHeader().getCorrelationID();
            String messageUrl = message.getHeader().getProducer().getComponentLocator().getNodeAddress().getValue();
            try {
                this.setMessagesToAcknowledged(new URL(messageUrl), UUID.fromString(messagesContainerUUID));
            } catch (MalformedURLException e) {
                log.error("message class=" + message.getClass().getName(), e);
            }
        }
    }

    @Override
    public Message deliverWithReply(Message message) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message deliverWithReply(Message message, long waitTime) throws DSPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ComponentDescriptor getComponentDescriptor() {
        return componentDescriptor;
    }

    @Override
    public void startComponent() throws DSPException {
        Log.log("MessagesDirectory.startComponent()");
    }

    @Override
    public void stopComponent() throws DSPException {
        Log.log("MessagesDirectory.stopComponent()");
    }

    @Override
    public String getComponentNodeId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }

    @Override
    public void initComponent(String componentNodeId, DSPContext context) throws DSPException {
        Log.log("MessagesDirectory.initComponent()");
        this.context = context;
        this.componentNodeId = componentNodeId;
        Log.log("Context: " + this.context.toString() + "  with component ID " + this.componentNodeId);
    }
}