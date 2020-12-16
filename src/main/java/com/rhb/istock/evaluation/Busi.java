package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Busi {
	private String itemID;
	private LocalDate openDate;
	private BigDecimal openPrice;
	private BigDecimal quantity;
	private LocalDate closeDate;
	private BigDecimal closePrice;
	private BigDecimal highestPrice;
	
	public Busi(String itemID,LocalDate openDate, BigDecimal openPrice,BigDecimal quantity,LocalDate closeDate, BigDecimal closePrice,BigDecimal highestPrice) {
		super();
		this.itemID = itemID;
		this.openDate = openDate;
		this.openPrice = openPrice;
		this.quantity = quantity;
		this.closeDate = closeDate;
		this.closePrice = closePrice;
		this.highestPrice = highestPrice;
	}
	
	public LocalDate getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(LocalDate closeDate) {
		this.closeDate = closeDate;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
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
	public void setOpenDate(LocalDate openDate) {
		this.openDate = openDate;
	}
	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}
	public void setHighestPrice(BigDecimal highestPrice) {
		this.highestPrice = highestPrice;
	}
	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
}
