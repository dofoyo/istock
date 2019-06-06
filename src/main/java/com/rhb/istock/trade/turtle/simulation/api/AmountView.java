package com.rhb.istock.trade.turtle.simulation.api;

import java.math.BigDecimal;

public class AmountView {

	private Integer cash;
	private Integer value;
	
	public AmountView(Integer cash, Integer value) {
		this.cash = cash;
		this.value = value;
	}
	
	public AmountView(String cash, String value) {
		this.cash = (new BigDecimal(cash)).intValue();
		this.value = (new BigDecimal(value)).intValue();
	}
	
	public Integer getCash() {
		return cash;
	}
	public void setCash(Integer cash) {
		this.cash = cash;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	
	public Integer getTotal() {
		return cash+value;
	}

	@Override
	public String toString() {
		return "AmountView [cash=" + cash + ", value=" + value + "]";
	}
	
}
