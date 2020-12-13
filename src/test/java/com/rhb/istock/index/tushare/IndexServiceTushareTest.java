package com.rhb.istock.index.tushare;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IndexServiceTushareTest {
	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	@Autowired
	@Qualifier("indexSpiderTushare")
	IndexSpiderTushare indexSpiderTushare;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	//@Test
	public void down() {
		indexSpiderTushare.downIndex_Daily();
		indexSpiderTushare.downIndex_weight();
		indexSpiderTushare.downIndex_basic();
		indexServiceTushare.generateIndex();
	}
	
	@Test
	public void getSseiGrowthRate() {
		LocalDate date = LocalDate.parse("2018-05-31");
		Integer sseiFlag = kdataService.getSseiFlag(date);
		Integer sseiTrend = kdataService.getSseiTrend(date,13);
		Integer[] sseiRatio = indexServiceTushare.getSseiGrowthRate(date, 13);
		System.out.println("sseiFlag=" + sseiFlag);
		System.out.println("sseiTrend=" + sseiTrend);
		System.out.println("sseiRatio=" + sseiRatio);
	}
	
	//@Test
	public void getGrowthRate() throws Exception {
		long beginTime=System.currentTimeMillis(); 
		String ts_code = "000001.SH";
		LocalDate endDate = LocalDate.now();
		Integer period = 13;
		Integer[] rate = indexServiceTushare.getGrowthRate(ts_code, endDate, period);
		System.out.println(rate);
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	
	//@Test
	public void downIndex_classify() throws Exception {
		long beginTime=System.currentTimeMillis(); 

		LocalDate endDate = LocalDate.now();
		Integer period = 13;
		Integer top = 5;
		Set<String> ids = indexServiceTushare.getItemIDsFromTopGrowthRateIndex(endDate, period, top);
		
		System.out.println(ids);
		System.out.println(ids.size());
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	//@Test
	public void generateIndex() {
		indexServiceTushare.generateIndex();
	}
	
	//@Test
	public void getGrowthRateOfAll() {
		long beginTime=System.currentTimeMillis(); 
		
		LocalDate endDate = LocalDate.parse("2020/08/05",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Integer period = 13;
		
		Map<String,Set<IndexWeight>> members = indexServiceTushare.getIndexWeights();
		Set<IndexWeight> ms;
		
		TreeMap<Integer[],Set<String>> indexs = indexServiceTushare.getGrowthRate(endDate, period);
		NavigableSet<Integer[]> keys = indexs.descendingKeySet();
		Set<String> codes;
		IndexBasic basic;
		int i = 0 ;
		StringBuffer sb = new StringBuffer(endDate.toString() + ", period=" + period + "\n");
		for(Integer[] key : keys) {
			codes = indexs.get(key);
			for(String code : codes) {
				basic = indexServiceTushare.getIndexBasic(code);
				if(basic!=null) {
					sb.append("rate=" + key.toString() + " - " + code + " - " + basic.getName() + ": ");
				}
/*				ms = members.get(code);
				if(ms!=null) {
					for(IndexWeight iw : ms) {
						sb.append(iw.getItemID() + ",");
					}
				}*/
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	
}
