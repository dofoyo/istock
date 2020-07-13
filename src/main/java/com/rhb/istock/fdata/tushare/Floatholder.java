package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class Floatholder {
	private String ts_code;			//	str	TS股票代码
	private String ann_date;		//	str	公告日期
	private String end_date;		//	str	报告期
	private String holder_name;		//	str	股东名称
	private String hold_amount;		//	float	持有数量（股）
	
	public Floatholder(JSONArray item) {
		this.ts_code = item.get(0).toString();
		this.ann_date  = item.get(1).toString();
		this.end_date  = item.get(2).toString();
		this.holder_name  = item.get(3).toString();
		this.hold_amount  = item.get(4).toString();
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
	public String getHolder_name() {
		return holder_name;
	}
	public void setHolder_name(String holder_name) {
		this.holder_name = holder_name;
	}
	public String getHold_amount() {
		return hold_amount;
	}
	public void setHold_amount(String hold_amount) {
		this.hold_amount = hold_amount;
	}


	@Override
	public String toString() {
		return "Floatholder [ts_code=" + ts_code + ", ann_date=" + ann_date + ", end_date=" + end_date
				+ ", holder_name=" + holder_name + ", hold_amount=" + hold_amount + "]";
	}
	
	
}
