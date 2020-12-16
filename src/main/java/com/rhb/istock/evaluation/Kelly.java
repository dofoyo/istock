package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rhb.istock.comm.util.Functions;
/*
 * 
凯利公式有几种形式，其中的一种如下：

f=p/a-q/b

其中：f表示分配的资金比例

p表示获胜的概率

q表示失败的概率

a表示失败损失率，指失败后押注的资金从1变成1-a

b表示获胜增长率，指获胜后押注的资金从1变成1+b

如果f算出来是0，表示这是一个期望收益为0的游戏，最优决策是不参加。

如果f算出来是负数，表示这是一个期望收益为负的游戏，更是不能参加了。

如果f算出来是小于1的正数，就应该按照这个比例下注；如果是个大于1的数，最优的决策是需要借钱来参与这个游戏。
 */
public class Kelly {
	private LocalDate date;

	private Integer win;
	private BigDecimal winOpen;
	private BigDecimal winClose;
	
	private Integer lose;
	private BigDecimal loseOpen;
	private BigDecimal loseClose;
	
	public Kelly(LocalDate date) {
		super();
		this.date = date;
		
		this.win = 0;
		this.winOpen = BigDecimal.ZERO;
		this.winClose = BigDecimal.ZERO;
		
		this.lose = 0;
		this.loseOpen = BigDecimal.ZERO;
		this.loseClose = BigDecimal.ZERO;

	}

	public void add(Integer count, BigDecimal open, BigDecimal close) {
		if(close.compareTo(open) == 1) {
			win = win + count;
			winOpen = winOpen.add(open);
			winClose = winClose.add(close);
		}else {
			lose = lose + count;
			loseOpen = loseOpen.add(open);
			loseClose = loseClose.add(close);
		}
	}
	
	public void subtract(Integer count, BigDecimal open, BigDecimal close) {
		if(close.compareTo(open) == 1) {
			win = win - count;
			winOpen = winOpen.subtract(open);
			winClose = winClose.subtract(close);
		}else {
			lose = lose - count;
			loseOpen = loseOpen.subtract(open);
			loseClose = loseClose.subtract(close);
		}
	}
	
	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Integer getWinPR() {
		return (win+lose)!=0 ? win*100/(win+lose) : 0;
	}
	
	public Integer getWinRate() {
		return Functions.growthRate(winClose, winOpen);
	}
	
	public Integer getLosePR() {
		return (win+lose)!=0 ? lose*100/(win+lose) : 0;
	}
	
	public Integer getLoseRate() {
		return Functions.growthRate(loseOpen, loseClose);
	}
	
	public Integer getScore() {
		Integer winPR = this.getWinPR();
		Integer winRate = this.getWinRate();
		Integer losePR = this.getLosePR();
		Integer loseRate = this.getLoseRate();
		
		Integer score = (loseRate!=0 && winRate!=0) ? 100*winPR/loseRate/100 - 100*losePR/winRate/100 : 0;
		if(loseRate==0 && winRate!=0) {
			score = winRate;
		}else if(loseRate!=0 && winRate==0) {
			score = -loseRate;
		}
		return score;
	}

	public LocalDate getDate() {
		return date;
	}

	public Integer getWin() {
		return win;
	}

	public BigDecimal getWinOpen() {
		return winOpen;
	}

	public BigDecimal getWinClose() {
		return winClose;
	}

	public Integer getLose() {
		return lose;
	}

	public BigDecimal getLoseOpen() {
		return loseOpen;
	}

	public BigDecimal getLoseClose() {
		return loseClose;
	}

	@Override
	public String toString() {
		return "Kelly [date=" + date + ", win=" + win + ", winOpen=" + winOpen + ", winClose=" + winClose + ", lose="
				+ lose + ", loseOpen=" + loseOpen + ", loseClose=" + loseClose + ", getWinPR()=" + getWinPR()
				+ ", getWinRate()=" + getWinRate() + ", getLosePR()=" + getLosePR() + ", getLoseRate()=" + getLoseRate()
				+ ", getScore()=" + getScore() + "]";
	}
	
	
}
