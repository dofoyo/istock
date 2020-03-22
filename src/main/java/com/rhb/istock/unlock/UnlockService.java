package com.rhb.istock.unlock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;

@Service("unlockService")
public class UnlockService {
	@Autowired
	@Qualifier("unlockDataSpiderTushare")
	UnlockDataSpiderTushare unlockDataSpiderTushare;

	@Autowired
	@Qualifier("unlockDataRepositoryTushare")
	UnlockDataRepositoryTushare unlockDataRepositoryTushare;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	protected static final Logger logger = LoggerFactory.getLogger(UnlockService.class);
	
	public List<UnlockData> getUnlockDatas(){
		List<UnlockData> datas = new ArrayList<UnlockData>();
		List<String> ids = itemService.getItemIDs();
		for(String id : ids) {
			datas.addAll(this.getUnlockData(id));
		}
		
		Collections.sort(datas, new Comparator<UnlockData>() {
			@Override
			public int compare(UnlockData o1, UnlockData o2) {
				return o1.getRatio().compareTo(o2.getRatio());
			}
			
		});
		
		System.out.println("TOTAL: " + datas.size() + " records.");
		
		return datas;
	}
	
	public List<UnlockData> getUnlockData(String itemID) {
		Map<LocalDate, UnlockData> ms = new HashMap<LocalDate, UnlockData>();
		
		UnlockData d;
		Kbar bar1,bar2,bar3;
		LocalDate lastDate = kdataService.getLastKdataDate(itemID);
		Item item = itemService.getItem(itemID);
		List<UnlockDataEntity> datas = unlockDataRepositoryTushare.getUnlockKdata(itemID);
		for(UnlockDataEntity data : datas) {
			d = ms.get(data.getFloat_date());
			if(d == null) {
				d = new UnlockData(itemID);
				if(item!=null) {
					d.setItemName(item.getName());
				}
				
				d.setAnn_date(data.getAnn_date());
				d.setFloat_date(data.getFloat_date());
				d.setFloat_ratio(data.getFloat_ratio());
				d.setFloat_share(data.getFloat_share());
				
				bar1 = kdataService.getKbar(itemID, data.getAnn_date(), true);
				if(bar1!=null) {
					d.setAnnPrice(bar1.getClose());
				}
				
				bar2 = kdataService.getKbar(itemID, data.getFloat_date(), true);
				if(bar2!=null) {
					d.setFloatPrice(bar2.getClose());
				}
				
				bar3 = kdataService.getKbar(itemID, lastDate, true);
				if(bar3!=null) {
					d.setLatestPrice(bar3.getClose());
				}
				
				d.setHighest(kdataService.getHighestPrice(itemID, data.getFloat_date(), true));
				
				ms.put(data.getFloat_date(), d);
			}else {
				d.addFloat_ratio(data.getFloat_ratio());
				d.addFloat_share(data.getFloat_share());
			}
		}
		
		kdataService.evictKDataCache();
		
		return new ArrayList<UnlockData>(ms.values());
	}
	
	public void downUnlockDatas() {
		unlockDataSpiderTushare.downUnlockDatas(itemService.getItemIDs());
	}
}
