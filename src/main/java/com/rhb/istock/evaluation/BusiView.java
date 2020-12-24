package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rhb.istock.comm.util.Functions;

public class BusiView {
	private String itemID;
	private String itemName;
	private LocalDate openDate;
	private BigDecimal openPrice;
	private BigDecimal quantity;
	private LocalDate closeDate;
	private BigDecimal closePrice;
	private BigDecimal highestPrice;
	
	public BusiView(String itemID, String itemName,LocalDate openDate, BigDecimal openPrice,BigDecimal quantity,LocalDate closeDate, BigDecimal closePrice,BigDecimal highestPrice) {
		super();
		this.itemID = itemID;
		this.itemName = itemName;
		this.openDate = openDate;
		this.openPrice = openPrice;
		this.quantity = quantity;
		this.closeDate = closeDate;
		this.closePrice = closePrice;
		this.highestPrice = highestPrice;
	}
	
	public String getItemName() {
		return itemName;
	}

	public LocalDate getCloseDate() {
		return closeDate;
	}

	public String getItemID() {
		return itemID;
	}

	public BigDecimal getOpenAmount() {
		return openPrice.multiply(quantity);
	}
	
	public BigDecimal getCloseAmount() {
		return closePrice.multiply(quantity);
	}
	
	public BigDecimal getHighestAmount() {
		return highestPrice.multiply(quantity);
	}
	
	public LocalDate getOpenDate() {
		return openDate;
	}
	public BigDecimal getOpenPrice() {
		return openPrice;
	}
	public BigDecimal getHighestPrice() {
		return highestPrice;
	}
	public BigDecimal getClosePrice() {
		return closePrice;
	}
	public boolean isWin() {
		return this.closePrice.compareTo(this.openPrice)>0;
	}
	public boolean isGood() {
		return this.highestPrice.compareTo(this.openPrice)>0;
	}
	public BigDecimal getQuantity() {
		return quantity;
	}
	
	public String getColor() {
		return this.isWin() ? "red" : "green";
	}
	
	public Integer getRate() {
		return Functions.growthRate(closePrice, openPrice);
	}
	
	public Integer getHighestRate() {
		return Functions.growthRate(highestPrice, openPrice);
	}

	@Override
	public String toString() {
		return "BusiView [itemID=" + itemID + ", itemName=" + itemName + ", openDate=" + openDate + ", openPrice="
				+ openPrice + ", quantity=" + quantity + ", closeDate=" + closeDate + ", closePrice=" + closePrice
				+ ", highestPrice=" + highestPrice + ", isWin()=" + isWin() + ", isGood()=" + isGood() + ", getColor()="
				+ getColor() + ", getRate()=" + getRate() + ", getHighestRate()=" + getHighestRate() + "]";
	}
	
	
}
