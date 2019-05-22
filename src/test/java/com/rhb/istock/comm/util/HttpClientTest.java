package com.rhb.istock.comm.util;

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
	
	@Test
	public void sleep() {
		for(int i=0; i<10; i++) {
			HttpClient.sleep(5);
		}
	}
}
