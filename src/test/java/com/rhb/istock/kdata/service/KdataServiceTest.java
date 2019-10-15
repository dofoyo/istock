package com.rhb.istock.kdata.service;

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
	public void downKdatas() {
		try {
			kdataService.downKdatasAndFactors();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void generateMusters() {
		kdataService.generateMusters();
	}
	
	@Test
	public void generateLatestMusters() {
		kdataService.generateLatestMusters();
	}

	//@Test
	public void updateLatestMusters() {
		kdataService.updateLatestMusters();
	}
	
	//@Test
	public void getLastMusters() {
		Map<String,Muster> musters = kdataService.getLatestMusters();
		List<Muster> mm = new ArrayList<Muster>(musters.values());
		Collections.sort(mm, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}});
		for(Muster muster : mm) {
			System.out.println(muster);
		}
	}
}
