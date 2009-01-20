package org.netbeams.dsp.message;

import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.w3c.dom.Node;

public class JaxbMessagesMarshallUnmarshallTest extends TestCase {

    private DSPMessagesFactory fac;
    private File xmlObjectPersisted = new File("/tmp/messagesExample.xml");

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
        Header header = this.fac.makeDSPMessageHeader(null, System.currentTimeMillis(), prod, cons);

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

        MeasureMessage stocks = null;
        try {
            //Specifying the correct content type for the message as the package of the component
            stocks = this.fac.makeDSPMeasureMessage("1", "org.netbeams.dsp.demo.stock", header, ticks);
        } catch (JAXBException e1) {
            fail(e1.getMessage());
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }

        ComponentIdentifier cons2 = this.fac.makeDSPComponentIdentifier(null, "192.168.0.101",
                "org.netbeams.dsp.mouseactions");
        Header header2 = this.fac.makeDSPMessageHeader(null, System.currentTimeMillis(), prod, cons2);

        /* ------------------ Creation of the Mouse Actions
           The DSP component writer needs just to create the payload in the way it should be as the
           body of the DSP message.
         */
        MouseActionsContainer macontainer = new MouseActionsContainer();
        MouseAction ma = new MouseAction();
        ma.setButton(ButtonName.CENTER);
        ma.setEvent(EventName.CLICKED);
        ma.setValue("Button clicked at (3.5)");
        ma.setX(3);
        ma.setY(5);
        MouseAction ma1 = new MouseAction();
        ma1.setButton(ButtonName.NONE);
        ma1.setEvent(EventName.MOVED);
        ma1.setValue("Button moved at (4.7)");
        ma1.setX(4);
        ma1.setY(7);
        macontainer.getMouseAction().add(ma);
        macontainer.getMouseAction().add(ma1);

        MeasureMessage mouseActions = null;
        try {
          //Specifying the correct content type for the message as the package of the component
            mouseActions = this.fac.makeDSPMeasureMessage("2", "org.netbeams.dsp.demo.mouseactions", header2, macontainer);
        } catch (JAXBException e1) {
            fail(e1.getMessage());
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }

        // ------------------ Creation of the Messages Container
        MessagesContainer messages = this.fac.makeDSPMessagesContainer();
        messages.getMessage().add(stocks);
        messages.getMessage().add(mouseActions);

        try {

            // ------------------ Marshalling of the Messages Container (to be on the transport component)
            JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
            Marshaller m = context.createMarshaller();
            FileWriter fileWriter = new FileWriter(this.xmlObjectPersisted);
            m.marshal(messages, fileWriter);
            assertTrue("Marshalled object doesn't exist at " + this.xmlObjectPersisted.getAbsolutePath(), 
                    this.xmlObjectPersisted.exists());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void testUnmashallMessagesContainerWithDifferentMessages() {
        try {
            JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
            Unmarshaller um = context.createUnmarshaller();
            MessagesContainer container = (MessagesContainer)um.unmarshal(this.xmlObjectPersisted);
            
            assertNotNull("The messages container was not unmarshalled", container);
            assertNotNull("The messages container must not be null", container.getMessage());
            assertEquals("The messages container must have 2 messages", 2, container.getMessage().size()); 
            
            for(AbstractMessage dspMsg : container.getMessage()) {
                Object obj = dspMsg.getBody().getAny();
                assertTrue("The body is not an instance of Node", obj instanceof Node);
                JAXBContext payloadCtx = JAXBContext.newInstance(dspMsg.getContentType());
                Unmarshaller umPayLoad = payloadCtx.createUnmarshaller();

                if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.stock")) {
                    StockTicks ticks = (StockTicks)umPayLoad.unmarshal((Node)obj);
                    assertNotNull("The payload was not unmashalled", ticks);
                    assertNotNull("The list of stock ticks is null", ticks.getStockTick());
                    assertEquals("The list of stock ticks must have one element", 1, ticks.getStockTick().size());
                    for (StockTick sttk : ticks.getStockTick()) {
                        assertEquals("The name of the stock is different", "Google, Inc.", sttk.getName());
                    }
                } else 
                if (dspMsg.getContentType().equals("org.netbeams.dsp.demo.mouseactions")) {
                    MouseActionsContainer mouseacts = (MouseActionsContainer)umPayLoad.unmarshal((Node)obj);
                    assertNotNull("The payload was not unmashalled", mouseacts);
                    assertNotNull("The list of stock ticks is null", mouseacts.getMouseAction());
                    assertEquals("The list of stock ticks must have one element", 2, mouseacts.getMouseAction().size());
                    for (MouseAction sttk : mouseacts.getMouseAction()) {
                        assertNotNull("The mouse action is null", sttk);
                    }
                }                
            }
            //Tearing down from the unmarshall method.
            if (this.xmlObjectPersisted.exists()) {
                this.xmlObjectPersisted.delete();
            }
            
        } catch (JAXBException e) {
            fail(e.getMessage());
        }
    }
}
