package org.netbeams.dsp.message.test;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.netbeams.dsp.message.MeasureMessage;
import org.netbeams.dsp.message.Message;

public class MessageTest {

	public static void main(String args[]){
		
		try {
			JAXBContext jc = JAXBContext.newInstance("org.netbeams.dsp.message");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			JAXBElement<Message> jbaxElement = 
					(JAXBElement)unmarshaller.unmarshal(new File( "C:\\Netbeams\\versions\\v2\\apps\\xml\\samples\\stock_tick.xml"));
			MeasureMessage measureMessage = (MeasureMessage)jbaxElement.getValue();

			System.out.println("MessageID=" + measureMessage.getMessageID());
			Object object = measureMessage.getBody().getAny();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
