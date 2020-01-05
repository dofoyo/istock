package com.rhb.istock.kdata.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID,
			@RequestParam(value="endDate", required=false) String endDate
			) {
		
		//System.out.println(endDate);
		
		LocalDate theEndDate = null;
		/*if(endDate!=null && !endDate.isEmpty()) {
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}*/
		
		KdatasView kdatas = new KdatasView();
		
		Item item = itemService.getItem(itemID);
		kdatas.setItemID(itemID);
		if(item!=null) {
			kdatas.setCode(item.getCode());
			kdatas.setName(item.getName());
			
			List<LocalDate> dates = kdataService.getKdata(itemID, theEndDate, true).getDates();
			Kbar bar=null;
			for(LocalDate date : dates) {
				bar = kdataService.getKbar(itemID, date, true);
				kdatas.addKdata(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
			}
			
			Kbar latestBar;
			if(theEndDate==null) {
				latestBar = kdataService.getLatestMarketData(itemID);
				if(bar==null || !bar.getDate().equals(latestBar.getDate())) {
					kdatas.addKdata(latestBar.getDate(), latestBar.getOpen(), latestBar.getHigh(), latestBar.getLow(), latestBar.getClose());
				}
			}
		}
		
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
}
