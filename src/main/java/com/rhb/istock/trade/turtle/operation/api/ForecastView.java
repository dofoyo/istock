package com.rhb.istock.trade.turtle.operation.api;

import java.util.Map;

import com.rhb.istock.selector.fina.QuarterCompare;

public class ForecastView extends ItemView {
	private String previous_netprofit_yoy;			//上一季度归属母公司股东的净利润同比增长率(%)  -- 与业绩预告对应
	private String previous_dt_netprofit_yoy; 		//上一季度归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private String previous_or_yoy; 				//上一季度营业收入同比增长率(%)
	private String forecast_date;					//新季度业绩预告发布日期，没有的话，表示还没有发布，适时买入
	private String forecast_netprofit_yoy_max;		//新季度业绩预告结果：业绩变动百分比， 没有的话，表示还没有发布，适时买入
	private String forecast_price_up_max;			//新季度业绩预告当天：股价变动百分比
	private String indicator_date;					//新季度业绩报告发布日期，没有的话，表示还没有发布，适时买入
	private String indicator_netprofit_yoy;			//新季度归属母公司股东的净利润同比增长率(%)  -- 与业绩预告对应
	private String indicator_dt_netprofit_yoy; 		//新季度归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private String indicator_price_up_max;			//新季度业绩报告当天：股价变动百分比
	
	public ForecastView(Map<String, String> map) {
		super(map);
	}
	
	public void setAll(QuarterCompare qc) {
		this.previous_netprofit_yoy = qc.getPrevious_netprofit_yoy_s();
		this.previous_dt_netprofit_yoy = qc.getPrevious_dt_netprofit_yoy_s();
		this.previous_or_yoy = qc.getPrevious_or_yoy_s();
		this.forecast_date = qc.getForecast_date();
		this.forecast_netprofit_yoy_max = qc.getForecast_netprofit_yoy_max_s();
		this.forecast_price_up_max = qc.getForecast_price_up_max_s();
		this.indicator_date = qc.getIndicator_date();
		this.indicator_netprofit_yoy = qc.getIndicator_netprofit_yoy_s();
		this.indicator_dt_netprofit_yoy = qc.getIndicator_dt_netprofit_yoy_s();
		this.indicator_price_up_max = qc.getIndicator_price_up_max_s();
	}

	public String getPrevious_or_yoy() {
		return previous_or_yoy;
	}

	public void setPrevious_or_yoy(String previous_or_yoy) {
		this.previous_or_yoy = previous_or_yoy;
	}

	public String getPrevious_netprofit_yoy() {
		return previous_netprofit_yoy;
	}

	public void setPrevious_netprofit_yoy(String previous_netprofit_yoy) {
		this.previous_netprofit_yoy = previous_netprofit_yoy;
	}

	public String getPrevious_dt_netprofit_yoy() {
		return previous_dt_netprofit_yoy;
	}

	public void setPrevious_dt_netprofit_yoy(String previous_dt_netprofit_yoy) {
		this.previous_dt_netprofit_yoy = previous_dt_netprofit_yoy;
	}

	public String getForecast_date() {
		return forecast_date;
	}

	public void setForecast_date(String forecast_date) {
		this.forecast_date = forecast_date;
	}

	public String getForecast_netprofit_yoy_max() {
		return forecast_netprofit_yoy_max;
	}

	public void setForecast_netprofit_yoy_max(String forecast_netprofit_yoy_max) {
		this.forecast_netprofit_yoy_max = forecast_netprofit_yoy_max;
	}

	public String getForecast_price_up_max() {
		return forecast_price_up_max;
	}

	public void setForecast_price_up_max(String forecast_price_up_max) {
		this.forecast_price_up_max = forecast_price_up_max;
	}

	public String getIndicator_date() {
		return indicator_date;
	}

	public void setIndicator_date(String indicator_date) {
		this.indicator_date = indicator_date;
	}

	public String getIndicator_netprofit_yoy() {
		return indicator_netprofit_yoy;
	}

	public void setIndicator_netprofit_yoy(String indicator_netprofit_yoy) {
		this.indicator_netprofit_yoy = indicator_netprofit_yoy;
	}

	public String getIndicator_dt_netprofit_yoy() {
		return indicator_dt_netprofit_yoy;
	}

	public void setIndicator_dt_netprofit_yoy(String indicator_dt_netprofit_yoy) {
		this.indicator_dt_netprofit_yoy = indicator_dt_netprofit_yoy;
	}

	public String getIndicator_price_up_max() {
		return indicator_price_up_max;
	}

	public void setIndicator_price_up_max(String indicator_price_up_max) {
		this.indicator_price_up_max = indicator_price_up_max;
	}

	@Override
	public String toString() {
		return "ForecastView [previous_netprofit_yoy=" + previous_netprofit_yoy + ", previous_dt_netprofit_yoy="
				+ previous_dt_netprofit_yoy + ", previous_or_yoy=" + previous_or_yoy + ", forecast_date="
				+ forecast_date + ", forecast_netprofit_yoy_max=" + forecast_netprofit_yoy_max
				+ ", forecast_price_up_max=" + forecast_price_up_max + ", indicator_date=" + indicator_date
				+ ", indicator_netprofit_yoy=" + indicator_netprofit_yoy + ", indicator_dt_netprofit_yoy="
				+ indicator_dt_netprofit_yoy + ", indicator_price_up_max=" + indicator_price_up_max + "]";
	}
	
	

}
