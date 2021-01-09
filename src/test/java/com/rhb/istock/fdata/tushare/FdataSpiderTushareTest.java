package com.rhb.istock.fdata.tushare;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.tushare.FdataSpiderTushare;
import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataSpiderTushareTest {
	@Autowired
	@Qualifier("fdataSpiderTushare")
	FdataSpiderTushare fdataSpiderTushare;

	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;
	
	@Test
	public void downs() throws Exception {
		long beginTime=System.currentTimeMillis(); 

		List<ItemEntity> items = itemRepository.getItemEntities();
		int i=1;
		for(ItemEntity item : items){
			Progress.show(items.size(),i++, item.getItemId());
			try {
				/*if(!fdataSpiderTushare.isExistIndicator(item.getItemId())) {
					fdataSpiderTushare.downIndicator(item.getItemId());
					Thread.sleep(500); //一分钟200个	
				}
				if(!fdataSpiderTushare.isExistIncome(item.getItemId())) {
					fdataSpiderTushare.downIncome(item.getItemId());
					Thread.sleep(500); //一分钟200个	
				}
				if(!fdataSpiderTushare.isExistCashflow(item.getItemId())) {
					fdataSpiderTushare.downCashflow(item.getItemId());
					Thread.sleep(500); //一分钟200个	
				}*/
				if(!fdataSpiderTushare.isExistBalancesheet(item.getItemId())) {
					fdataSpiderTushare.downBalancesheet(item.getItemId());
					Thread.sleep(1000); //一分钟200个	
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	//@Test
	public void downForecast() throws Exception {
		String itemID = "sz002077";
		fdataSpiderTushare.downForecast(itemID);
	}
	
	//@Test
	public void downFloatholders() {
		String itemID = "sh603399";
		String period = "20200331";
		fdataSpiderTushare.downFloatholders(itemID, period);
	}

	//@Test
	public void downAll() {
		//String period = "20200331";
		fdataSpiderTushare.downAll();
	}
}
