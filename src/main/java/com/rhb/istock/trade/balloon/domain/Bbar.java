package com.rhb.istock.trade.balloon.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Bbar  {
	private LocalDate date;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal upLinePrice;
	private BigDecimal baseLinePrice;
	private BigDecimal midPrice;
	
	public Bbar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}
	
	public boolean isYzb() {
		return high.compareTo(low)==0;
	}
	
	public boolean isAboveBaseLine() {
		return close.compareTo(baseLinePrice)==1;
	}
	
	public boolean isAboveUpLine() {
		return (close.setScale(2, BigDecimal.ROUND_HALF_UP)).compareTo(upLinePrice.setScale(2, BigDecimal.ROUND_HALF_UP))==1;
	}
	
	public boolean isSlip() {
		return close.compareTo(upLinePrice)==-1;
	}
	
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public BigDecimal getOpen() {
		return open;
	}
	public void setOpen(BigDecimal open) {
		this.open = open;
	}
	public BigDecimal getHigh() {
		return high;
	}
	public void setHigh(BigDecimal high) {
		this.high = high;
	}
	public BigDecimal getLow() {
		return low;
	}
	public void setLow(BigDecimal low) {
		this.low = low;
	}
	public BigDecimal getClose() {
		return close;
	}
	public void setClose(BigDecimal close) {
		this.close = close;
	}
	
	public BigDecimal getUpLinePrice() {
		return upLinePrice;
	}

	public void setUpLinePrice(BigDecimal upLinePrice) {
		this.upLinePrice = upLinePrice;
	}

	public BigDecimal getBaseLinePrice() {
		return baseLinePrice;
	}

	public void setBaseLinePrice(BigDecimal baseLinePrice) {
		this.baseLinePrice = baseLinePrice;
	}

	public BigDecimal getGoldenPrice() {
		return midPrice;
	}

	public void setMidPrice(BigDecimal midPrice) {
		this.midPrice = midPrice;
	}

	@Override
	public String toString() {
		return "Bbar [date=" + date + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
				+ ", upLinePrice=" + upLinePrice + ", baseLinePrice=" + baseLinePrice + ", midPrice=" + midPrice + "]";
	}
	
	
}
