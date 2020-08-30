package com.rhb.istock.fund;

import java.math.BigDecimal;

import com.rhb.istock.comm.util.Functions;

public class ItemPortfolio {
	private String symbol;	//str	Y	TS基金代码
	private String end_date;	//str	Y	截止日期
	private BigDecimal amount;	//float	Y	持有股票数量（股）
	private BigDecimal stk_mkv_ratio;	//float	Y	占股票市值比
	
	public String getItemID() {
		if(this.symbol.endsWith(".SZ")) {
			return "sz" + this.symbol.substring(0,6);
		}else {
			return "sh" + this.symbol.substring(0,6);
		}
	}
	
	public BigDecimal getStk_mkv_ratio() {
		return stk_mkv_ratio;
	}

	public void setStk_mkv_ratio(BigDecimal stk_mkv_ratio) {
		this.stk_mkv_ratio = stk_mkv_ratio;
	}

	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public void addAmount(BigDecimal amount) {
		this.amount = this.amount.add(amount);
	}
	public void addStk_mkv_ratio(BigDecimal stk_mkv_ratio) {
		this.stk_mkv_ratio = this.stk_mkv_ratio.add(stk_mkv_ratio);
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "ItemPortfolio [symbol=" + symbol + ", end_date=" + end_date + ", amount=" + amount + ", stk_mkv_ratio="
				+ stk_mkv_ratio + ", getItemID()=" + getItemID() + "]";
	}
	
}
