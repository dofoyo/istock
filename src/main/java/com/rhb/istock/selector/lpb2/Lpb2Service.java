package com.rhb.istock.selector.lpb2;

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
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("lpb2Service")
public class Lpb2Service {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Value("${lpb2File}")
	private String lpb2File;	
	
	protected static final Logger logger = LoggerFactory.getLogger(Lpb2Service.class);
	
	public void generateLPB2(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateLPB2(date));
		}
		FileTools.writeTextFile(lpb2File, sb.toString(), false);
	}
	
	public String getLpb2() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateLPB2(date);
	}
	
	public String generateLPB2(LocalDate endDate) {
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Integer previous_period = 8;
		StringBuffer sb = new StringBuffer(endDate.toString() + ":");
		if(musters!=null) {
			List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
			if(previousDates!=null) {
				Map<String,Muster> previous = kdataService.getMusters(previousDates.get(0));
				
				List<Muster>  ms = new ArrayList<Muster>(musters.values());
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						if(o1.getAverageGap().equals(o2.getAverageGap())) {
							return o1.getLatestPrice().compareTo(o2.getLatestPrice());
						}else {
							return o1.getAverageGap().compareTo(o2.getAverageGap()); //a-z
						}
					}
				});
				
				Muster m,p;
				Integer ratio=8;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(m!=null && p!=null
							&& !m.isUpLimited() 
							&& !m.isDownLimited() 
							&& m.isBreaker(ratio)
							&& p.getAverageGap()<ratio
							&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1
							) {
						sb.append(m.getItemID() + "(" + String.format("%04d",i) + ")" +",");
					}
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
