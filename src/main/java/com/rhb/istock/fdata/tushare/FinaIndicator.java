package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class FinaIndicator {
	private String ann_date;		//str	公告日期
	private String end_date;		//str	报告期
	private BigDecimal profit_dedt;
	private BigDecimal grossprofit_margin;
	private BigDecimal op_yoy;  			//营业利润同比增长率(%)
	private BigDecimal ebt_yoy;  			//利润总额同比增长率(%)
	private BigDecimal netprofit_yoy; 		//归属母公司股东的净利润同比增长率(%)  -- 业绩预告
	private BigDecimal dt_netprofit_yoy; 	//归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private BigDecimal or_yoy; 				//营业收入同比增长率(%)
	
	public boolean isValid() {
		return this.profit_dedt!=null && !this.profit_dedt.equals(BigDecimal.ZERO);
	}
	public FinaIndicator(JSONArray item) {
		this.ann_date = item.get(1).toString();
		this.end_date  = item.getString(2);
		this.profit_dedt  = item.get(11).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(11);
		this.grossprofit_margin  = item.get(49).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(49);
		this.op_yoy  = item.get(94).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(94);
		this.ebt_yoy  = item.get(95).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(95);
		this.netprofit_yoy  = item.get(96).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(96);
		this.dt_netprofit_yoy  = item.get(97).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(97);
		this.or_yoy  = item.get(104).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(104);
	}
	
	public String getInfo() {
		return end_date+"净利:" +  netprofit_yoy.intValue() + "、非经:" +  dt_netprofit_yoy.intValue() + "、营收:" + or_yoy.intValue() + "";
	}
	
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public BigDecimal getOr_yoy() {
		return or_yoy;
	}
	public void setOr_yoy(BigDecimal or_yoy) {
		this.or_yoy = or_yoy;
	}
	public BigDecimal getNetprofit_yoy() {
		return netprofit_yoy;
	}
	public void setNetprofit_yoy(BigDecimal netprofit_yoy) {
		this.netprofit_yoy = netprofit_yoy;
	}
	public BigDecimal getDt_netprofit_yoy() {
		return dt_netprofit_yoy;
	}
	public void setDt_netprofit_yoy(BigDecimal dt_netprofit_yoy) {
		this.dt_netprofit_yoy = dt_netprofit_yoy;
	}
	public String getAnn_date() {
		return ann_date;
	}
	public void setAnn_date(String ann_date) {
		this.ann_date = ann_date;
	}
	public BigDecimal getEbt_yoy() {
		return ebt_yoy;
	}
	public void setEbt_yoy(BigDecimal ebt_yoy) {
		this.ebt_yoy = ebt_yoy;
	}
	public BigDecimal getOp_yoy() {
		return op_yoy;
	}
	public void setOp_yoy(BigDecimal op_yoy) {
		this.op_yoy = op_yoy;
	}
	public BigDecimal getProfit_dedt() {
		return profit_dedt;
	}
	public void setProfit_dedt(BigDecimal profit_dedt) {
		this.profit_dedt = profit_dedt;
	}
	public BigDecimal getGrossprofit_margin() {
		return grossprofit_margin;
	}
	public void setGrossprofit_margin(BigDecimal grossprofit_margin) {
		this.grossprofit_margin = grossprofit_margin;
	}
	@Override
	public String toString() {
		return "FinaIndicator [ann_date=" + ann_date + ", profit_dedt=" + profit_dedt + ", grossprofit_margin="
				+ grossprofit_margin + ", op_yoy=" + op_yoy + ", ebt_yoy=" + ebt_yoy + ", netprofit_yoy="
				+ netprofit_yoy + ", dt_netprofit_yoy=" + dt_netprofit_yoy + ", or_yoy=" + or_yoy + "]";
	}

}
