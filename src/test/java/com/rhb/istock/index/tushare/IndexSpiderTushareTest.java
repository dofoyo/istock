package com.rhb.istock.index.tushare;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IndexSpiderTushareTest {
	@Autowired
	@Qualifier("indexSpiderTushare")
	IndexSpiderTushare indexSpiderTushare;

	//@Test
	public void downIndex_classify() throws Exception {
		long beginTime=System.currentTimeMillis(); 
		indexSpiderTushare.downIndex_basic();
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	//@Test
	public void downIndex_member() throws Exception {
		long beginTime=System.currentTimeMillis(); 
		indexSpiderTushare.downIndex_weight();
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	@Test
	public void downIndex_Daily() {
		//String itemID = "sh000001";
		//indexSpiderTushare.downIndex_Daily(itemID);
		indexSpiderTushare.downIndex_Daily();
		
	}
	
}
