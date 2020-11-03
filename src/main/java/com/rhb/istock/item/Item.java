package com.rhb.istock.item;

public class Item {
	private String itemID;
	private String code;
	private String name;
	private String area;
	private String industry;
	private String ipo;
	private Integer cagr;  //利润年均增长率
	private Integer recommendations;  //机构推荐买入次数
	
	public Integer getCagr() {
		return cagr;
	}
	public void setCagr(Integer cagr) {
		this.cagr = cagr;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public String getNameWithCAGR() {
		String str = name;
		if(cagr!=null && cagr!=0) {
			str = str + "(" + cagr.toString() + "%)";
		}
		if(recommendations!=null && recommendations!=0) {
			str = str + "(" + recommendations.toString() + ")";
		}
		return str;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public String getMarket() {
		if(this.itemID.startsWith("sh60")) {
			return "上海主板";
		}else if(this.itemID.startsWith("sh688")) {
			return "科创板";
		}else if(this.itemID.startsWith("sz000")) {
			return "深圳主板";
		}else if(this.itemID.startsWith("sz002") || this.itemID.startsWith("sz003")) {
			return "中小板";
		}else if(this.itemID.startsWith("sz300")) {
			return "创业板";
		}else {
			return "other";
		}
	}
	
	public String getIpo() {
		return ipo;
	}
	public void setIpo(String ipo) {
		this.ipo = ipo;
	}
	@Override
	public String toString() {
		return "Item [itemID=" + itemID + ", code=" + code + ", name=" + name + ", area=" + area + ", industry="
				+ industry + ", ipo=" + ipo + ", cagr=" + cagr + "]";
	}
	public Integer getRecommendations() {
		return recommendations;
	}
	public void setRecommendations(Integer recommendations) {
		this.recommendations = recommendations;
	}

}
