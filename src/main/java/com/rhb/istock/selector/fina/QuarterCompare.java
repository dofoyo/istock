package com.rhb.istock.selector.fina;

/*
 * 季度业绩报告对比
 * 如果上一季度业绩大幅提升，
 * 在新季度业绩预告前，可以适时买入，等业绩预告或报告出来后，高开卖出，赚取差价
 */
public class QuarterCompare {
	private String itemID;
	private Integer previous_netprofit_yoy;			//上一季度归属母公司股东的净利润同比增长率(%)  -- 与业绩预告对应
	private Integer previous_dt_netprofit_yoy; 		//上一季度归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private Integer previous_or_yoy; 				//上一季度营业收入同比增长率(%)
	private String forecast_date;					//新季度业绩预告发布日期，没有的话，表示还没有发布，适时买入
	private Integer forecast_netprofit_yoy_max;		//新季度业绩预告结果：业绩变动百分比， 没有的话，表示还没有发布，适时买入
	private Integer forecast_price_up_max;			//新季度业绩预告当天：股价变动百分比
	private String indicator_date;					//新季度业绩报告发布日期，没有的话，表示还没有发布，适时买入
	private Integer indicator_netprofit_yoy;		//新季度归属母公司股东的净利润同比增长率(%)  -- 与业绩预告对应
	private Integer indicator_dt_netprofit_yoy; 	//新季度归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private Integer indicator_price_up_max;			//新季度业绩报告当天：股价变动百分比
	
	public String getInfo() {
		return "净利润(非经)大幅增长" +  Integer.toString(previous_netprofit_yoy) + "(" +  Integer.toString(previous_dt_netprofit_yoy) + ")";
	}
	
	public QuarterCompare() {
		
	}
	
	public Integer getPrevious_or_yoy() {
		return previous_or_yoy;
	}
	
	public String getPrevious_or_yoy_s() {
		return previous_or_yoy==0 ? "" : previous_or_yoy.toString();
	}

	public void setPrevious_or_yoy(Integer previous_or_yoy) {
		this.previous_or_yoy = previous_or_yoy;
	}

