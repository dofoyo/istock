package com.rhb.istock.fdata;

public class OkfinanceStatementDto {
	private String stockcode;
	private Integer year;
	private String reportdate;
	public String getStockcode() {
		return stockcode;
	}
	public void setStockcode(String stockcode) {
		this.stockcode = stockcode;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getReportdate() {
		return reportdate;
	}
	public void setReportdate(String reportdate) {
		this.reportdate = reportdate;
	}
	@Override
	public String toString() {
		return "OkfinanceStatementDto [stockcode=" + stockcode + ", year=" + year + ", reportdate=" + reportdate + "]";
	}
	
	
	
}
