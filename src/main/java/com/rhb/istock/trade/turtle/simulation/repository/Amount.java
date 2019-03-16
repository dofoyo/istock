package com.rhb.istock.trade.turtle.simulation.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Amount  implements Comparable<Amount>{
	@Override
	public String toString() {
		return "BarEntity [date=" + date + ", code=" + code + ", amount=" + amount + "]";
	}

	private LocalDate date;
	private String code;
	private BigDecimal amount = new BigDecimal(0);
	
	public Amount(String code) {
		this.code = code;
	}
	
	public Amount(LocalDate date,String code, BigDecimal amount) {
		this.date = date;
		this.code = code;
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getAmount() {
		return amount;
	}
	
	public Integer getAmountInt() {
		return amount.divide(new BigDecimal(100000),BigDecimal.ROUND_HALF_UP).intValue();
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	@Override
	public int compareTo(Amount o) {
		return o.getAmount().compareTo(this.getAmount());
	}
	
}
