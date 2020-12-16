package com.rhb.istock.evaluation;

import java.time.LocalDate;

public class KellyView {
	private LocalDate date;
	private Integer win;
	private Integer winPR;
	private Integer winRate;
	private Integer lose;
	private Integer losePR;
	private Integer loseRate;
	private Integer score;
	
	public KellyView(LocalDate date, Integer win, Integer winPR, Integer winRate, Integer lose, Integer losePR, Integer loseRate, Integer score) {
		super();
		this.date = date;
		this.win = win;
		this.winPR = winPR;
		this.winRate = winRate;
		this.lose = lose;
		this.losePR = losePR;
		this.loseRate = loseRate;
		this.score = score;
	}
	
	public Integer getWin() {
		return win;
	}

	public Integer getLose() {
		return lose;
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

	public String getColor() {
		return this.score>0 ? "red" : "green";
	}
	@Override
	public String toString() {
		return "KellyView [date=" + date + ", winPR=" + winPR + ", winRate=" + winRate + ", losePR=" + losePR
				+ ", loseRate=" + loseRate + ", score=" + score + "]";
	}

	public Integer getScore() {
		return score;
	}
}
