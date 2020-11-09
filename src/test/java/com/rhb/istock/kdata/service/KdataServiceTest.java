package com.rhb.istock.kdata.service;

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
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataServiceTest {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;	
	
	@Test
	public void getSSEI() {
		String itemID = "sh000001";
		boolean byCache = true;
		Kdata kdata = kdataService.getKdata(itemID, byCache);
		System.out.println(kdata.getLastBar().getDate());
	}
	
	//@Test
	public void doOpen() throws Exception {
		//itemService.downItems();		// 1. 下载最新股票代码
		//itemService.init();  // 2. 
		kdataService.downFactors(); // 3. 上一交易日的收盘数据要等开盘前才能下载到, 大约需要15分钟
		kdataService.downSSEI();
		kdataService.generateLatestMusters(null);
		kdataService.updateLatestMusters();
	}
	
	//@Test
	public void generateMusters() {
		LocalDate date = LocalDate.parse("2010-01-01");
		//kdataService.generateMusters(date);
		kdataService.generateLatestMusters(null);
		kdataService.updateLatestMusters();
	}
	
	//@Test
	public void test() {
		String itemID = "sh600673";
		Kbar bar =  kdataService.getLatestMarketData(itemID);
		System.out.println(bar);

	}
	
	
	//@Test
	public void getLastMusters() {
		LocalDate date = LocalDate.parse("2020-08-14");
		Map<String,Muster> musters = kdataService.getMusters(date);
		List<Muster> mm = new ArrayList<Muster>(musters.values());
		Collections.sort(mm, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o2.getAbove2121().compareTo(o1.getAbove2121())==0) {
					return o1.getCloseRateOf21().compareTo(o2.getCloseRateOf21());
				}
				return o2.getAbove2121().compareTo(o1.getAbove2121());
			}});
		String str = null;
		//BigDecimal b = new BigDecimal(500000000).multiply(new BigDecimal(100)); //伍佰亿
		for(Muster muster : mm) {
			//if(muster.getTotal_mv().compareTo(b)==-1) {
				str = String.format("%s, %s: %d %d %.2f\n", muster.getItemID(), muster.getItemName(), muster.getAbove2121(), muster.getAbove2189(), muster.getCloseRateOf21());
				System.out.println(str);
			//}
		}
	}
	
	//@Test
	public void downClosedDatas() {
		LocalDate date = LocalDate.parse("2020-10-19");
		try {
			//kdataService.downClosedDatas(date);
			kdataService.downFactors(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void getSseiRatio() {
		List<LocalDate> dates = kdataService.getMusterDates();
		for(LocalDate date : dates) {
			System.out.format("%tF, %d\n",date,kdataService.getSseiFlag(date));

		}
/*		LocalDate date = LocalDate.parse("2020-03-20");
		try {
			System.out.println(kdataService.getSseiFlag(date));
			//System.out.println(kdataService.getSseiRatio(date, 8));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	//@Test
	public void getMusters() {
		LocalDate date = LocalDate.parse("2020-01-09");
		Map<String,Muster> musters = kdataService.getMusters(date);
		List<Muster> mm = new ArrayList<Muster>(musters.values());
		Collections.sort(mm, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getTotal_mv().compareTo(o2.getTotal_mv());
			}});
		for(Muster muster : mm) {
			if(muster.getItemID().startsWith("sz300")) {
				System.out.println(muster);
			}

		}
	}
	
	//@Test
	public void testt() {
		LocalDate endDate = LocalDate.parse("2020-09-12");
		List<LocalDate> dates = kdataService.getMusterDates(13, endDate);
		System.out.println(dates);
	}
}
