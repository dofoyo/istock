package com.rhb.istock.item.spider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ItemSpiderTest {
	@Autowired
	@Qualifier("itemSpiderTushare")
	ItemSpider itemSpider;
	
	//@Test
	public void test() {
		try {
			itemSpider.download();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done!");
	}
	
	@Test
	public void getTopic() {
		String itemID = "sz300022";
		String topic = itemSpider.getTopic(itemID);
		System.out.println(topic);
	}
	
	//@Test
	public void getTopciTops() {
		System.out.println(itemSpider.getTopicTops(5));
	}
}
