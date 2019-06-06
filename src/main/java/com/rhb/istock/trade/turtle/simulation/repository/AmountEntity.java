package com.rhb.istock.trade.turtle.simulation.repository;

public class AmountEntity {
	private String cash;
	private String value;
	
	public AmountEntity(String cash, String value) {
		super();
		this.cash = cash;
		this.value = value;
	}
	
	public String getCash() {
		return cash;
	}
	public void setCash(String cash) {
		this.cash = cash;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AmountEntity [cash=" + cash + ", value=" + value + "]";
	}
	
	
}
