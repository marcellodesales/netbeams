package org.netbeams.dsp.demo.stocks.data;

import java.util.Collection;
import java.util.Date;

import org.netbeams.dsp.data.MessageContent;
import org.netbeams.dsp.demo.stocks.data.StockCT;
import org.netbeams.dsp.demo.stocks.data.StockTick;

public class StockTickData implements MessageContent{

	private Date collectionDate;
	private Collection<StockTick> ticks;
	
	@Override
	public String getContentType() {
		return StockCT.STOCK_TICK;
	}
	
	public Date getCollectionDate() {
		return collectionDate;
	}
	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}
	public Collection<StockTick> getTicks() {
		return ticks;
	}
	public void setTicks(Collection<StockTick> ticks) {
		this.ticks = ticks;
	}
	
}
