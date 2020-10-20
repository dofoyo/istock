package com.rhb.istock.fdata.eastmoney;

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
public class FdataSpiderEastmoneyTest {
	@Autowired
	@Qualifier("fdataSpiderEastmoney")
	FdataSpiderEastmoney fdataSpiderEastmoney;

	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;
	
	//@Test
	public void downIndicators() throws Exception {
		long beginTime=System.currentTimeMillis(); 

		List<ItemEntity> items = itemRepository.getItemEntities();
		int i=1;
		for(ItemEntity item : items){
			Progress.show(items.size(),i++, item.getItemId());
			try {

			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	@Test
	public void downForecast() throws Exception {
		String itemID = "sz002610";
		//fdataSpiderEastmoney.down(itemID);
		fdataSpiderEastmoney.downAll();
	}

}
