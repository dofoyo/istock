package com.rhb.istock.fdata.api;

import java.util.ArrayList;
import java.util.List;

public class FdatasView {
	private String nameCode;
	private List<String> years;
	private List<String> revenues;
	private List<String> profits;
	private List<String> cashes;
	
	public FdatasView(String code, String name, String industry) {
		this.nameCode = name + "(" + code + "," + industry + ")";
		years = new ArrayList<String>();
		revenues = new ArrayList<String>();
		profits = new ArrayList<String>();
		cashes = new ArrayList<String>();
	}
	
	public void add(String year, String revenue, String profit, String cash) {
		years.add(year);
		revenues.add(revenue);
		profits.add(profit);
		cashes.add(cash);
	}

	public String getNameCode() {
		return nameCode;
	}

	public void setNameCode(String nameCode) {
		this.nameCode = nameCode;
	}

	public List<String> getYears() {
		return years;
	}

	public void setYears(List<String> years) {
		this.years = years;
	}

	public List<String> getRevenues() {
		return revenues;
	}

	public void setRevenues(List<String> revenues) {
		this.revenues = revenues;
	}

	public List<String> getProfits() {
		return profits;
	}

	public void setProfits(List<String> profits) {
		this.profits = profits;
	}

	public List<String> getCashes() {
		return cashes;
	}

	public void setCashes(List<String> cashes) {
		this.cashes = cashes;
	}
	
	
}
