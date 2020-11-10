package com.rhb.istock.index.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.comm.util.Functions;

public class IndexData {
	protected static final Logger logger = LoggerFactory.getLogger(IndexData.class);
	
	private String ts_code;
	private TreeMap<LocalDate,IndexBar> bars;

	public String getItemID() {
		String code = this.ts_code.substring(0,6);
		if(this.ts_code.contains(".SH")) {
			return "sh" + code;
		}else {
			return "sz" + code;
		}
	}
	
	public boolean isExist(LocalDate date) {
		return this.bars.containsKey(date);
	}
	
	public IndexData(String ts_code) {
		this.ts_code = ts_code;
		this.bars = new TreeMap<LocalDate,IndexBar>();
	}
	
	public void addBar(IndexBar bar) {
		this.bars.put(bar.getDate(), bar);
	}
	
	public void addBar(LocalDate trade_date, BigDecimal close, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal pre_close, BigDecimal vol, BigDecimal amount
					) {
		this.bars.put(trade_date, new IndexBar(trade_date,close, open, high, low, pre_close, vol, amount));
	}
	
	public void addBar(String trade_date, String close, String open, String high, String low,
			String pre_close, String vol, String amount
				) {
		if(!"null".equals(trade_date) 
				&& !"null".equals(close)
				&& !"null".equals(open)
				&& !"null".equals(high)
				&& !"null".equals(low)
				&& !"null".equals(pre_close)
				&& !"null".equals(vol)
				&& !"null".equals(amount)
				) {
			this.bars.put(LocalDate.parse(trade_date,DateTimeFormatter.ofPattern("yyyyMMdd")), 
					new IndexBar(LocalDate.parse(trade_date,DateTimeFormatter.ofPattern("yyyyMMdd")),close, open, high, low, pre_close, vol, amount));
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
	
	public String getTs_code() {
		return ts_code;
	}

	public void setTs_code(String ts_code) {
		this.ts_code = ts_code;
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
		if(lowest ==null || lowest.compareTo(BigDecimal.ZERO)<1) {
			return 0;
		}else {
			BigDecimal close = bars.lastEntry().getValue().getClose();
			Integer ratio = Functions.growthRate(close, lowest);
			//System.out.println(close);
			//System.out.println(lowest);
			//System.out.println(ratio);

			return ratio;
		}
	}


	@Override
	public String toString() {
		return "KdataEntity [ts_code=" + ts_code + ", bars=" + bars + "]";
	}

}
