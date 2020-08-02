package com.rhb.istock.index.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * 
trade_date	str	交易日
close	float	收盘点位
open	float	开盘点位
high	float	最高点位
low	float	最低点位
pre_close	float	昨日收盘点
change	float	涨跌点
pct_chg	float	涨跌幅（%）
vol	float	成交量（手）
amount	float	成交额（千元）
 */
public class IndexBar {

	//"trade_date","close","open","high","low","pre_close","change","pct_chg","vol","amount"
	private BigDecimal close;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal pre_close;
	private BigDecimal change;
	private BigDecimal pct_chg;
	private BigDecimal vol;
	private BigDecimal amount;
	
	public IndexBar(BigDecimal close, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal pre_close, BigDecimal change, BigDecimal pct_chg, BigDecimal vol, BigDecimal amount) {
		super();
		this.close = close;
		this.open = open;
		this.high = high;
		this.low = low;
		this.pre_close = pre_close;
		this.change = change;
		this.pct_chg = pct_chg;
		this.vol = vol;
		this.amount = amount;
	}
	
	public IndexBar(String close, String open, String high, String low,
			String pre_close, String change, String pct_chg, String vol, String amount) {
		super();
		this.close = new BigDecimal(close);
		this.open = new BigDecimal(open);
		this.high = new BigDecimal(high);
		this.low = new BigDecimal(low);
		this.pre_close = new BigDecimal(pre_close);
		this.change = new BigDecimal(change);
		this.pct_chg = new BigDecimal(pct_chg);
		this.vol = new BigDecimal(vol);
		this.amount = new BigDecimal(amount);
	}
	
	public BigDecimal getClose() {
		return close;
	}
	public void setClose(BigDecimal close) {
		this.close = close;
	}
	public BigDecimal getOpen() {
		return open;
	}
	public void setOpen(BigDecimal open) {
		this.open = open;
	}
	public BigDecimal getHigh() {
		return high;
	}
	public void setHigh(BigDecimal high) {
		this.high = high;
	}
	public BigDecimal getLow() {
		return low;
	}
	public void setLow(BigDecimal low) {
		this.low = low;
	}
	public BigDecimal getPre_close() {
		return pre_close;
	}
	public void setPre_close(BigDecimal pre_close) {
		this.pre_close = pre_close;
	}
	public BigDecimal getChange() {
		return change;
	}
	public void setChange(BigDecimal change) {
		this.change = change;
	}
	public BigDecimal getPct_chg() {
		return pct_chg;
	}
	public void setPct_chg(BigDecimal pct_chg) {
		this.pct_chg = pct_chg;
	}
	public BigDecimal getVol() {
		return vol;
	}
	public void setVol(BigDecimal vol) {
		this.vol = vol;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	@Override
	public String toString() {
		return "IndexBar [close=" + close + ", open=" + open + ", high=" + high
				+ ", low=" + low + ", pre_close=" + pre_close + ", change=" + change + ", pct_chg=" + pct_chg + ", vol="
				+ vol + ", amount=" + amount + "]";
	}

}
