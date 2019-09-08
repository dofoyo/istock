package com.rhb.istock.comm.util;

import java.util.List;

import org.junit.Test;

public class HttpClientTest {

	//@Test
	public void test() {
		String strUrl = "http://stockpage.10jqka.com.cn/300022/";
		String result = HttpClient.doGet(strUrl);
		String gn = ParseString.subString(result, "<dd title=\"|\">");
		System.out.println(result);
		System.out.println(gn);
	}
	
	//@Test
	public void sleep() {
		for(int i=0; i<10; i++) {
			HttpClient.sleep(5);
		}
	}
	
	@Test
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
