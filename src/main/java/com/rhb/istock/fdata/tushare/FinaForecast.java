package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class FinaForecast {
	private String ts_code;			//str	TS股票代码
	private String ann_date;		//str	公告日期
	private String end_date;		//str	报告期
	private String type;			//str	业绩预告类型(预增/预减/扭亏/首亏/续亏/续盈/略增/略减)
	private BigDecimal p_change_min;	//float	预告净利润变动幅度下限（%）
	private BigDecimal p_change_max;	//float	预告净利润变动幅度上限（%）
	private BigDecimal net_profit_min;	//float	预告净利润下限（万元）
	private BigDecimal net_profit_max;	//float	预告净利润上限（万元）
	private BigDecimal last_parent_net;	//float	上年同期归属母公司净利润
	private String first_ann_date;	//str	首次公告日
	private String summary;			//str	业绩预告摘要
	private String change_reason;	//str	业绩变动原因
	
	public FinaForecast(JSONArray item) {
		this.ts_code  = item.getString(0);
		this.ann_date  = item.getString(1);
		this.end_date  = item.getString(2);
		this.type  = item.getString(3);
		this.p_change_min  = item.get(4).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(4);
		this.p_change_max  = item.get(5).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(5);
		this.net_profit_min  = item.get(6).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(6);
		this.net_profit_max  = item.get(7).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(7);
		this.last_parent_net  = item.get(8).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(8);
		this.first_ann_date  = item.get(9).toString();
		this.summary  = item.get(10).toString();
		this.change_reason  = item.get(11).toString();
		
	}
	
	public String getInfo() {
		return end_date+"净利润预计" +  p_change_min.intValue() + "～" +  p_change_max.intValue();
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public BigDecimal getP_change_min() {
		return p_change_min;
	}
	public void setP_change_min(BigDecimal p_change_min) {
		this.p_change_min = p_change_min;
	}
	public BigDecimal getP_change_max() {
		return p_change_max;
	}
	public void setP_change_max(BigDecimal p_change_max) {
		this.p_change_max = p_change_max;
	}
	public BigDecimal getNet_profit_min() {
		return net_profit_min;
	}
	public void setNet_profit_min(BigDecimal net_profit_min) {
		this.net_profit_min = net_profit_min;
	}
	public BigDecimal getNet_profit_max() {
		return net_profit_max;
	}
	public void setNet_profit_max(BigDecimal net_profit_max) {
		this.net_profit_max = net_profit_max;
	}
	public BigDecimal getLast_parent_net() {
		return last_parent_net;
	}
	public void setLast_parent_net(BigDecimal last_parent_net) {
		this.last_parent_net = last_parent_net;
	}
	public String getFirst_ann_date() {
		return first_ann_date;
	}
	public void setFirst_ann_date(String first_ann_date) {
		this.first_ann_date = first_ann_date;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getChange_reason() {
		return change_reason;
	}
	public void setChange_reason(String change_reason) {
		this.change_reason = change_reason;
	}
	
	@Override
	public String toString() {
		return "FinaForecast [ts_code=" + ts_code + ", ann_date=" + ann_date + ", end_date=" + end_date + ", type="
				+ type + ", p_change_min=" + p_change_min + ", p_change_max=" + p_change_max + ", net_profit_min="
				+ net_profit_min + ", net_profit_max=" + net_profit_max + ", last_parent_net=" + last_parent_net
				+ ", first_ann_date=" + first_ann_date + ", summary=" + summary + ", change_reason=" + change_reason
				+ "]";
	}
}
