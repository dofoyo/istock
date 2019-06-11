package com.rhb.istock.trade.turtle.simulation.api;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AllAmountView {
	private String min;
	private List<String> dates;
	private List<String> bhls;
	private List<String> bavs;
	private List<String> bdts;
	DecimalFormat df = new DecimalFormat("#.00");

	public AllAmountView() {
		dates = new ArrayList<String>();
		bhls = new ArrayList<String>();
		bavs = new ArrayList<String>();
		bdts = new ArrayList<String>();
	}
	
	public void add(String date, String bhl, String bav, String bdt) {
		dates.add(date);
		bhls.add(bhl);
		bavs.add(bav);
		bdts.add(bdt);
	}
	
	public void add(LocalDate date, BigDecimal bhl, BigDecimal bav, BigDecimal bdt) {
		dates.add(date.toString());
		bhls.add(df.format(bhl));
		bavs.add(df.format(bav));
		bdts.add(df.format(bdt));
	}

	public String getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = df.format(min);
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}

	public List<String> getBhls() {
		return bhls;
	}

	public void setBhls(List<String> bhls) {
		this.bhls = bhls;
	}

	public List<String> getBavs() {
		return bavs;
	}

	public void setBavs(List<String> bavs) {
		this.bavs = bavs;
	}

	public List<String> getBdts() {
		return bdts;
	}

	public void setBdts(List<String> bdts) {
		this.bdts = bdts;
	}

	
	
}
