package com.rhb.istock.unlock;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UnlockDataEntity {
	private String ts_code;
    private LocalDate ann_date;
    private LocalDate float_date;
    private BigDecimal float_share;
    private BigDecimal float_ratio;
    private String holder_name;
    private String share_type;
	public String getTs_code() {
		return ts_code;
	}
	public void setTs_code(String ts_code) {
		this.ts_code = ts_code;
	}
	public LocalDate getAnn_date() {
		return ann_date;
	}
	public void setAnn_date(LocalDate ann_date) {
		this.ann_date = ann_date;
	}
	public LocalDate getFloat_date() {
		return float_date;
	}
	public void setFloat_date(LocalDate float_date) {
		this.float_date = float_date;
	}
	public BigDecimal getFloat_share() {
		return float_share;
	}
	public void setFloat_share(BigDecimal float_share) {
		this.float_share = float_share;
	}
	public BigDecimal getFloat_ratio() {
		return float_ratio;
	}
	public void setFloat_ratio(BigDecimal float_ratio) {
		this.float_ratio = float_ratio;
	}
	public String getHolder_name() {
		return holder_name;
	}
	public void setHolder_name(String holder_name) {
		this.holder_name = holder_name;
	}
	public String getShare_type() {
		return share_type;
	}
	public void setShare_type(String share_type) {
		this.share_type = share_type;
	}
	@Override
	public String toString() {
		return "UnlockData [ts_code=" + ts_code + ", ann_date=" + ann_date + ", float_date=" + float_date
				+ ", float_share=" + float_share + ", float_ratio=" + float_ratio + ", holder_name=" + holder_name
				+ ", share_type=" + share_type + "]";
	}
	
}
