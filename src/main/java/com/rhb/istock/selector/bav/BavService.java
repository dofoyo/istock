package com.rhb.istock.selector.bav;

import java.math.BigDecimal;
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

	public String getBAV() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateBAV(date);
	}
	
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
				
				//System.out.println("************  hs300 = " + ms.size());
				
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						return o2.getAmount().compareTo(o1.getAmount()); //Z-A
					}
				});
				
				Muster m,p;
				BigDecimal previousAverageAmount;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(p==null) {
						previousAverageAmount = BigDecimal.ZERO;
					}else {
						previousAverageAmount = p.getAverageAmount();
					}
					if(m!=null
							&& !m.isUpLimited() 
							&& !m.isDownLimited() 
							&& m.isBreaker(8)
							&& m.getAverageAmount().compareTo(previousAverageAmount)==1
							) {
						sb.append(m.getItemID() + "(" + String.format("%04d",i) + ")" +",");
					}
					
					//logger.info(String.format("%d %s %.0f",i,m.getItemID(),m.getAmount()));
				}
				
				sb.append("\n");
				
			}else {
				sb.append(" No previous!");
			}
		}else {
			sb.append(" No muster!");
		}
		
		//logger.info(sb.toString());
		
		return sb.toString();
	}
}
