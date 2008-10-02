package org.netbeams.dsp.core.stockproducer;

/**
 * <stk:tickers xmlns:stk="http://www.snpa.org/stock.xsd">
 *   <stk:ticker>
 *     <stk:name></stk:name>
 *     <stk:symbol></stk:symbol>
 *     <stk:value></stk:value>
 *     <stk:when></stk:when>
 *   </stk:ticker>
 * </stk:ticker>
 */

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public class StockJDOMHelper {
	
	public StockJDOMHelper() {}
	
	public Element generateStockTicks(List<StockTicker> ticks){
		// <stk:ticks xmlns:stk="http://www.dmp.org/stock.xsd">
		Element eTicks = new Element(XMLConstants.ELEMENT_TICKS, XMLConstants.NAMESPACE_PREFIX, 
				XMLConstants.NAMESPACE_URI);
		Namespace ns = eTicks.getNamespace();
		for(StockTicker tick: ticks){
			// <stk:tick>
			Element eTick = new Element(XMLConstants.ELEMENT_TICK, ns);
			eTicks.addContent(eTick);
			// <stk:name></stk:name>
			Element eName = new Element(XMLConstants.ELEMENT_NAME, ns);
			eName.setText(tick.name);
			eTick.addContent(eName);
			// <stk:symbol></stk:symbol>
			Element eSymbol = new Element(XMLConstants.ELEMENT_SYMBOL, ns);
			eSymbol.setText(tick.symbol);
			eTick.addContent(eSymbol);
			// <stk:value></stk:value>
			Element eValue = new Element(XMLConstants.ELEMENT_VALUE, ns);
			eValue.setText(String.valueOf(tick.value));
			eTick.addContent(eValue);
			// <stk:value></stk:value>
			Element eWhen = new Element(XMLConstants.ELEMENT_WHEN, ns);
			eWhen.setText(String.valueOf(tick.when));
			eTick.addContent(eWhen);
		}
		return eTicks;
	}

}
