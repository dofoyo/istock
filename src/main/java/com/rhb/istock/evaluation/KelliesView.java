package com.rhb.istock.evaluation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KelliesView {
	private Integer min;
	private List<String> dates;
	private List<Integer> bhls;
	private List<Integer> bavs;
	private List<Integer> bdts;
	private List<Integer> hlbs;
	private List<Integer> avbs;
	private List<Integer> dtbs;

	public KelliesView() {
		min = 0;
		dates = new ArrayList<String>();
		bhls = new ArrayList<Integer>();
		bavs = new ArrayList<Integer>();
		bdts = new ArrayList<Integer>();
		hlbs = new ArrayList<Integer>();
		avbs = new ArrayList<Integer>();
		dtbs = new ArrayList<Integer>();
	}
	
	public void add(LocalDate date, Integer bhl, Integer bav, Integer bdt, Integer hlb, Integer avb, Integer dtb) {
		dates.add(date.toString());
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

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer a) {
		if(this.min==null || this.min==0) {
			this.min = a;
		}else if(a.compareTo(this.min)==-1) {
			this.min = a;
		}
	}

	public List<String> getDates() {
		return dates;
	}

	public List<Integer> getBhls() {
		return bhls;
	}

	public List<Integer> getBavs() {
		return bavs;
	}

	public List<Integer> getBdts() {
		return bdts;
	}

	public List<Integer> getHlbs() {
		return hlbs;
	}

	public List<Integer> getAvbs() {
		return avbs;
	}

	public List<Integer> getDtbs() {
		return dtbs;
	}

	@Override
	public String toString() {
		return "KelliesView [min=" + min + ", dates=" + dates + ", bhls=" + bhls + ", bavs=" + bavs + ", bdts=" + bdts
				+ ", hlbs=" + hlbs + ", avbs=" + avbs + ", dtbs=" + dtbs + "]";
	}
	
}
