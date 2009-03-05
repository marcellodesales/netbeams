package org.netbeams.dsp.message;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.netbeams.dsp.data.property.DSProperties;
import org.netbeams.dsp.data.property.DSProperty;
import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.util.NetworkUtil;
import org.netbeams.dsp.ysi.SondeDataContainer;
import org.netbeams.dsp.ysi.SondeDataType;

public class JaxbMessagesMarshallUnmarshallTest extends TestCase {

    private DSPMessagesFactory fac;
    private File xmlObjectPersisted = new File("/tmp/messagesExample2.xml");

    public JaxbMessagesMarshallUnmarshallTest() {
        this.fac = DSPMessagesFactory.INSTANCE;        
    }

    /**
     * Verifies if a message container with different messages from different DSP components can be
     * marshalled.
     */
    public void testMarshallMessagesContainerWithDifferentMessages() {

        ComponentIdentifier prod = this.fac.makeDSPComponentIdentifier("1", "LOCAL",
                "org.netbeams.dsp.platform.management.component.ComponentManager");
        ComponentIdentifier cons = this.fac.makeDSPComponentIdentifier(null, "LOCAL",
                "org.netbeams.dsp.demo.stocks.consumer");
        Header header = this.fac.makeDSPMessageHeader(null, prod, cons);

        /* ------------------ Creation of the Stock Ticks
        The DSP component writer needs just to create the payload in the way it should be as the
        body of the DSP message.
      */
        StockTicks ticks = new StockTicks();
        StockTick tk1 = new StockTick();
        tk1.setName("Google, Inc.");
        tk1.setSymbol("GOOG");
        tk1.setValue((float) 23.4);
        ticks.getStockTick().add(tk1);

        //Specifying the correct content type for the message as the package of the component
        MeasureMessage stocks = this.fac.makeDSPMeasureMessage(header, ticks);

        ComponentIdentifier prod3 = this.fac.makeDSPComponentIdentifier("1234", "LOCAL",
        "org.netbeams.dsp.platform.management.component.ComponentManager");
        ComponentIdentifier cons3 = this.fac.makeDSPComponentIdentifier(null, "LOCAL",
                "org.netbeams.dsp.wiretransport.client");
        Header header3 = this.fac.makeDSPMessageHeader(null, prod3, cons3);
        
        /* ------------------ Creation of the Stock Ticks
        The DSP component writer needs just to create the payload in the way it should be as the
        body of the DSP message.
        */  
        
        DSProperties properties = new DSProperties();
        DSProperty serverIp = new DSProperty();
        serverIp.setName("WIRE_TRANSPORT_SERVER_IP");
        serverIp.setValue("127.0.0.1");
        properties.getProperty().add(serverIp);
        
        DSProperty serverPort = new DSProperty();
        serverPort.setName("WIRE_TRANSPORT_SERVER_PORT");
        serverPort.setValue("8080");
        properties.getProperty().add(serverPort);
        
        DSProperty serverUri = new DSProperty();
        serverUri.setName("HTTP_SERVER_BASE_URI");
        serverUri.setValue("/transportDspMessages");
        properties.getProperty().add(serverUri);
        
        DSProperty serverVar = new DSProperty();
        serverVar.setName("HTTP_SERVER_REQUEST_VARIABLE");
        serverVar.setValue("dspMessagesContainer");
        properties.getProperty().add(serverVar);
        
        UpdateMessage updates = null;

        //Specifying the correct content type for the message as the package of the component
        updates = this.fac.makeDSPUpdateMessage(header3, properties);

        ComponentIdentifier cons2 = this.fac.makeDSPComponentIdentifier(null, "192.168.0.101",
                "org.netbeams.dsp.mouseactions");
        Header header2 = this.fac.makeDSPMessageHeader(null, prod, cons2);

        /* ------------------ Creation of the Mouse Actions
           The DSP component writer needs just to create the payload in the way it should be as the
           body of the DSP message.
         */
        MouseActionsContainer macontainer = new MouseActionsContainer();
        MouseAction ma = new MouseAction();
        ma.setButton(ButtonName.CENTER);
        ma.setEvent(EventName.CLICKED);
        ma.setX(3);
        ma.setY(5);
        MouseAction ma1 = new MouseAction();
        ma1.setButton(ButtonName.NONE);
        ma1.setEvent(EventName.MOVED);
        ma1.setX(4);
        ma1.setY(7);
        macontainer.getMouseAction().add(ma);
        macontainer.getMouseAction().add(ma1);

        MeasureMessage mouseActions = null;
        
          //Specifying the correct content type for the message as the package of the component
            mouseActions = this.fac.makeDSPMeasureMessage(header2, macontainer);

        String destIpAddress = NetworkUtil.getCurrentEnvironmentNetworkIp();
        
        SondeDataContainer sondeContainer = new SondeDataContainer();
        SondeDataType sData = new SondeDataType();
        sData.setTime("20:30:40");
        sData.setDate("04/30/2009");
        sData.setCond("blue skies");
        sData.setBattery("full-Power");
        sData.setPH("negative");
        sData.setPress("too-much");
        sondeContainer.getSondeData().add(sData);
        MeasureMessage sondeMeasurements = this.fac.makeDSPMeasureMessage(header3, sondeContainer);
        
        // ------------------ Creation of the Messages Container
        MessagesContainer messages = this.fac.makeDSPMessagesContainer(destIpAddress);
//        messages.getMessage().add(stocks);
        messages.getMessage().add(sondeMeasurements);
        messages.getMessage().add(updates);
        try {

            // ------------------ Marshalling of the Messages Container (to be on the transport component)
            FileWriter fileWriter = new FileWriter(this.xmlObjectPersisted);
            fileWriter.append(messages.toXml());
            fileWriter.flush();
            fileWriter.close();
            assertTrue("Marshalled object doesn't exist at " + this.xmlObjectPersisted.getAbsolutePath(), 
                    this.xmlObjectPersisted.exists());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
//    public void testUnmashallMessagesContainerWithDifferentMessages() {
//        try {
//            JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
//            Unmarshaller um = context.createUnmarshaller();
//            MessagesContainer container = (MessagesContainer)um.unmarshal(this.xmlObjectPersisted);
//            
//            assertNotNull("The messages container was not unmarshalled", container);
//            assertNotNull("The messages container must not be null", container.getMessage());
//            assertEquals("The messages container must have 2 messages", 2, container.getMessage().size()); 
//            
//            for(AbstractMessage dspMsg : container.getMessage()) {
//                Object obj = dspMsg.getBody().getAny();
//                assertTrue("The body is not an instance of Node", obj instanceof Node);
//                JAXBContext payloadCtx = JAXBContext.newInstance(dspMsg.getContentType());
//                Unmarshaller umPayLoad = payloadCtx.createUnmarshaller();
//
//                assertTrue("The message is not an instance of MeasureMessage", dspMsg instanceof MeasureMessage);
//                
//                if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.stock")) {
//                    StockTicks ticks = (StockTicks)umPayLoad.unmarshal((Node)obj);
//                    assertNotNull("The payload was not unmashalled", ticks);
//                    assertNotNull("The list of stock ticks is null", ticks.getStockTick());
//                    assertEquals("The list of stock ticks must have one element", 1, ticks.getStockTick().size());
//                    for (StockTick sttk : ticks.getStockTick()) {
//                        assertEquals("The name of the stock is different", "Google, Inc.", sttk.getName());
//                    }
//                } else 
//                if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.mouseactions")) {
//                    MouseActionsContainer mouseacts = (MouseActionsContainer)umPayLoad.unmarshal((Node)obj);
//                    assertNotNull("The payload was not unmashalled", mouseacts);
//                    assertNotNull("The list of stock ticks is null", mouseacts.getMouseAction());
//                    assertEquals("The list of stock ticks must have one element", 2, mouseacts.getMouseAction().size());
//                    for (MouseAction sttk : mouseacts.getMouseAction()) {
//                        assertNotNull("The mouse action is null", sttk);
//                    }
//                }                
//            }
//            //Tearing down from the unmarshall method.
//            if (this.xmlObjectPersisted.exists()) {
//                this.xmlObjectPersisted.delete();
//            }
//            
//        } catch (JAXBException e) {
//            fail(e.getMessage());
//        }
//    }
}
