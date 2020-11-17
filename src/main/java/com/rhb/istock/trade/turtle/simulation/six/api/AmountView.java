package com.rhb.istock.trade.turtle.simulation.six.api;

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
	
	public Integer getRatio() {
		//Integer a = 10000;
		Integer b = (this.getTotal()-1000000)/10000;
		//System.out.println(b);
		return b;
	}
	
	public String getColor() {
		return this.getRatio()>0 ? "red" : "green";
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
