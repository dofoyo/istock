package com.rhb.istock.kdata.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;

@RestController
public class KdataAPI {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@GetMapping("/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID) {
		KdatasView kdatas = new KdatasView();
		
		Item item = itemService.getItem(itemID);
		kdatas.setItemID(itemID);
		kdatas.setCode(item.getCode());
		kdatas.setName(item.getName());
		
		List<LocalDate> dates = kdataService.getDailyKdata(itemID, true).getDates();
		Kbar bar;
		for(LocalDate date : dates) {
			bar = kdataService.getKbar(itemID, date, true);
			kdatas.addKdata(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());

		}
		bar = kdataService.getLatestMarketData(itemID);
		kdatas.addKdata(bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
		
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
}
