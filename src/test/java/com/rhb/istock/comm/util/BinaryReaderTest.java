package com.rhb.istock.comm.util;

import org.junit.Test;

/**
 * 
 * @author Administrator
 * 读取通达信的概念板块数据
 *
 *
 */

public class BinaryReaderTest {

	//@Test
	public void test() {
		String fileName = "D:\\dev\\istock-data\\kdata\\block_gn.dat";
		BinaryReader br = new BinaryReader(fileName);
		String str1 =  br.readString(28);
		System.out.println(str1);
		for(int i=0; i<100; i++) {
			int j = br.read();
			System.out.println(j);
		}
	}
	
	//@Test
	public void test1() {
		double or1 = 120.0;
		double or3 = 100.0;
		double rate1 = Math.sqrt(or1/or3)-1;
		double rate2 = Math.pow(or1/or3, 1.0/2)-1;
		
		System.out.println(rate1);
		System.out.println(rate2);
	}
	
	@Test
	public void test2() {
		String itemID = "300022";
		System.out.println(itemID.startsWith("300"));
	}
}
