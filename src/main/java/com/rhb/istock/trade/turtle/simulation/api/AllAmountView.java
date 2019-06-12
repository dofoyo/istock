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
	private List<String> hlbs;
	private List<String> avbs;
	private List<String> dtbs;
	DecimalFormat df = new DecimalFormat("#.00");

	public AllAmountView() {
		dates = new ArrayList<String>();
		bhls = new ArrayList<String>();
		bavs = new ArrayList<String>();
		bdts = new ArrayList<String>();
		hlbs = new ArrayList<String>();
		avbs = new ArrayList<String>();
		dtbs = new ArrayList<String>();
	}
	
	public void add(String date, String bhl, String bav, String bdt, String hlb, String avb, String dtb) {
		dates.add(date);
		bhls.add(bhl);
		bavs.add(bav);
		bdts.add(bdt);
		hlbs.add(hlb);
		avbs.add(avb);
		dtbs.add(dtb);
	}
	
	public void add(LocalDate date, BigDecimal bhl, BigDecimal bav, BigDecimal bdt, BigDecimal hlb, BigDecimal avb, BigDecimal dtb) {
		dates.add(date.toString());
		bhls.add(df.format(bhl));
		bavs.add(df.format(bav));
		bdts.add(df.format(bdt));
		hlbs.add(df.format(hlb));
		avbs.add(df.format(avb));
		dtbs.add(df.format(dtb));
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

	public List<String> getHlbs() {
		return hlbs;
	}

	public void setHlbs(List<String> hlbs) {
		this.hlbs = hlbs;
	}

	public List<String> getAvbs() {
		return avbs;
	}

	public void setAvbs(List<String> avbs) {
		this.avbs = avbs;
	}

	public List<String> getDtbs() {
		return dtbs;
	}

	public void setDtbs(List<String> dtbs) {
		this.dtbs = dtbs;
	}

	public void setMin(String min) {
		this.min = min;
	}

	
	
}
