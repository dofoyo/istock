package com.rhb.istock.fund;

import java.math.BigDecimal;

public class FundPortfolio {
	private String ts_code;	//str	Y	TS基金代码
	private String ann_date;	//str	Y	公告日期
	private String end_date;	//str	Y	截止日期
	private String symbol;	//str	Y	股票代码
	private BigDecimal mkv;	//float	Y	持有股票市值(元)
	private BigDecimal amount;	//float	Y	持有股票数量（股）
	private BigDecimal stk_mkv_ratio;	//float	Y	占股票市值比
	private BigDecimal stk_float_ratio;	//float	Y	占流通股本比例
	
	public FundPortfolio(String ts_code, String ann_date, String end_date, String symbol, BigDecimal mkv,
			BigDecimal amount, BigDecimal stk_mkv_ratio, BigDecimal stk_float_ratio) {
		super();
		this.ts_code = ts_code;
		this.ann_date = ann_date;
		this.end_date = end_date;
		this.symbol = symbol;
		this.mkv = mkv;
		this.amount = amount;
		this.stk_mkv_ratio = stk_mkv_ratio;
		this.stk_float_ratio = stk_float_ratio;
	}
	public String getTs_code() {
		return ts_code;
	}
	public void setTs_code(String ts_code) {
		this.ts_code = ts_code;
	}
	public String getAnn_date() {
		return ann_date;
	}
	public void setAnn_date(String ann_date) {
		this.ann_date = ann_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public BigDecimal getMkv() {
		return mkv;
	}
	public void setMkv(BigDecimal mkv) {
		this.mkv = mkv;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getStk_mkv_ratio() {
		return stk_mkv_ratio;
	}
	public void setStk_mkv_ratio(BigDecimal stk_mkv_ratio) {
		this.stk_mkv_ratio = stk_mkv_ratio;
	}
	public BigDecimal getStk_float_ratio() {
		return stk_float_ratio;
	}
	public void setStk_float_ratio(BigDecimal stk_float_ratio) {
		this.stk_float_ratio = stk_float_ratio;
	}
	@Override
	public String toString() {
		return "FundPortfolio [ts_code=" + ts_code + ", ann_date=" + ann_date + ", end_date=" + end_date + ", symbol="
				+ symbol + ", mkv=" + mkv + ", amount=" + amount + ", stk_mkv_ratio=" + stk_mkv_ratio
				+ ", stk_float_ratio=" + stk_float_ratio + "]";
	}
	
	
	
}
