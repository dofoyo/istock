package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Kdata {
	private String itemID;
	private Map<LocalDate,Kbar> bars;

	public Kdata(String itemID) {
		this.itemID = itemID;
		this.bars = new TreeMap<LocalDate,Kbar>();
	}
	
	public void addBar(LocalDate date,BigDecimal open,BigDecimal high,BigDecimal low,BigDecimal close,BigDecimal amount,BigDecimal quantity) {
		this.bars.put(date, new Kbar(open, high, low, close, amount, quantity));
	}
	
	public void addBar(String date,String open,String high,String low,String close,String amount, String quantity) {
		this.bars.put(LocalDate.parse(date), new Kbar(new BigDecimal(open), new BigDecimal(high), new BigDecimal(low), new BigDecimal(close), new BigDecimal(amount), new BigDecimal(quantity)));
	}
	
	public Kbar getBar(LocalDate date){
		return this.bars.get(date);
	}
	
	public List<LocalDate> getDates() {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		for(Map.Entry<LocalDate, Kbar> entry : this.bars.entrySet()) {
			dates.add(entry.getKey());
		}
		return dates;
	}
	
	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	

}
