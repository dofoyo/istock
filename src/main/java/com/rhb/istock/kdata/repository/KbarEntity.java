package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;

public class KbarEntity {
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal amount;
	private BigDecimal quantity;

	public KbarEntity(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal amount,BigDecimal quantity) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.amount = amount;
		this.quantity = quantity;
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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "KbarEntity [open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + ", amount="
				+ amount + ", quantity=" + quantity + "]";
	}
	
}
