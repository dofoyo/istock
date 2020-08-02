package com.rhb.istock.index.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Kdata;

public class IndexData {
	private String itemID;
	private TreeMap<LocalDate,IndexBar> bars;

	public IndexData(String itemID) {
		this.itemID = itemID;
		this.bars = new TreeMap<LocalDate,IndexBar>();
	}
	
	public void addBar(LocalDate trade_date, BigDecimal close, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal pre_close, BigDecimal change, BigDecimal pct_chg, BigDecimal vol, BigDecimal amount
					) {
		this.bars.put(trade_date, new IndexBar(close, open, high, low, pre_close, change, pct_chg, vol, amount));
	}
	
	public void addBar(String trade_date, String close, String open, String high, String low,
			String pre_close, String change, String pct_chg, String vol, String amount
				) {
		if(!"null".equals(trade_date) 
				&& !"null".equals(close)
				&& !"null".equals(open)
				&& !"null".equals(high)
				&& !"null".equals(low)
				&& !"null".equals(pre_close)
				&& !"null".equals(change)
				&& !"null".equals(pct_chg)
				&& !"null".equals(vol)
				&& !"null".equals(amount)
				) {
			this.bars.put(LocalDate.parse(trade_date,DateTimeFormatter.ofPattern("yyyyMMdd")), 
					new IndexBar(close, open, high, low, pre_close, change, pct_chg, vol, amount));
		}
	}
	
	public void addBar(LocalDate trade_date, IndexBar bar) {
		this.bars.put(trade_date, bar);
	}
	
	public Set<LocalDate> getDates(){
		return this.bars.keySet();
	}
	
	public LocalDate getLastDate() {
		return this.bars.lastKey();
	}
	
	public IndexBar getBar(LocalDate date){
		return this.bars.get(date);
	}
	
	public Integer getBarSize() {
		return this.bars.size();
	}
	
	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	
	public Map<LocalDate,IndexBar> getBars(){
		return this.bars;
	}
	
	private BigDecimal getLowest() {
		BigDecimal lowest = null;
		for(Map.Entry<LocalDate, IndexBar> entry : this.bars.entrySet()) {
			//System.out.println(entry.getKey() + "," + entry.getValue());
			if(lowest==null) {
				lowest = entry.getValue().getLow();
			}else {
				lowest = lowest.compareTo(entry.getValue().getLow())==1 ? entry.getValue().getLow() : lowest;
			}
			
		}
		return lowest;
	}
	
	public Integer growthRate() {
		BigDecimal lowest = this.getLowest();
		if(lowest ==null) {
			return 0;
		}else {
			BigDecimal close = bars.lastEntry().getValue().getClose();
			Integer ratio = Functions.growthRate(close, lowest);
			return ratio;
		}
	}


	@Override
	public String toString() {
		return "KdataEntity [itemID=" + itemID + ", bars=" + bars + "]";
	}

}
