package com.rhb.istock.trade.turtle.operation.api;

import java.util.ArrayList;
import java.util.List;

public class IndustryView {
	private String industry;
	private List<PotentialView> potentials;
	
	public IndustryView(String industry) {
		this.industry = industry;
		this.potentials = new ArrayList<PotentialView>();
	}
	
	public void addPotential(PotentialView view) {
		this.potentials.add(view);
	}
	
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public Integer getCount() {
		return this.potentials.size();
	}


}
