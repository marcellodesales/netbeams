package org.netbeams.dsp.message;

import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.netbeams.dsp.demo.mouseactions.ButtonName;
import org.netbeams.dsp.demo.mouseactions.EventName;
import org.netbeams.dsp.demo.mouseactions.MouseAction;
import org.netbeams.dsp.demo.mouseactions.MouseActionsContainer;
import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.ComponentIdentifier;
import org.netbeams.dsp.message.DSPMessagesFactory;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.MessagesContainer;

public class JaxbMessagesMarshallUnmarshallTest extends TestCase {

    private DSPMessagesFactory fac;

    public JaxbMessagesMarshallUnmarshallTest() {
        this.fac = DSPMessagesFactory.INSTANCE;
    }

    public void testMarshallMessagesContainerWithDifferentMessages() {

        ComponentIdentifier prod = this.fac.makeDSPComponentIdentifier("1", "LOCAL",
                "org.netbeams.dsp.platform.management.component.ComponentManager");
        ComponentIdentifier cons = this.fac.makeDSPComponentIdentifier(null, "LOCAL",
                "org.netbeams.dsp.demo.stocks.consumer");
        Header header = this.fac.makeDSPMessageHeader(null, System.currentTimeMillis(), prod, cons);

        StockTicks ticks = new StockTicks();
        StockTick tk1 = new StockTick();
        tk1.setName("Google, Inc.");
        tk1.setSymbol("GOOG");
        tk1.setValue((float) 23.4);
        ticks.getStockTick().add(tk1);

        MeasureMessage stocks = null;
        try {
            stocks = this.fac.makeDSPMessage("1", "org.netbeams.dsp.demo.stock", header, ticks);
        } catch (JAXBException e1) {
            fail(e1.getMessage());
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }

        // ------------------ Creation of the Mouse Actions
        ComponentIdentifier cons2 = this.fac.makeDSPComponentIdentifier(null, "192.168.0.101", "org.netbeams.dsp.mouseactions");
        Header header2 = this.fac.makeDSPMessageHeader(null, System.currentTimeMillis(), prod, cons2);

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
            mouseActions = this.fac.makeDSPMessage("2", "org.netbeams.dsp.demo.mouseactions", header2, macontainer);
        } catch (JAXBException e1) {
            fail(e1.getMessage());
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        }

        MessagesContainer messages = this.fac.makeDSPMessagesContainer();
        messages.getMessage().add(stocks);
        messages.getMessage().add(mouseActions);

        try {

            JAXBContext context = JAXBContext.newInstance("org.netbeams.dsp.message");
            Marshaller m = context.createMarshaller();
            String messagesExampleFilePath = "/tmp/messagesExample.xml";
            FileWriter fileWriter = new FileWriter(new File(messagesExampleFilePath));
            assertTrue("The marshalled should exist at " + messagesExampleFilePath, new File(messagesExampleFilePath).exists());
            m.marshal(messages, fileWriter);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
