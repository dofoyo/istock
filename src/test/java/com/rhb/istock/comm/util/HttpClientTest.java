package com.rhb.istock.comm.util;

import java.util.List;

import org.junit.Test;

public class HttpClientTest {

	@Test
	public void test() {
		//String strUrl = "http://f10.eastmoney.com/ProfitForecast/ProfitForecastAjax?code=sz002610";
		//String strUrl = "http://basic.10jqka.com.cn/002610/worth.html";
		String strUrl = "http://reportapi.eastmoney.com/report/list?cb=datatable8515095&pageNo=1&pageSize=200&code=603345&industryCode=*&industry=*&rating=*&ratingchange=*&beginTime=2017-01-01&endTime=2018-01-01&fields=&qType=0&_=1603757059836";
		String result = HttpClient.doGet(strUrl);
		//String gn = ParseString.subString(result, "<dd title=\"|\">");
		System.out.println(result);
		//System.out.println(gn);
	}
	
	//@Test
	public void sleep() {
		for(int i=0; i<10; i++) {
			HttpClient.sleep(5);
		}
	}
	
	//@Test
	public void test1() {
		String strUrl = "http://bond.jrj.com.cn/data";
		String result = HttpClient.doGet(strUrl);
		List<String> ss = ParseString.subStrings(result, "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"dt1 sortable scrollable table_bonddata_qz\" rowclass=\",ln\">|</div>");
		//String gn = ParseString.subString(result, "<dd title=\"|\">");
		System.out.println(result);
		System.out.println("----------");
		for(String s : ss) {
			System.out.println(s);
		}
	}
	
}
