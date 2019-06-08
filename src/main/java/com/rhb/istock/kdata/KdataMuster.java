package com.rhb.istock.kdata;

import java.math.BigDecimal;

public class KdataMuster {
	private String itemID;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal price;
	private Integer period;
	private Integer count;
	
	public Integer getHLGap() {
		return highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public boolean isPeriodCount() {
		return this.period==this.count;
	}
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getPeriod() {
		return period;
	}
	public void setPeriod(Integer period) {
		this.period = period;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getAverageAmount() {
		return averageAmount;
	}
	public void setAverageAmount(BigDecimal averageAmount) {
		this.averageAmount = averageAmount;
	}
	public BigDecimal getHighest() {
		return highest;
	}
	public void setHighest(BigDecimal highest) {
		this.highest = highest;
	}
	public BigDecimal getLowest() {
		return lowest;
	}
	public void setLowest(BigDecimal lowest) {
		this.lowest = lowest;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "KdataMuster [itemID=" + itemID + ", amount=" + amount + ", averageAmount=" + averageAmount
				+ ", highest=" + highest + ", lowest=" + lowest + ", price=" + price + ", period=" + period + ", count="
				+ count + "]";
	}
	
	
}
