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
import com.rhb.istock.comm.util.Functions;
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

	public Map<String,String> isB21(List<String> itemIDs, LocalDate endDate) {
		Map<String,String> results = new HashMap<String,String>();
		Integer previous_period = 13;
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		List<Map<String,Muster>> previous = kdataService.getPreviousMusters(previous_period, endDate);
		Integer sseiRatio = kdataService.getSseiRatio(endDate, previous_period);
		Integer ratio;
		if(musters!=null && previous!=null) {
			Muster m, p;
			String v;
			for(String id : itemIDs) {
				m = musters.get(id);
				p = previous.get(0).get(id);
				v = "0";
				if(m!=null && p!=null) {
					ratio = this.getRatio(previous, m.getItemID(), m.getLatestPrice()); 
					if(m.isDropAve(21) || ratio < sseiRatio) {  // 弱于大盘
 						v = "-2";
					}

					if(m.isJustBreaker() 
							&& m.getHLGap()<=55 //股价还没飞涨
							&& (m.getAverageGap()<8  //均线在8%范围内纠缠
									|| m.getAveragePrice21().compareTo(p.getAveragePrice21())==1  //上升趋势
									|| m.getAverageAmount().compareTo(p.getAverageAmount())==1)  // 放量
							) {
						v = "1";
					}else if(m.isUpBreaker()) {  //创新高
						v = "3";
					}else if(ratio >= sseiRatio   // 强于大盘
							&& m.isUpAve(21)
							//&& m.getHLGap()<=55
							&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1 ) { //上升趋势
						v = "2";
					}
						
					
					results.put(id, v);
				}
			}
		}
		
		return results;
	}
	
	private Integer getRatio(List<Map<String,Muster>> musters, String itemID, BigDecimal price) {
		Integer ratio = 0;
		BigDecimal lowest=null;
		Muster m;
		for(Map<String,Muster> ms : musters) {
			m = ms.get(itemID);
			if(m!=null) {
				lowest = (lowest==null || lowest.compareTo(m.getLatestPrice())==1) ? m.getLatestPrice() : lowest;
			}
		}
		
		if(lowest==null || lowest.compareTo(BigDecimal.ZERO)==0) {
			lowest = price;
		}
		ratio = Functions.growthRate(price, lowest);
		//logger.info(String.format("%s, lowest=%.2f, highest=%.2f, ratio=%d", itemID, lowest, price,ratio));
		return ratio;
	}
}
