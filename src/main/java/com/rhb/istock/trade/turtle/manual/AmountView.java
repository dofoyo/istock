package com.rhb.istock.trade.turtle.manual;

import java.math.BigDecimal;

import com.rhb.istock.comm.util.Functions;

public class AmountView {

	private BigDecimal cash;
	private BigDecimal value;
	private BigDecimal init;
	
	public AmountView(BigDecimal cash, BigDecimal value, BigDecimal init) {
		this.cash = cash;
		this.value = value;
		this.init = init;
	}
	
	public Integer getRatio() {
		//Integer a = 10000;
		Integer b = Functions.growthRate(cash.add(value), this.init);
		return b;
	}
	
	public String getColor() {
		return this.getRatio()>0 ? "red" : "green";
	}
	
	public Integer getCash() {
		return cash.intValue();
	}

	public Integer getValue() {
		return value.intValue();
	}

	public Integer getInit() {
		return init.intValue();
	}

	public Integer getTotal() {
		return cash.add(value).intValue();
	}

	@Override
	public String toString() {
		return "AmountView [cash=" + cash + ", value=" + value + ", getRatio()=" + getRatio() + ", getColor()="
				+ getColor() + ", getTotal()=" + getTotal() + "]";
	}
	
}
