package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataSpiderTest {
	@Autowired
	@Qualifier("kdataSpiderTushare")
	KdataSpider kdataSpiderTushare;

	@Autowired
	@Qualifier("kdataSpider163")
	KdataSpider kdataSpider163;
	
	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;
	
	//@Test
	public void testDwnKdataByID() throws Exception {
		String itemID = "sz000852";
		kdataSpiderTushare.downKdatas(itemID);
		kdataSpiderTushare.downFactors(itemID);
	}
	
	@Test
	public void testDwnKdataByIDs() {
		List<ItemEntity> items = itemRepository.getItemEntities();
		int i=1;
		for(ItemEntity item : items){
			Progress.show(items.size(),i++, " testDwnKdataByIDs  " + item.getItemId());
			try {
				kdataSpiderTushare.downKdatas(item.getItemId());
				kdataSpiderTushare.downFactors(item.getItemId());
				Thread.sleep(300); //一分钟200个
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	//@Test
	public void testDwnKdataByDate() {
		LocalDate date = LocalDate.parse("2019-06-19");
		try {
			//kdataSpiderTushare.downKdatas(date);
			kdataSpiderTushare.downFactors(date); //此处仅供测试，正式使用时，factor的日期要比kdata的日期提前一个交易日
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("done!");
	}
	
	//@Test
	public void testDownKdataByIDFrom163() {
		String id = "sh000001";
		try {
			kdataSpider163.downKdatas(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
