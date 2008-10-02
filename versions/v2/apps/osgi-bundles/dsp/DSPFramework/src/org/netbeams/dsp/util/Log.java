package org.netbeams.dsp.util;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class Log {

	public static void log(String msg){
		System.out.println(msg);
	}
	
	public static void log(Throwable t){
		t.printStackTrace(System.out);
	}

	public static void log(String msg, Throwable t){
		System.out.println(msg);
		t.printStackTrace();
	}
	
	public static void log(Document document){
		log(document.getRootElement());
	}
	
	public static void log(Element ele){
		XMLOutputter outputter = new XMLOutputter();
		StringWriter writer = new StringWriter();
		try {
			outputter.output(ele, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(writer.toString());
	}
	
}
