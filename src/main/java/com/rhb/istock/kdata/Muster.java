package com.rhb.istock.kdata;

import java.math.BigDecimal;

public class Muster {
	private String itemID;
	private String itemName;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal close;
	private BigDecimal dropPrice;
	private BigDecimal latestPrice;

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getHNGap() {
		return highest.subtract(latestPrice).divide(latestPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public boolean isUpLimited() {
		return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(0.095))>=0;
	}

	public boolean isDownLimited() {
		return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(-0.095))<=0;
	}
	
	public boolean isDrop() {
		return latestPrice.compareTo(dropPrice)==-1;
	}
	
	public boolean isBreaker() {
		return latestPrice.compareTo(highest)==1; 
	}
	
	public boolean isDown() {
		return latestPrice.compareTo(close) == -1;
	}
	
	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}

	public Integer getHLGap() {
		return highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
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
	@Override
	public String toString() {
		return "Muster [itemID=" + itemID + ", itemName=" + itemName + ", amount=" + amount + ", averageAmount="
				+ averageAmount + ", highest=" + highest + ", lowest=" + lowest + ", close=" + close + ", dropPrice="
				+ dropPrice + ", latestPrice=" + latestPrice + "]";
	}
	
	
}
