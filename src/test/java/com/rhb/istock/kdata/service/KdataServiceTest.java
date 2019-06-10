package com.rhb.istock.kdata.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataServiceTest {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	//@Test
	public void downKdatas() {
		try {
			kdataService.downKdatasAndFactors();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void getLatestFactors() {
		String itemID = "sz300538";
		BigDecimal factor = kdataService.getLatestFactors(itemID);
		System.out.println(factor);
	}
	
	
	//@Test
	public void generateLatestFactors() {
		kdataService.generateLatestFactors();
	}
	
	//@Test
	public void generateMusters() {
		kdataService.generateMusters();
	}
	
	@Test
	public void generateLastMusters() {
		kdataService.generateLastMusters();
	}
	
	//@Test
	public void getLastMusters() {
		List<Muster> musters = kdataService.getLastMusters();
		for(Muster muster : musters) {
			System.out.println(muster);
		}
	}
}
