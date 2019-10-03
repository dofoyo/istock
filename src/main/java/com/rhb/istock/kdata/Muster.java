package com.rhb.istock.kdata;

import java.math.BigDecimal;

public class Muster {
	private String itemID;
	private String itemName;
	private String industry;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal close; 		//上一交易日收盘价
	private BigDecimal dropPrice;
	private BigDecimal latestPrice;  //当日收盘价
	private Integer limited;        //当日是否一字板
	
	public Integer getLimited() {
		return limited;
	}

	public void setLimited(Integer limited) {
		this.limited = limited;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

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
		return limited==1 || latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(0.095))>=0;
		//return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(0.095))>=0;
	}

	public boolean isDownLimited() {
		return limited==1 || latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(-0.095))<=0;
		//return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(-0.095))<=0;
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
		return "Muster [itemID=" + itemID + ", itemName=" + itemName + ", industry=" + industry + ", amount=" + amount
				+ ", averageAmount=" + averageAmount + ", highest=" + highest + ", lowest=" + lowest + ", close="
				+ close + ", dropPrice=" + dropPrice + ", latestPrice=" + latestPrice + "]";
	}
	
	
}
