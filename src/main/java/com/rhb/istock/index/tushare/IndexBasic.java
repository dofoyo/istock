package com.rhb.istock.index.tushare;

public class IndexBasic {
	private String ts_code;//	str	TS代码
	private String name;//	str	简称
	private String market;//	str	市场
	private String publisher;//	str	发布方
	private String category;//	str	指数类别
	private String base_date;//	str	基期
	private String base_point;//	float	基点
	private String list_date;//	str	发布日期
	
	public IndexBasic(String ts_code, String name, String market, String publisher, String category, String base_date,
			String base_point, String list_date) {
		super();
		this.ts_code = ts_code;
		this.name = name;
		this.market = market;
		this.publisher = publisher;
		this.category = category;
		this.base_date = base_date;
		this.base_point = base_point;
		this.list_date = list_date;
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
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getBase_date() {
		return base_date;
	}
	public void setBase_date(String base_date) {
		this.base_date = base_date;
	}
	public String getBase_point() {
		return base_point;
	}
	public void setBase_point(String base_point) {
		this.base_point = base_point;
	}
	public String getList_date() {
		return list_date;
	}
	public void setList_date(String list_date) {
		this.list_date = list_date;
	}
	@Override
	public String toString() {
		return "IndexBasic [ts_code=" + ts_code + ", name=" + name + ", market=" + market + ", publisher=" + publisher
				+ ", category=" + category + ", base_date=" + base_date + ", base_point=" + base_point + ", list_date="
				+ list_date + "]";
	}

}
