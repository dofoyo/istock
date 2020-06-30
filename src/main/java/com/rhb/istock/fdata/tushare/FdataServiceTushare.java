package com.rhb.istock.fdata.tushare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@Service("fdataServiceTushare")
public class FdataServiceTushare {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;

	protected static final Logger logger = LoggerFactory.getLogger(FdataServiceTushare.class);
	
	public List<String> getGrowModels(String b_date, String e_date,Integer n) {
		long beginTime=System.currentTimeMillis(); 

		List<String> models = new ArrayList<String>(); 

		GrowModel model;
		
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, " getGrowModels: " + item.getItemID());//进度条
			model = this.getGrowModel(item.getItemID(),item.getName(), b_date, e_date,n);
			if(model!=null && model.isOK()) {
				models.add(item.getItemID());
			}
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          

		return models;
		
	}
	
	private GrowModel getGrowModel(String itemID, String itemName, String b_date, String e_date,Integer n) {
		Fina b_fina = fdataRepositoryTushare.getFina(itemID, b_date);
		Fina e_fina = fdataRepositoryTushare.getFina(itemID, e_date);
		fdataRepositoryTushare.init();
		return new GrowModel(itemID, itemName,b_fina, e_fina,n);
	}
}
