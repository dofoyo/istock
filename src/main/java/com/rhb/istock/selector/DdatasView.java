package com.rhb.istock.selector;

import java.util.ArrayList;
import java.util.List;

public class DdatasView {
	private String name;
	private List<String> dates;
	private List<String> ratios;
	private List<String> averages;
	
	public DdatasView(String name) {
		this.name = name;
		dates = new ArrayList<String>();
		ratios = new ArrayList<String>();
		averages = new ArrayList<String>();
	}
	
	public void add(String date, String ratio, String average) {
		dates.add(date);
		ratios.add(ratio);
		averages.add(average);
	}

	public String getName() {
		return name;
	}

	public List<String> getDates() {
		return dates;
	}

	public List<String> getRatios() {
		return ratios;
	}

	public List<String> getAverages() {
		return averages;
	}
}
