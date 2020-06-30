package com.rhb.istock.selector.b21;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("b21Service")
public class B21Service {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Value("${b21File}")
	private String b21File;	
	
	protected static final Logger logger = LoggerFactory.getLogger(B21Service.class);

	public String getB21() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateB21(date);
	}
	
	public Map<String,String> isB21(List<String> itemIDs, LocalDate endDate) {
		Map<String,String> results = new HashMap<String,String>();

		Integer previous_period = 8;
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Map<String,Muster> previous = kdataService.getPreviousMusters(previous_period, endDate);
		if(musters!=null && previous!=null) {
			Muster m, p;
			String v;
			for(String id : itemIDs) {
				m = musters.get(id);
				p = previous.get(id);
				v = "0";
				if(m!=null && p!=null
						&& m.isJustBreaker(8)  //刚刚突破21日线，同时21日线在89日线上放不超过8%
						) {
					
					if(m.getAverageGap()<8 //均线在8%范围内纠缠
						|| m.getAveragePrice21().compareTo(p.getAveragePrice21())==1 //上升趋势
						|| m.getAverageAmount().compareTo(p.getAverageAmount())==1 // 放量
						) {
						
							v = "2";
					}
				}else if(m!=null && m.isDropAve(21)) {
					v = "-2";
				}
				
				results.put(id, v);
			}
		}
		
		return results;
	}
	
	public void generateB21(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateB21(date));
		}
		FileTools.writeTextFile(b21File, sb.toString(), false);
	}
	
	private String generateB21(LocalDate endDate) {
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
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //价格小到大排序
					}
				});
				
				Muster m,p;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(m!=null && p!=null
							&& m.isJustBreaker(8)  //刚刚突破21日线，同时21日线在89日线上放不超过8%
							) {
						
						if(m.getAverageGap()<8 //均线在8%范围内纠缠
							|| m.getAveragePrice21().compareTo(p.getAveragePrice21())==1 //上升趋势
							|| m.getAverageAmount().compareTo(p.getAverageAmount())==1 // 放量
							) {
								sb.append(m.getItemID() + "(" + String.format("%04d",i) + ")" +",");
							}
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
