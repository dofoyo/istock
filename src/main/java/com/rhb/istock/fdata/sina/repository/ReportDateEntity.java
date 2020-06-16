package com.rhb.istock.fdata.sina.repository;

public class ReportDateEntity {
	private String stockCode;
	private Integer year;
	private String reportdate;
	
	
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
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
		return "ReportDateEntity [stockCode=" + stockCode + ", year=" + year + ", reportdate=" + reportdate + "]";
	}

}
