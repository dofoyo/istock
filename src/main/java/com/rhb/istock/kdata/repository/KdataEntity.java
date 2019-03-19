package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

public class KdataEntity {
	private String itemID;
	private TreeMap<LocalDate,KbarEntity> bars;

	public KdataEntity(String itemID) {
		this.itemID = itemID;
		this.bars = new TreeMap<LocalDate,KbarEntity>();
	}
	
	public void addBar(LocalDate date,BigDecimal open,BigDecimal high,BigDecimal low,BigDecimal close,BigDecimal amount,BigDecimal quantity) {
		this.bars.put(date, new KbarEntity(open, high, low, close, amount,quantity));
	}
	
	public void addBar(String date,String open,String high,String low,String close,String amount,String quantity) {
		this.bars.put(LocalDate.parse(date), new KbarEntity(new BigDecimal(open), new BigDecimal(high), new BigDecimal(low), new BigDecimal(close), new BigDecimal(amount), new BigDecimal(quantity)));
	}
	
	public LocalDate getLatestDate() {
		TreeMap<LocalDate,KbarEntity> bs = (TreeMap<LocalDate,KbarEntity>)this.bars;
		return bs.lastKey();
	}
	
	public BigDecimal getAvarageAmount(Integer count) {
		BigDecimal total = new BigDecimal(0);
		NavigableSet<LocalDate> dates = this.bars.descendingKeySet();
		int i=0;
		for(LocalDate date : dates) {
			//System.out.println(date);
			if(i++ < count) {
				total = total.add(this.bars.get(date).getAmount());
			}else {
				break;
			}
		}
		
		return total.divide(new BigDecimal(count),BigDecimal.ROUND_HALF_UP);
	}
	
	public KbarEntity getBar(LocalDate date){
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
	
	public Map<LocalDate,KbarEntity> getBars(){
		return this.bars;
	}


	@Override
	public String toString() {
		return "KdataEntity [itemID=" + itemID + ", bars=" + bars + "]";
	}

}
