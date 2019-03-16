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
	
	public String getString() {
		//System.out.println(bars.size());
		StringBuffer sb = new StringBuffer();
		sb.append("'"+itemID+"'	日线\n");
		sb.append("日期	开盘	最高	最低	收盘	成交量	成交额\n");
		for(Map.Entry<LocalDate, Kbar> entry : bars.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue().getOpen());
			sb.append(" ");
			sb.append(entry.getValue().getHigh());
			sb.append(" ");
			sb.append(entry.getValue().getLow());
			sb.append(" ");
			sb.append(entry.getValue().getClose());
			sb.append(" ");
			sb.append(entry.getValue().getQuantity());
			sb.append(" ");
			sb.append(entry.getValue().getAmount());
			sb.append("\n");
		}
		
		return sb.toString();
	}

	

}
