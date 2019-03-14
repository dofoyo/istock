package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Kbar {
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal quantity;
	private BigDecimal amount;

	public Kbar(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal amount, BigDecimal quantity) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.amount = amount;
		this.quantity = quantity;
	}

	public Kbar(String open, String high, String low, String close, String amount, String quantity) {
		this.open = new BigDecimal(open);
		this.high = new BigDecimal(high);
		this.low = new BigDecimal(low);
		this.close = new BigDecimal(close);
		this.amount = new BigDecimal(amount);
		this.quantity = new BigDecimal(quantity);
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

}
