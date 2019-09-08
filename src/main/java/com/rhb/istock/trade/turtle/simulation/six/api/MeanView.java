package com.rhb.istock.trade.turtle.simulation.six.api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MeanView {
	private Integer min;

	private List<String> dates;
	private List<String> bhls;
	private List<String> bavs;
	private List<String> bdts;
	private List<String> hlbs;
	private List<String> avbs;
	private List<String> dtbs;
	DecimalFormat df = new DecimalFormat("#.00");

	public MeanView() {
		min = null;
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
		
		this.setMin(bhl);
		this.setMin(bav);
		this.setMin(bdt);
		this.setMin(hlb);
		this.setMin(avb);
		this.setMin(dtb);
		
	}
	

	public String getMin() {
		return min.toString();
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
		Integer a = Integer.parseInt(min);
		if(this.min==null) {
			this.min = a;
		}else if(a < this.min) {
			this.min = a;
		}
	}

}
