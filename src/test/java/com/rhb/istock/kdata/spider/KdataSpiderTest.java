package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataSpiderTest {
	@Autowired
	@Qualifier("kdataSpiderTushare")
	KdataSpider kdataSpiderTushare;
	
	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;
	
	//@Test
	public void testDwnKdataByID() {
		List<ItemEntity> items = itemRepository.getItemEntities();
		int i=1;
		for(ItemEntity item : items){
			System.out.format(" %d/%d \n", i++, items.size());
			try {
				kdataSpiderTushare.downKdata(item.getItemId());
				Thread.sleep(300); //一分钟200个
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	@Test
	public void testDwnKdataByDate() {
		LocalDate date = LocalDate.parse("2019-03-19");
		try {
			kdataSpiderTushare.downKdata(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("done!");
	}
	
}
