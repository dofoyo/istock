package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Kbar {
	private String id;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal quantity;
	private BigDecimal amount;
	private LocalDate date;
	private BigDecimal turnover_rate_f;
	private BigDecimal volume_ratio;
	private BigDecimal total_mv;
	private BigDecimal circ_mv;
	private BigDecimal total_share;
	private BigDecimal float_share;
	private BigDecimal free_share;
	private BigDecimal pe;
	private BigDecimal pe_ttm;

	public Kbar(BigDecimal open, 
			BigDecimal high, 
			BigDecimal low, 
			BigDecimal close, 
			BigDecimal amount, 
			BigDecimal quantity,
			LocalDate date,
			BigDecimal turnover_rate_f,
			BigDecimal volume_ratio,
			BigDecimal total_mv,
			BigDecimal circ_mv,
			BigDecimal total_share,
			BigDecimal float_share,
			BigDecimal free_share,
			BigDecimal pe,
			BigDecimal pe_ttm
			) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.amount = amount;
		this.quantity = quantity;
		this.date = date;
		this.turnover_rate_f = turnover_rate_f;
		this.volume_ratio = volume_ratio;
		this.total_mv = total_mv;
		this.circ_mv = circ_mv;
		this.total_share = total_share;
		this.float_share = float_share;
		this.free_share = free_share;
		this.pe = pe;
		this.pe_ttm = pe_ttm;
	}

	public Kbar(String open, 
			String high, 
			String low, 
			String close, 
			String amount, 
			String quantity,
			String date,
			String turnover_rate_f,
			String volume_ratio,
			String total_mv,
			String circ_mv,
			String total_share,
			String float_share,
			String free_share,
			String pe,
			String pe_ttm
			) {
		this.open = new BigDecimal(open);
		this.high = new BigDecimal(high);
		this.low = new BigDecimal(low);
		this.close = new BigDecimal(close);
		this.amount = new BigDecimal(amount);
		this.quantity = new BigDecimal(quantity);
		if(date!=null) {
			if(date.indexOf("-")==-1) {
				this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
			}else {
				this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}
		}
		this.turnover_rate_f = new BigDecimal(turnover_rate_f);
		this.volume_ratio = new BigDecimal(volume_ratio);
		this.total_mv = new BigDecimal(total_mv);
		this.circ_mv = new BigDecimal(circ_mv);
		this.total_share = new BigDecimal(total_share);
		this.float_share = new BigDecimal(float_share);
		this.free_share = new BigDecimal(free_share);
		this.pe = new BigDecimal(pe);
		this.pe_ttm = new BigDecimal(pe_ttm);
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getPe_ttm() {
		return pe_ttm;
	}

	public void setPe_ttm(BigDecimal pe_ttm) {
		this.pe_ttm = pe_ttm;
	}

	public BigDecimal getPe() {
		return pe;
	}

	public void setPe(BigDecimal pe) {
		this.pe = pe;
	}

	public BigDecimal getTotal_share() {
		return total_share;
	}

	public void setTotal_share(BigDecimal total_share) {
		this.total_share = total_share;
	}

	public BigDecimal getFloat_share() {
		return float_share;
	}

	public void setFloat_share(BigDecimal float_share) {
		this.float_share = float_share;
	}

	public BigDecimal getFree_share() {
		return free_share;
	}

	public void setFree_share(BigDecimal free_share) {
		this.free_share = free_share;
	}

	public BigDecimal getTotal_mv() {
		return total_mv;
	}

	public void setTotal_mv(BigDecimal total_mv) {
		this.total_mv = total_mv;
	}

	public BigDecimal getCirc_mv() {
		return circ_mv;
	}

	public void setCirc_mv(BigDecimal circ_mv) {
		this.circ_mv = circ_mv;
	}

	public BigDecimal getTurnover_rate_f() {
		return turnover_rate_f;
	}

	public void setTurnover_rate_f(BigDecimal turnover_rate_f) {
		this.turnover_rate_f = turnover_rate_f;
	}

	public BigDecimal getVolume_ratio() {
		return volume_ratio;
	}

	public void setVolume_ratio(BigDecimal volume_ratio) {
		this.volume_ratio = volume_ratio;
	}

	public Integer isLimited() {
		return open.equals(high) && open.equals(low) && open.equals(close) ? 1 : 0;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public boolean isLine() {
		return high.compareTo(low)==0;
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

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Kbar [id=" + id + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
				+ ", quantity=" + quantity + ", amount=" + amount + ", date=" + date + ", turnover_rate_f="
				+ turnover_rate_f + ", volume_ratio=" + volume_ratio + ", total_mv=" + total_mv + ", circ_mv=" + circ_mv
				+ ", total_share=" + total_share + ", float_share=" + float_share + ", free_share=" + free_share
				+ ", pe=" + pe + "]";
	}

}
