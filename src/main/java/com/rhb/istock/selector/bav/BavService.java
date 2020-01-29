package com.rhb.istock.selector.bav;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("bavService")
public class BavService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Value("${bavFile}")
	private String bavFile;	
	
	protected static final Logger logger = LoggerFactory.getLogger(BavService.class);
	
	public void generateBAV(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateBAV(date));
		}
		FileTools.writeTextFile(bavFile, sb.toString(), false);
	}
	
	private String generateBAV(LocalDate endDate) {
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Integer previous_period = 8;
		StringBuffer sb = new StringBuffer(endDate.toString() + ":");
		if(musters!=null) {
			List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
			if(previousDates!=null) {
				Map<String,Muster> previous = kdataService.getMusters(previousDates.get(0));
				Integer sseiFlag = kdataService.getSseiFlag(endDate);
				List<String> hs300 = itemService.getHs300(endDate);
				
				List<Muster>  ms = new ArrayList<Muster>();
				for(String id : hs300) {
					if(musters.get(id) != null) {
						ms.add(musters.get(id));
					}
				}
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						return o2.getAmount().compareTo(o1.getAmount()); //Z-A
					}
				});
				
				Muster m,p;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(m!=null && p!=null
							&& !m.isUpLimited() 
							&& !m.isDownLimited() 
							&& m.isBreaker(8)
							&& m.getAverageAmount().compareTo(p.getAverageAmount())==1
							) {
						sb.append(m.getItemID() + "(" + i + ")" +",");
					}
				}
				
				sb.append("\n");
				
			}else {
				sb.append(" No previous!");
			}
		}else {
			sb.append(" No muster!");
		}
		
		logger.info(sb.toString());
		
		return sb.toString();
	}
}
