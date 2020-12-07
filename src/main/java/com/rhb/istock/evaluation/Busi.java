package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Busi {
	private LocalDate openDate;
	private BigDecimal openPrice;
	private BigDecimal highestPrice;
	private BigDecimal closePrice;
	public Busi(LocalDate openDate, BigDecimal openPrice,BigDecimal highestPrice, BigDecimal closePrice) {
		super();
		this.openDate = openDate;
		this.openPrice = openPrice;
		this.highestPrice = highestPrice;
		this.closePrice = closePrice;
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

}
