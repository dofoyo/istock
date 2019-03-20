package com.rhb.istock.selector.bluechip.api;

public class BluechipView {
	private String code;
	private String name;
	private Integer upProbability = -1;
	private String okYears;
	private String date;
	private Integer aboveAv60Days;
	private Integer biasOfAv120;
	private Integer biasOfMidPrice;
	private String ipoDate;
	
	
	
	public String getIpoDate() {
		return ipoDate;
	}
	public void setIpoDate(String ipoDate) {
		this.ipoDate = ipoDate;
	}
	public Integer getAboveAv60Days() {
		return aboveAv60Days;
	}
	public void setAboveAv60Days(Integer aboveAv60Days) {
		this.aboveAv60Days = aboveAv60Days;
	}
	public Integer getBiasOfAv120() {
		return biasOfAv120;
	}
	public void setBiasOfAv120(Integer biasOfAv120) {
		this.biasOfAv120 = biasOfAv120;
	}
	public Integer getBiasOfMidPrice() {
		return biasOfMidPrice;
	}
	public void setBiasOfMidPrice(Integer biasOfMidPrice) {
		this.biasOfMidPrice = biasOfMidPrice;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Integer getUpProbability() {
		return upProbability;
	}
	public void setUpProbability(Integer upProbability) {
		this.upProbability = upProbability;
	}
	public String getOkYears() {
		return okYears.substring(0,okYears.length()-1); //去除逗号
	}
	public void setOkYears(String okYears) {
		this.okYears = okYears;
	}
	@Override
	public String toString() {
		return "BluechipView [code=" + code + ", name=" + name + ", upProbability=" + upProbability + ", okYears="
				+ okYears + ", date=" + date + ", aboveAv120Days=" + aboveAv60Days + ", biasOfAv120=" + biasOfAv120
				+ ", biasOfMidPrice=" + biasOfMidPrice + "]";
	}

	


	
	
	
}
