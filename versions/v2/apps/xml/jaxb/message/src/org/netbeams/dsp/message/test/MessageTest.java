package org.netbeams.dsp.message.test;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.netbeams.dsp.demo.stock.StockTick;
import org.netbeams.dsp.demo.stock.StockTicks;
import org.netbeams.dsp.message.Body;
import org.netbeams.dsp.message.Header;
import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;

public class MessageTest {

	public static void main(String args[]){
		MessageTest mt = new MessageTest();
		mt.testUnmarshall();
//		mt.testMarshall();

	}
	
	
	void testUnmarshall(){
		try {
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			MeasureMessage measureMessage = 
					(MeasureMessage)unmarshaller.unmarshal(new File( "C:\\Netbeams\\versions\\v2\\apps\\xml\\samples\\stock_tick.xml"));

			System.out.println("CreationTime=" + measureMessage.getHeader().getCreationTime());
			Object object = measureMessage.getBody().getAny();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void testMarshall(){
		StockTicks ticks = generateStockTicks();
		Header header = new Header();
		header.setCreationTime(10);
		Body body = new Body();
		body.setAny(ticks);
		MeasureMessage message = new MeasureMessage();
		message.setHeader(header);
		message.setBody(body);
		
		try{
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message:org.netbeams.dsp.demo.stock");
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					   new Boolean(true));
			marshaller.marshal(message, new File("C:\\Netbeams\\versions\\v2\\apps\\xml\\jaxb\\message\\output\\stocktick.xml"));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


	private StockTicks generateStockTicks() {
		StockTicks ticks = new StockTicks();
		StockTick tick = new StockTick();
		tick.setSymbol("GOOG");
		tick.setName("Google");
		tick.setValue(357.86f);
		ticks.getStockTick().add(tick);
		
		
		return ticks;
	}
	
	
}