	public Integer getIndicator_netprofit_yoy() {
		return indicator_netprofit_yoy;
	}
	public String getIndicator_netprofit_yoy_s() {
		return indicator_netprofit_yoy==0 ? "" : indicator_netprofit_yoy.toString();
	}
	public void setIndicator_netprofit_yoy(Integer indicator_netprofit_yoy) {
		this.indicator_netprofit_yoy = indicator_netprofit_yoy;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public Integer getPrevious_netprofit_yoy() {
		return previous_netprofit_yoy;
	}
	public String getPrevious_netprofit_yoy_s() {
		return previous_netprofit_yoy==0 ? "" : previous_netprofit_yoy.toString();
	}
	public void setPrevious_netprofit_yoy(Integer previous_netprofit_yoy) {
		this.previous_netprofit_yoy = previous_netprofit_yoy;
	}
	public Integer getPrevious_dt_netprofit_yoy() {
		return previous_dt_netprofit_yoy;
	}
	public String getPrevious_dt_netprofit_yoy_s() {
		return previous_dt_netprofit_yoy==0 ? "" : previous_dt_netprofit_yoy.toString();
	}
	public void setPrevious_dt_netprofit_yoy(Integer previous_dt_netprofit_yoy) {
		this.previous_dt_netprofit_yoy = previous_dt_netprofit_yoy;
	}
	public String getForecast_date() {
		return forecast_date;
	}
	public void setForecast_date(String forecast_date) {
		this.forecast_date = forecast_date;
	}
	public Integer getForecast_netprofit_yoy_max() {
		return forecast_netprofit_yoy_max;
	}
	public String getForecast_netprofit_yoy_max_s() {
		return forecast_netprofit_yoy_max==0 ? "" : forecast_netprofit_yoy_max.toString();
	}
	public void setForecast_netprofit_yoy_max(Integer forecast_netprofit_yoy_max) {
		this.forecast_netprofit_yoy_max = forecast_netprofit_yoy_max;
	}
	public Integer getForecast_price_up_max() {
		return forecast_price_up_max;
	}
	public String getForecast_price_up_max_s() {
		return forecast_price_up_max==0 ? "" : forecast_price_up_max.toString();
	}
	public void setForecast_price_up_max(Integer forecast_price_up_max) {
		this.forecast_price_up_max = forecast_price_up_max;
	}
	public String getIndicator_date() {
		return indicator_date;
	}
	public void setIndicator_date(String indicator_date) {
		this.indicator_date = indicator_date;
	}
	public Integer getNetprofit_yoy() {
		return indicator_netprofit_yoy;
	}
	public String getNetprofit_yoy_s() {
		return indicator_netprofit_yoy==0 ? "" : indicator_netprofit_yoy.toString();
	}
	public void setNetprofit_yoy(Integer indicator_netprofit_yoy) {
		this.indicator_netprofit_yoy = indicator_netprofit_yoy;
	}
	
	public Integer getIndicator_dt_netprofit_yoy() {
		return indicator_dt_netprofit_yoy;
	}
	public String getIndicator_dt_netprofit_yoy_s() {
		return indicator_dt_netprofit_yoy==0 ? "" : indicator_dt_netprofit_yoy.toString();
	}
	public void setIndicator_dt_netprofit_yoy(Integer indicator_dt_netprofit_yoy) {
		this.indicator_dt_netprofit_yoy = indicator_dt_netprofit_yoy;
	}
	public Integer getIndicator_price_up_max() {
		return indicator_price_up_max;
	}
	public String getIndicator_price_up_max_s() {
		return indicator_price_up_max==0 ? "" : indicator_price_up_max.toString();
	}
	public void setIndicator_price_up_max(Integer indicator_price_up_max) {
		this.indicator_price_up_max = indicator_price_up_max;
	}
	public String getTxt() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getItemID());
		sb.append(",");
		sb.append(this.getPrevious_netprofit_yoy());
		sb.append(",");
		sb.append(this.getPrevious_dt_netprofit_yoy());		
		sb.append(",");
		sb.append(this.getPrevious_or_yoy());		
		sb.append(",");
		sb.append(this.forecast_date==null ? "" : this.forecast_date);
		sb.append(",");
		sb.append(this.forecast_netprofit_yoy_max==null ? "" : this.forecast_netprofit_yoy_max);
		sb.append(",");
		sb.append(this.forecast_price_up_max==null ? "" : this.forecast_price_up_max);					
		sb.append(",");
		sb.append(this.indicator_date==null ? "" : this.indicator_date);
		sb.append(",");
		sb.append(this.indicator_netprofit_yoy==null ? "" : this.indicator_netprofit_yoy);
		sb.append(",");
		sb.append(this.indicator_dt_netprofit_yoy==null ? "" : this.indicator_dt_netprofit_yoy);
		sb.append(",");
		sb.append(this.indicator_price_up_max==null ? "" : this.indicator_price_up_max);					
		sb.append("\n");
		return sb.toString();
	}
	public QuarterCompare(String txt) {
		//System.out.println(txt);
		String[] ss = txt.split(",",-1);
		
		this.itemID = ss[0];
		this.previous_netprofit_yoy = ss[1].isEmpty()? 0 :Integer.parseInt(ss[1]);
		this.previous_dt_netprofit_yoy = ss[2].isEmpty()? 0 :Integer.parseInt(ss[2]);
		this.previous_or_yoy = ss[3].isEmpty()? 0 :Integer.parseInt(ss[3]);
		this.forecast_date = ss[4];
		this.forecast_netprofit_yoy_max = ss[5].isEmpty()? 0 :Integer.parseInt(ss[5]);
		this.forecast_price_up_max = ss[6].isEmpty()? 0 :Integer.parseInt(ss[6]);
		this.indicator_date = ss[7];
		this.indicator_netprofit_yoy = ss[8].isEmpty()? 0 :Integer.parseInt(ss[8]);
		this.indicator_dt_netprofit_yoy = ss[9].isEmpty()? 0 :Integer.parseInt(ss[9]);
		this.indicator_price_up_max = ss[10].isEmpty()? 0 :Integer.parseInt(ss[10]);
	}
}
