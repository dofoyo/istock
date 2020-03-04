package com.rhb.istock.selector.hlb2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("hlb2Service")
public class Hlb2Service {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Value("${hlb2File}")
	private String hlb2File;	
	
	protected static final Logger logger = LoggerFactory.getLogger(Hlb2Service.class);
	
	public void generateHLB2(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateHLB2(date));
		}
		FileTools.writeTextFile(hlb2File, sb.toString(), false);
	}
	
	public String getHLB2() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateHLB2(date);
	}
	
	public String generateHLB2(LocalDate endDate) {
		Set<String> breakers = new HashSet<String>();
		Integer previous_period = 13;
		List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
		Map<String,Muster> musters = null;
		for(LocalDate date : previousDates) {
			musters = kdataService.getMusters(date);
			for(Muster m : musters.values()) {
				if(m.isUpBreaker()) {
					breakers.add(m.getItemID());
				}
			}
		}
		
		Muster m;
		List<Muster> ms = new ArrayList<Muster>();
		if(musters != null) {
			for(Iterator<String> it=breakers.iterator(); it.hasNext();) {
				m = musters.get(it.next());
				if(m!=null 
						&& m.getLatestPrice().compareTo(m.getAveragePrice5())<0  //突破后回调到5日线附近
						&& m.getLatestPrice().compareTo(m.getAveragePrice21())>=0
						) {  
					ms.add(m);
				}
			}
		}

		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //Z-A
			}
		});
		
		StringBuffer sb = new StringBuffer(endDate.toString() + ":");
		for(int i=0; i<ms.size() && i<89; i++) {
			m = ms.get(i);
			sb.append(m.getItemID() + "(" + String.format("%04d",i) + ")" +",");
		}
		//logger.info(sb.toString());
		
		return sb.toString();
	}
}
