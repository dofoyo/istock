package com.rhb.istock.kdata.muster;

import java.math.BigDecimal;

public class MusterEntity {
	private String itemID;
	private BigDecimal close;
	private BigDecimal amount;
	private BigDecimal latestPrice; 
	private Integer limited;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal lowest21;
	private BigDecimal lowest34;
	private BigDecimal averageAmount;
	private BigDecimal averagePrice;
	private BigDecimal averagePrice8;
	private BigDecimal averagePrice13;
	private BigDecimal averagePrice21;
	private BigDecimal averagePrice34;

	public MusterEntity(String itemID, BigDecimal close, BigDecimal amount, BigDecimal latestPrice, Integer limited, BigDecimal highest, BigDecimal lowest, 
			BigDecimal averageAmount, BigDecimal averagePrice, BigDecimal averagePrice8, BigDecimal averagePrice13, BigDecimal averagePrice21, BigDecimal averagePrice34, BigDecimal lowest21, BigDecimal lowest34) {
		this.itemID = itemID;
		this.close = close;
		this.amount = amount;
		this.latestPrice = latestPrice;
		this.limited = limited;
		this.highest = highest;
		this.lowest = lowest;
		this.lowest21 = lowest21;
		this.lowest34 = lowest34;
		this.averageAmount = averageAmount;
		this.averagePrice = averagePrice;
		this.averagePrice8 = averagePrice8;
		this.averagePrice13 = averagePrice13;
		this.averagePrice21 = averagePrice21;
		this.averagePrice34 = averagePrice34;
	}
	
	public MusterEntity(String txt) {
		String[] ss = txt.split(",");
		this.itemID = ss[0];
		this.close = new BigDecimal(ss[1]);
		this.amount = new BigDecimal(ss[2]);
		this.latestPrice = new BigDecimal(ss[3]);
		this.limited = Integer.parseInt(ss[4]);
		this.highest = new BigDecimal(ss[5]);
		this.lowest = new BigDecimal(ss[6]);
		this.averageAmount = new BigDecimal(ss[7]);
		this.averagePrice = new BigDecimal(ss[8]);
		this.averagePrice8 = new BigDecimal(ss[9]);
		this.averagePrice13 = new BigDecimal(ss[10]);
		this.averagePrice21 = new BigDecimal(ss[11]);
		this.averagePrice34 = new BigDecimal(ss[12]);
		this.lowest21 = new BigDecimal(ss[13]);
		this.lowest34 = new BigDecimal(ss[14]);
	}
	
	public BigDecimal getLowest34() {
		return lowest34;
	}

	public void setLowest34(BigDecimal lowest34) {
		this.lowest34 = lowest34;
	}

	public Integer getLimited() {
		return limited;
	}

	public void setLimited(Integer limited) {
		this.limited = limited;
	}

	public String toText() {
		return this.itemID + "," + 
				this.close + "," + 
				this.amount + "," + 
				this.latestPrice + "," + 
				this.limited + "," +
				this.highest + "," + 
				this.lowest + "," + 
				this.averageAmount + "," + 
				this.averagePrice + "," + 
				this.averagePrice8 + "," + 
				this.averagePrice13 + "," + 
				this.averagePrice21 + "," + 
				this.averagePrice34 + "," + 
				this.lowest21 + "," +  
				this.lowest34 + "\n"; 
	}

	public BigDecimal getLowest21() {
		return lowest21;
	}

	public void setLowest21(BigDecimal lowest21) {
		this.lowest21 = lowest21;
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

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public BigDecimal getAveragePrice8() {
		return averagePrice8;
	}

	public void setAveragePrice8(BigDecimal averagePrice8) {
		this.averagePrice8 = averagePrice8;
	}

	public BigDecimal getAveragePrice13() {
		return averagePrice13;
	}

	public void setAveragePrice13(BigDecimal averagePrice13) {
		this.averagePrice13 = averagePrice13;
	}

	public BigDecimal getAveragePrice21() {
		return averagePrice21;
	}

	public void setAveragePrice21(BigDecimal averagePrice21) {
		this.averagePrice21 = averagePrice21;
	}

	public BigDecimal getAveragePrice34() {
		return averagePrice34;
	}

	public void setAveragePrice34(BigDecimal averagePrice34) {
		this.averagePrice34 = averagePrice34;
	}

	@Override
	public String toString() {
		return "MusterEntity [itemID=" + itemID + ", close=" + close + ", amount=" + amount + ", latestPrice="
				+ latestPrice + ", limited=" + limited + ", highest=" + highest + ", lowest=" + lowest
				+ ", averageAmount=" + averageAmount + ", averagePrice=" + averagePrice + ", averagePrice8="
				+ averagePrice8 + ", averagePrice13=" + averagePrice13 + ", averagePrice21=" + averagePrice21
				+ ", averagePrice34=" + averagePrice34 + "]";
	}

	
}
