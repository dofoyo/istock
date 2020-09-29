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
		Integer b = Functions.growthRate(this.getTotal(), this.init);
		return b;
	}
	
	public String getColor() {
		return this.getRatio()>0 ? "red" : "green";
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

	public BigDecimal getInit() {
		return init;
	}

	public void setInit(BigDecimal init) {
		this.init = init;
	}

	public BigDecimal getTotal() {
		return cash.add(value);
	}

	@Override
	public String toString() {
		return "AmountView [cash=" + cash + ", value=" + value + ", getRatio()=" + getRatio() + ", getColor()="
				+ getColor() + ", getTotal()=" + getTotal() + "]";
	}
	
}
