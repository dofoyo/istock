package com.rhb.istock.selector.newb;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("newbService")
public class NewbService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;
	
	protected static final Logger logger = LoggerFactory.getLogger(NewbService.class);

	public List<String> getNewbs(LocalDate endDate){
		List<Muster> ms = new ArrayList<Muster>();
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		
		int i=1;
		for(Muster m : musters.values()) {
			//Progress.show(musters.values().size(), i++, "  get newbs, " + m.getItemID());

			if(m.isUpBreaker()) {
				ms.add(m);
			}
		}
		
		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o1.getHLGap().equals(o2.getHLGap())) {
					return o1.getLNGap().compareTo(o2.getLNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());
				}			
			}
			
		});
		
		List<String> ids = new ArrayList<String>();
		for(Muster m : ms) {
			ids.add(m.getItemID());
		}
		return ids;
	}
}
