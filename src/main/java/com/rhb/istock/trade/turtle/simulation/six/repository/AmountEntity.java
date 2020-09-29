package com.rhb.istock.trade.turtle.simulation.six.repository;

import java.math.BigDecimal;

public class AmountEntity {
	private BigDecimal cash;
	private BigDecimal value;

	public AmountEntity(String cash, String value) {
		super();
		this.cash = new BigDecimal(cash);
		this.value = new BigDecimal(value);
	}
	
	public BigDecimal getTotal() {
		return cash.add(value);
	}

	public BigDecimal getCash() {
		return cash;
	}

	public void setCash(BigDecimal cash) {
		this.cash = cash;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
	
	
}
