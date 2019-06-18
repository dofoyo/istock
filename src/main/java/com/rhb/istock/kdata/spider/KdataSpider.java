package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;

public interface KdataSpider {

	/*
	 * id 格式为 sh600001 或 sz000001
	 */
	public void downKdatas(String id) throws Exception ;
	public void downKdatas(List<String> ids) throws Exception ;
	public void downKdatas(LocalDate date) throws Exception ;

	
	public void downFactors(String id) throws Exception ;
	public void downFactors(List<String> ids) throws Exception ;
	public void downFactors(LocalDate date) throws Exception ;

	
}
