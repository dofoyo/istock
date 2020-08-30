package com.rhb.istock.fund;

public class FundBasic {
	private String ts_code;	//str	Y	基金代码
	private String name;	//str	Y	简称
	private String management;	//str	Y	管理人
	private String fund_type;	//str	Y	投资类型
	private String status; //str	Y	存续状态D摘牌 I发行 L已上市
	
	public FundBasic(String ts_code, String name, String management, String fund_type, String status) {
		super();
		this.ts_code = ts_code;
		this.name = name;
		this.management = management;
		this.fund_type = fund_type;
		this.status = status;
	}
	
	public String getFund_type() {
		return fund_type;
	}

	public void setFund_type(String fund_type) {
		this.fund_type = fund_type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTs_code() {
		return ts_code;
	}
	public void setTs_code(String ts_code) {
		this.ts_code = ts_code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getManagement() {
		return management;
	}
	public void setManagement(String management) {
		this.management = management;
	}
	@Override
	public String toString() {
		return "FundBasic [ts_code=" + ts_code + ", name=" + name + ", management=" + management + ", fund_type="
				+ fund_type + ", status=" + status + "]";
	}
	
}
