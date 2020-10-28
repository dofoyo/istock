package com.rhb.istock.fdata.api;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsView {
	private String nameCode;
	private List<String> dates;
	private List<String> recommendations;
	
	public RecommendationsView(String code, String name, String industry) {
		this.nameCode = name + "(" + code + "," + industry + ")";
		dates = new ArrayList<String>();
		recommendations = new ArrayList<String>();
	}
	
	public void add(String date, String recommendation) {
		dates.add(date);
		recommendations.add(recommendation);
	}

	public String getNameCode() {
		return nameCode;
	}

	public List<String> getDates() {
		return dates;
	}

	public List<String> getRecommendations() {
		return recommendations;
	}

	
}
