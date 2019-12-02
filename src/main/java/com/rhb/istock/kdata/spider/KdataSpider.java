package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;

public interface KdataSpider {

	/*
	 * id 格式为 sh600001 或 sz000001
	 */
	public void downKdatas(String itemID) throws Exception ;
	public void downKdatas(List<String> itemIDs) throws Exception ;
	public void downKdatas(LocalDate date) throws Exception ;

	
	public void downFactors(String itemID) throws Exception ;
	public void downFactors(List<String> itemIDs) throws Exception ;
	public void downFactors(LocalDate date) throws Exception ;

	public void downBasics(String itemID) throws Exception ;
	public void downBasics(List<String> itemIDs) throws Exception ;
	public void downBasics(LocalDate date) throws Exception ;
	
}
