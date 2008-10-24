package org.netbeams.dsp.demo.stocks.data;

public class StockTick {
	
	private String stockSymbol;
	private float value;
	
	public String getStockSymbol() {
		return stockSymbol;
	}
	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	
	
}
