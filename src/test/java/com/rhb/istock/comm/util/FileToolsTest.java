package com.rhb.istock.comm.util;

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
	
	@Test
	public void test1() {
		BigDecimal a = new BigDecimal(-0.0001);
		System.out.println(a.compareTo(BigDecimal.ZERO));
	}
}
