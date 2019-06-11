package com.rhb.istock.kdata.muster;

import java.math.BigDecimal;

public class MusterEntity {
	private String itemID;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal close;
	private BigDecimal dropPrice;
	private BigDecimal latestPrice;  

	public MusterEntity(String itemID, BigDecimal amount, BigDecimal averageAmount, BigDecimal highest, BigDecimal lowest, BigDecimal close, BigDecimal dropPrice, BigDecimal latestPrice) {
		this.itemID = itemID;
		this.amount = amount;
		this.averageAmount = averageAmount;
		this.highest = highest;
		this.lowest = lowest;
		this.close = close;
		this.dropPrice = dropPrice;
		this.latestPrice = latestPrice;
	}
	
	public MusterEntity(String txt) {
		String[] ss = txt.split(",");
		this.itemID = ss[0];
		this.amount = new BigDecimal(ss[1]);
		this.averageAmount = new BigDecimal(ss[2]);
		this.highest = new BigDecimal(ss[3]);
		this.lowest = new BigDecimal(ss[4]);
		this.close = new BigDecimal(ss[5]);
		this.dropPrice = new BigDecimal(ss[6]);
		this.latestPrice = new BigDecimal(ss[7]);
	}
	
	public String toText() {
		return this.itemID + "," + this.amount + "," + this.averageAmount + "," + this.highest + "," + this.lowest + "," + this.close + "," + this.dropPrice + "," + this.latestPrice + "\n";
	}

	public BigDecimal getDropPrice() {
		return dropPrice;
	}

	public void setDropPrice(BigDecimal dropPrice) {
		this.dropPrice = dropPrice;
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

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}

	@Override
	public String toString() {
		return "MusterEntity [itemID=" + itemID + ", amount=" + amount + ", averageAmount=" + averageAmount
				+ ", highest=" + highest + ", lowest=" + lowest + ", close=" + close + ", dropPrice=" + dropPrice + "]";
	}
	
}
