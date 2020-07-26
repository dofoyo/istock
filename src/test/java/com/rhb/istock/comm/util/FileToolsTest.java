package com.rhb.istock.comm.util;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class FileToolsTest {
	//@Test
	public void test() {
		String path = "d:\\hua.txt";
		Map<LocalDate,Set<String>> content = new HashMap<LocalDate,Set<String>>();
		Set<String> ids = new HashSet<String>();
		ids.add("123");
		ids.add("234");
		content.put(LocalDate.now(), ids);
		FileTools.writeTextFile(path, content, false);
	}
	
	//@Test
	public void test1() {
		BigDecimal a = new BigDecimal(-0.0001);
		System.out.println(a.compareTo(BigDecimal.ZERO));
	}
	
	//@Test
	public void rename() {
		String path = "D:\\dev\\istock-data\\fdata\\tushare";		//要遍历的路径
		File file = new File(path);		//获取其file对象
		File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
		String newName=null;
		for(File f:fs){					//遍历File[]数组
			if(!f.isDirectory())		//若非目录(即文件)，则打印
				//System.out.println(f.getAbsolutePath());
				newName = new StringBuffer(f.getAbsolutePath()).insert(41, "_fina").toString();
				//System.out.println(newName.toString());
				f.renameTo(new File(newName));
		}
	}
	
	@Test
	public void testFunctions() {
		BigDecimal a = new BigDecimal(2.06);
		BigDecimal b = new BigDecimal(1.8);
		Integer rate = Functions.growthRate(b, a);
		System.out.println(rate);
		
		
	}
}
