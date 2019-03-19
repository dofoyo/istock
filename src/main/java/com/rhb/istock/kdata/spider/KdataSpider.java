package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;

public interface KdataSpider {

	/*
	 * id 格式为 sh600001 或 sz000001
	 */
	public void downKdata(List<String> ids) throws Exception ;
	public void downKdata(String id) throws Exception ;
	public void downKdata(LocalDate date) throws Exception ;

	
}
