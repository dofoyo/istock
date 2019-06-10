package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Kbar {
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal quantity;
	private BigDecimal amount;
	private LocalDate date;

	public Kbar(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal amount, BigDecimal quantity,LocalDate date) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.amount = amount;
		this.quantity = quantity;
		this.date = date;
	}

	public Kbar(String open, String high, String low, String close, String amount, String quantity,String date) {
		this.open = new BigDecimal(open);
		this.high = new BigDecimal(high);
		this.low = new BigDecimal(low);
		this.close = new BigDecimal(close);
		this.amount = new BigDecimal(amount);
		this.quantity = new BigDecimal(quantity);
		if(date!=null) {
			if(date.indexOf("-")==-1) {
				this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
			}else {
				this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}
		}
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public boolean isLine() {
		return high.compareTo(low)==0;
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
		return "Kbar [open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + ", quantity=" + quantity
				+ ", amount=" + amount + "]";
	}

}
