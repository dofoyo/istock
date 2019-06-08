package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;

public class KdataMusterEntity {
	private String itemID;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal price;
	private Integer period;
	private Integer count;

	public KdataMusterEntity(String itemID, BigDecimal averageAmount, BigDecimal amount, BigDecimal highest, BigDecimal lowest, BigDecimal price, Integer period, Integer count) {
		this.itemID = itemID;
		this.amount = amount;
		this.averageAmount = averageAmount;
		this.highest = highest;
		this.lowest = lowest;
		this.price = price;
		this.period = period;
		this.count = count;
		
	}
	
	public KdataMusterEntity(String txt) {
		String[] ss = txt.split(",");
		this.itemID = ss[0];
		this.amount = new BigDecimal(ss[1]);
		this.averageAmount = new BigDecimal(ss[2]);
		this.highest = new BigDecimal(ss[3]);
		this.lowest = new BigDecimal(ss[4]);
		this.price = new BigDecimal(ss[5]);
		this.period = Integer.parseInt(ss[6]);
		this.count = Integer.parseInt(ss[7]);
	}
	
	public String toText() {
		return this.itemID + "," + this.amount + "," + this.averageAmount + "," + this.highest + "," + this.lowest + "," + this.price + "," + this.period + "," + this.count;
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
		return "KdataMusterEntity [itemID=" + itemID + ", amount=" + amount + ", averageAmount=" + averageAmount
				+ ", highest=" + highest + ", lowest=" + lowest + ", price=" + price + ", period=" + period + ", count="
				+ count + "]";
	}
	
	
}
