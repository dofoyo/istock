package com.rhb.istock.evaluation;

import java.time.LocalDate;

public class BusiView {
	private LocalDate date;
	private Integer winPR;
	private Integer winRate;
	private Integer losePR;
	private Integer loseRate;
	private Integer kelly;
	
	public BusiView(LocalDate date, Integer winPR, Integer winRate, Integer losePR, Integer loseRate, Integer kelly) {
		super();
		this.date = date;
		this.winPR = winPR;
		this.winRate = winRate;
		this.losePR = losePR;
		this.loseRate = loseRate;
		this.kelly = kelly;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public Integer getWinPR() {
		return winPR;
	}
	public Integer getWinRate() {
		return winRate;
	}
	public Integer getLosePR() {
		return losePR;
	}
	public Integer getLoseRate() {
		return loseRate;
	}
	public Integer getKelly() {
		return kelly;
	}
	public String getColor() {
		return this.kelly>0 ? "red" : "green";
	}
	@Override
	public String toString() {
		return "BusiView [date=" + date + ", winPR=" + winPR + ", winRate=" + winRate + ", losePR=" + losePR
				+ ", loseRate=" + loseRate + ", kelly=" + kelly + "]";
	}
}
