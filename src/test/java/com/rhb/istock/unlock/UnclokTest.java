package com.rhb.istock.unlock;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UnclokTest {
	@Autowired
	@Qualifier("unlockDataSpiderTushare")
	UnlockDataSpiderTushare unlockDataSpiderTushare;

	@Autowired
	@Qualifier("unlockDataRepositoryTushare")
	UnlockDataRepositoryTushare unlockDataRepositoryTushare;

	
	@Autowired
	@Qualifier("unlockService")
	UnlockService unlockService;

	@Test
	public void getUnlockDatas() {
		List<UnlockData> datas = unlockService.getUnlockDatas();
		System.out.println("code, annDate, floatDate, floatShare, floatRatio, annPrice, floatPrice, latestPrice, highestPrice, profitRatio");
		for(UnlockData data : datas) {
			System.out.println(data.getInfo());
		}		
	}
	
	//@Test
	public void getUnlockData() {
		String itemID = "sh600390";
		List<UnlockData> datas = unlockService.getUnlockData(itemID);
		for(UnlockData data : datas) {
			System.out.println(data.getInfo());
		}
	}

	//@Test
	public void getUnlockKdataEntity() {
		String itemID = "sh600390";
		List<UnlockDataEntity> datas = unlockDataRepositoryTushare.getUnlockKdata(itemID);
		for(UnlockDataEntity data : datas) {
			System.out.println(data);
		}
	}
	
	//@Test
	public void downUnlockDatas() {
		unlockService.downUnlockDatas();
	}
	
	//@Test
	public void downUnlockData() throws Exception {
		String itemID = "sh600390";
		unlockDataSpiderTushare.downUnlockData(itemID);
	}
}
