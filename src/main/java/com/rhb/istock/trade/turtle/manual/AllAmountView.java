package com.rhb.istock.trade.turtle.manual;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AllAmountView {
	private BigDecimal min;
	private List<String> dates;
	private List<String> bhls;
	private List<String> bavs;
	private List<String> bdts;
	private List<String> hlbs;
	private List<String> avbs;
	private List<String> dtbs;
	DecimalFormat df = new DecimalFormat("#.00");

	public AllAmountView() {
		min = BigDecimal.ZERO;
		dates = new ArrayList<String>();
		bhls = new ArrayList<String>();
		bavs = new ArrayList<String>();
		bdts = new ArrayList<String>();
		hlbs = new ArrayList<String>();
		avbs = new ArrayList<String>();
		dtbs = new ArrayList<String>();
	}
	
	public void add(LocalDate date, BigDecimal bhl, BigDecimal bav, BigDecimal bdt, BigDecimal hlb, BigDecimal avb, BigDecimal dtb) {
		dates.add(date.toString());
		bhls.add(df.format(bhl));
		bavs.add(df.format(bav));
		bdts.add(df.format(bdt));
		hlbs.add(df.format(hlb));
		avbs.add(df.format(avb));
		dtbs.add(df.format(dtb));
		
		this.setMin(bhl);
		this.setMin(bav);
		this.setMin(bdt);
		this.setMin(hlb);
		this.setMin(avb);
		this.setMin(dtb);
		
	}

	public String getMin() {
		return df.format(min);
	}

	public void setMin(BigDecimal a) {
		if(this.min==null) {
			this.min = a;
		}else if(a.compareTo(this.min)==-1) {
			this.min = a;			
		}
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
	
}
