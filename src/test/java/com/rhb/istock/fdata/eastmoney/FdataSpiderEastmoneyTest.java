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
	
	@Test
	public void downForecast() throws Exception {
		//String itemID = "sz002610";
		//fdataSpiderEastmoney.down(itemID);
		fdataSpiderEastmoney.downAll();
	}

}
