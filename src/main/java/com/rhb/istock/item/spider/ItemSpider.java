package com.rhb.istock.item.spider;

public interface ItemSpider {
	/*
	 * 每周1 - 5，上午9:00，执行一次
	 */
	public void download() throws Exception ;
	
	public String getTopic(String itemID);
}
