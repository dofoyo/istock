package com.rhb.istock.kdata.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataServiceTest {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	//@Test
	public void generateMusters() {
		LocalDate date = LocalDate.parse("2000-01-01");
		//kdataService.generateMusters(date);
		kdataService.generateLatestMusters();
		kdataService.updateLatestMusters();
	}
	
	
	//@Test
	public void getLastMusters() {
		LocalDate date = LocalDate.parse("2020-01-09");
		Map<String,Muster> musters = kdataService.getMusters(date);
		List<Muster> mm = new ArrayList<Muster>(musters.values());
		Collections.sort(mm, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getTotal_mv().compareTo(o2.getTotal_mv());
			}});
		String str = null;
		BigDecimal b = new BigDecimal(500000000).multiply(new BigDecimal(100));
		for(Muster muster : mm) {
			if(muster.getTotal_mv().compareTo(b)==-1) {
				str = String.format("%s: %.2f\n", muster.getItemName(), muster.getTotal_mv());
				System.out.println(str);
			}

		}
	}
	
	@Test
	public void downClosedDatas() {
		LocalDate date = LocalDate.parse("2020-02-26");
		try {
			kdataService.downClosedDatas(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
