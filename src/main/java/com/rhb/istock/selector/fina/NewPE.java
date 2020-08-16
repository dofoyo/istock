package com.rhb.istock.selector.fina;

import java.math.BigDecimal;

public class NewPE {
	private String id;
	private BigDecimal rate;
	
	public NewPE(String id, BigDecimal rate) {
		this.id = id;
		this.rate = rate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	@Override
	public String toString() {
		return "NewPE [id=" + id + ", rate=" + rate + "]";
	}

}
