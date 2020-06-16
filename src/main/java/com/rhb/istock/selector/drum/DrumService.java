package com.rhb.istock.selector.drum;

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
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("drumService")
public class DrumService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Value("${drumFile}")
	private String drumFile;	
	
	protected static final Logger logger = LoggerFactory.getLogger(DrumService.class);
	
	public void generateDrum(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateDrum(date));
		}
		FileTools.writeTextFile(drumFile, sb.toString(), false);
	}
	
	public String getDrum() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateDrum(date);
	}
	
	public String generateDrum(LocalDate endDate) {
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Integer previous_period = 8;
		StringBuffer sb = new StringBuffer(endDate.toString() + ":");
		if(musters!=null) {
			List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
			if(previousDates!=null) {
				List<Muster>  ms = new ArrayList<Muster>(musters.values());
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //Z-A
					}
				});
				
				Map<String,Muster> previous = kdataService.getMusters(previousDates.get(0));
				Integer ratio, sseiRatio = kdataService.getSseiRatio(endDate, previous_period);
				Muster m,p;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(m!=null && p!=null) {
						ratio = Functions.growthRate(m.getAveragePrice21(), m.getAveragePrice());
						if(Functions.growthRate(m.getClose(),p.getClose()) >= sseiRatio
							&& ratio<=5
							&& ratio >0
							&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1
							&& !m.isUpLimited() 
							&& !m.isDownLimited() 
							) {
						sb.append(m.getItemID() + "(" + i + ")" +",");
						}
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
