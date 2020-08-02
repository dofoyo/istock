package com.rhb.istock.index.tushare;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IndexRepositoryTushareTest {

	@Autowired
	@Qualifier("indexRepositoryTushare")
	IndexRepositoryTushare indexRepositoryTushare;
	
	//@Test
	public void getIndexBasic() {
		Set<IndexBasic> ics = indexRepositoryTushare.getIndexBasic();
		//Map<String,Set<IndexMember>> members = indexRepositoryTushare.getIndexMembers();
		for(IndexBasic ic : ics) {
			System.out.println(ic);
		}
		System.out.println("there are " + ics.size() + " indexes");
	}
	
	@Test
	public void getIndexWeights() {
		String ts_code = "000122.SH";
		Map<String,Set<IndexWeight>> members = indexRepositoryTushare.getIndexWeights();
		Set<IndexWeight> iws = members.get(ts_code);
		for(IndexWeight iw : iws) {
			System.out.println(iw.getCon_code());
		}
		/*for(Map.Entry<String, Set<IndexWeight>> ms : members.entrySet()) {
			for(IndexWeight m : ms.getValue()) {
				System.out.println(ms.getKey() + "," + m.getCon_code());
			}
		}*/
	}
	
	//@Test
	public void getIndexData() {
		String ts_code = "000001.SH";
		IndexData data = indexRepositoryTushare.getIndexDatas(ts_code);
		
		LocalDate trade_date = LocalDate.parse("20200731", DateTimeFormatter.ofPattern("yyyyMMdd"));
		IndexBar bar = data.getBar(trade_date);
		
		System.out.println(bar);
	}
	
}
