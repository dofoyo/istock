package com.rhb.istock.kdata.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataServiceTest {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Test
	public void test() {
		String path = "D:\\dev\\istock-data\\kdata\\sz000651.txt";
		String itemID = "sz000651";
		boolean byCache = false;
		Kdata data = kdataService.getDailyKdata(itemID,byCache);
		String str = data.getString();
		FileUtil.writeTextFile(path, str, false);
		
	}
}
