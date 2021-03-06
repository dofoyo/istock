package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

public class FinaIndicator {
	private String itemID;
	private String ann_date;		//str	公告日期
	private String end_date;		//str	报告期
	private BigDecimal profit_dedt;			//扣除非经常性损益后的净利润
	private BigDecimal grossprofit_margin;  //销售毛利率
	private BigDecimal op_yoy;  			//营业利润同比增长率(%)
	private BigDecimal ebt_yoy;  			//利润总额同比增长率(%)
	private BigDecimal netprofit_yoy; 		//归属母公司股东的净利润同比增长率(%)  -- 业绩预告
	private BigDecimal dt_netprofit_yoy; 	//归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private BigDecimal or_yoy; 				//营业收入同比增长率(%)
	private BigDecimal ocfps;				//每股经营活动产生的现金流量净额
	private BigDecimal roe;				//净资产收益率
	private BigDecimal debt_to_assets;    //资产负债率
	
	public boolean isValid() {
		return this.profit_dedt!=null && !this.profit_dedt.equals(BigDecimal.ZERO);
	}
	public FinaIndicator(String itemID,JSONArray item) {
		this.itemID = itemID;
		this.ann_date = item.get(1).toString();
		this.end_date  = item.getString(2);
		this.profit_dedt  = item.get(11).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(11);
		this.ocfps  = item.get(36).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(36);
		this.grossprofit_margin  = item.get(43).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(43);
		this.op_yoy  = item.get(94).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(94);
		this.ebt_yoy  = item.get(95).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(95);
		this.netprofit_yoy  = item.get(96).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(96);
		this.dt_netprofit_yoy  = item.get(97).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(97);
		this.or_yoy  = item.get(104).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(104);
		this.roe  = item.get(54).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(54);
		this.debt_to_assets  = item.get(62).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(62);
	}
	
	public boolean isOK() {
		return this.profit_dedt.compareTo(new BigDecimal(1000000000))==1   //扣除非经常性损益后的净利润
				//&& this.grossprofit_margin.compareTo(BigDecimal.ZERO)==1   //销售毛利率
				&& this.or_yoy.compareTo(new BigDecimal(13))==1    //营业收入同比增长率(%)
				&& this.dt_netprofit_yoy.compareTo(new BigDecimal(13))==1  //归属母公司股东的净利润-扣除非经常损益同比增长率(%)
				&& this.ocfps.compareTo(BigDecimal.ZERO)==1  //每股经营活动产生的现金流量净额
				&& this.roe.compareTo(new BigDecimal(13))==1  //净资产收益率
				//&& this.debt_to_assets.compareTo(new BigDecimal(55))==-1  //净资产收益率
				;
	}
	
	public boolean isGood() {
		return //this.profit_dedt.compareTo(new BigDecimal(100000000))==1   //扣除非经常性损益后的净利润
				//&& this.grossprofit_margin.compareTo(BigDecimal.ZERO)==1   //销售毛利率
				this.or_yoy.compareTo(BigDecimal.ZERO)==1    //营业收入同比增长率(%)
				&& this.dt_netprofit_yoy.compareTo(BigDecimal.ZERO)==1  //归属母公司股东的净利润-扣除非经常损益同比增长率(%)
				//&& this.ocfps.compareTo(BigDecimal.ZERO)==1  //每股经营活动产生的现金流量净额
				//&& this.roe.compareTo(new BigDecimal(13))==1  //净资产收益率
				;
	}
	
	public BigDecimal getDebt_to_assets() {
		return debt_to_assets;
	}
	public void setDebt_to_assets(BigDecimal debt_to_assets) {
		this.debt_to_assets = debt_to_assets;
	}
	public BigDecimal getOcfps() {
		return ocfps;
	}
	public void setOcfps(BigDecimal ocfps) {
		this.ocfps = ocfps;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
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
	
	public BigDecimal getRoe() {
		return roe;
	}
	public void setRoe(BigDecimal roe) {
		this.roe = roe;
	}
	@Override
	public String toString() {
		return "FinaIndicator [itemID=" + itemID + ", ann_date=" + ann_date + ", end_date=" + end_date
				+ ", profit_dedt=" + profit_dedt + ", grossprofit_margin=" + grossprofit_margin + ", op_yoy=" + op_yoy
				+ ", ebt_yoy=" + ebt_yoy + ", netprofit_yoy=" + netprofit_yoy + ", dt_netprofit_yoy=" + dt_netprofit_yoy
				+ ", or_yoy=" + or_yoy + ", ocfps=" + ocfps + ", roe=" + roe + ", debt_to_assets=" + debt_to_assets
				+ ", isValid()=" + isValid() + ", isOK()=" + isOK() + ", isGood()=" + isGood() + "]";
	}

}
