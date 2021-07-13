package com.rhb.istock.item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.fdata.tushare.FdataServiceTushare;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ItemServiceTest {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;
	
	//@Test
	public void downTopics() {
		//itemService.downTopics();
		itemService.downItems();
	}
	
	//@Test
	public void getTopic() {
		String itemID = "sz300022";
		String topic = itemService.getTopic(itemID);
		System.out.println(topic);
	}
	
	@Test
	public void getItem() {
		String itemID = "sz000032";
		Item item = itemService.getItem(itemID);
		System.out.println(item);
	}
	
	//@Test
	public void getItems() {
		String period1 = "20191231";
		Map<String, Integer> advReceiptsRates1 = fdataServiceTushare.getAdvReceiptsRates(period1);
		
		String period2 = "20171231";
		Map<String, Integer> advReceiptsRates2 = fdataServiceTushare.getAdvReceiptsRates(period2);
		
		List<AdvReceipt> advReceipts = new ArrayList<AdvReceipt>();
		
		Integer a1, a2;
		List<String> ids = itemService.getItemIDs();
		for(String id : ids) {
			a1 = advReceiptsRates1.get(id);
			a2 = advReceiptsRates2.get(id);
			if(a1!=null && a2!=null) {
				advReceipts.add(new AdvReceipt(id, a1, a2));
			}
			
		}
		
		Collections.sort(advReceipts, new Comparator<AdvReceipt>() {

			@Override
			public int compare(AdvReceipt o1, AdvReceipt o2) {
				return o2.getAdv1().compareTo(o1.getAdv1());
			}
			
		});
		
		for(AdvReceipt advReceipt : advReceipts) {
			if(advReceipt.isOK()) {
				System.out.println(advReceipt.getId() + "," + advReceipt.getAdv1() + "," + advReceipt.getAdv2());
			}
		}
		
	}
	
	//@Test
	public void getSz50() {
		LocalDate date = LocalDate.parse("2019-11-01");
		List<String> items = itemService.getSz50(date);
		for(String item : items) {
			System.out.println(item);
		}
	}
	
	//@Test
	public void getHs300() {
		LocalDate date = LocalDate.parse("2005-04-08");
		List<String> items = itemService.getHs300(date);
		for(String item : items) {
			System.out.println(item);
		}
	}
	
	class AdvReceipt{
		private String id;
		private Integer adv1;
		private Integer adv2;
		public AdvReceipt(String id, Integer adv1, Integer adv2) {
			this.id = id;
			this.adv1 = adv1;
			this.adv2 = adv2;
		}
		
		public boolean isOK() {
			return adv1>adv2 && adv1>0 && adv2>0;
		}

		public String getId() {
			return id;
		}

		public Integer getAdv1() {
			return adv1;
		}

		public Integer getAdv2() {
			return adv2;
		}

		@Override
		public String toString() {
			return "AdvReceipt [id=" + id + ", adv1=" + adv1 + ", adv2=" + adv2 + "]";
		}
		
	}
	
}
