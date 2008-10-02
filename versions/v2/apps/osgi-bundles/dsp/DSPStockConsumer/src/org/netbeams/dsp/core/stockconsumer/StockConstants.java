package org.netbeams.dsp.core.stockconsumer;


public interface StockConstants {
	
	public static final String NAMESPACE_URI = "http://www.snpa.org/stock.xsd";
	public static final String NAMESPACE_PREFIX = "stk";
	
	public static final String  ELEMENT_TICKERS = "tickers";
	public static final String  ELEMENT_TICKER = "ticker";
	public static final String  ELEMENT_NAME = "name";
	public static final String  ELEMENT_SYMBOL = "symbol";
	public static final String  ELEMENT_VALUE = "value";
	
	public final static String MEASUREMENT_TYPE_STOCK_TICK = "stock_tick";
	
}
