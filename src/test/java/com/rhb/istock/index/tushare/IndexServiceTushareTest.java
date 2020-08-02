package com.rhb.istock.index.tushare;


import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IndexServiceTushareTest {
	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	//@Test
	public void getGrowthRate() throws Exception {
		long beginTime=System.currentTimeMillis(); 
		String ts_code = "h50008.SH";
		LocalDate endDate = LocalDate.now();
		Integer period = 13;
		Integer rate = indexServiceTushare.getGrowthRate(ts_code, endDate, period);
		System.out.println(rate);
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	
	@Test
	public void downIndex_classify() throws Exception {
		long beginTime=System.currentTimeMillis(); 
		
		Map<String,Set<IndexWeight>> members = indexServiceTushare.getIndexWeights();
		Set<IndexWeight> ms;
		
		LocalDate endDate = LocalDate.now();
		Integer period = 13;
		TreeMap<Integer,Set<IndexBasic>> indexs = indexServiceTushare.getGrowthRate(endDate, period);
/*		Integer i = indexs.lastKey();
		Set<IndexBasic> basics = indexs.get(i);
		for(IndexBasic ib : basics) {
			ms = members.get(ib.getTs_code());
			if(ms!=null) {
				System.out.println("indexID="+ ib.getTs_code()+", indexName=" + ib.getName() + ", rate=" + i + ", and members are " + ms.size());
				for(IndexWeight iw : ms) {
					System.out.print(iw.getCon_code() + ",");
				}
			}		
		}	*/	
		
		for(Map.Entry<Integer, Set<IndexBasic>> entry : indexs.entrySet()) {
			for(IndexBasic ib : entry.getValue()) {
				System.out.print(entry.getKey() + ", " + ib.getName());
				ms = members.get(ib.getTs_code());
				if(ms!=null) {
					for(IndexWeight iw : ms) {
						System.out.print(iw.getCon_code() + ",");
					}
				}
				System.out.println("");
			}
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	
}
