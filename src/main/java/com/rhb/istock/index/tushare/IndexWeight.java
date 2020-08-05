package com.rhb.istock.index.tushare;

public class IndexWeight {
	private String index_code; //	str	Y	指数代码
	private String con_code; //	str	Y	成分股票代码
	private String trade_date; //	str	Y	交易日期
	private String weight; //	str	Y	权重
	
	public IndexWeight(String index_code, String con_code, String trade_date, String weight) {
		super();
		this.index_code = index_code;
		this.con_code = con_code;
		this.trade_date = trade_date;
		this.weight = weight;
	}
	
	public String getItemID() {
		String code = this.con_code.substring(0,6);
		if(this.con_code.contains(".SH")) {
			return "sh" + code;
		}else {
			return "sz" + code;
		}
	}
	
	public String getIndex_code() {
		return index_code;
	}
	public void setIndex_code(String index_code) {
		this.index_code = index_code;
	}
	public String getCon_code() {
		return con_code;
	}
	public void setCon_code(String con_code) {
		this.con_code = con_code;
	}
	public String getTrade_date() {
		return trade_date;
	}
	public void setTrade_date(String trade_date) {
		this.trade_date = trade_date;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	@Override
	public String toString() {
		return "IndexWeight [index_code=" + index_code + ", con_code=" + con_code + ", trade_date=" + trade_date
				+ ", weight=" + weight + "]";
	}
	

	
}
