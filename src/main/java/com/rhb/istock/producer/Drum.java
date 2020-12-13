package com.rhb.istock.producer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

/*
 * 强于大盘
 * 
 */

@Service("drum")
public class Drum implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Drum.class);
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;
	
	@Value("${operationsPath}")
	private String operationsPath;

	private String fileName  = "Drum.txt";

	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> breakers;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date = bDate; (date.isBefore(eDate) || date.equals(eDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, fileName + ", " + date.toString());
			breakers = this.produce(date,false);
			if(breakers!=null && breakers.size()>0) {
				results.put(date, breakers);
			}
		}
		
		FileTools.writeMapFile(this.getFileName(), results, false);
		
		return results;
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		LocalDate date;
		for(Map.Entry<LocalDate, List<String>> entry : all.entrySet()) {
			date = entry.getKey();
			if((date.isAfter(bDate) || date.equals(bDate))
					&& (date.isBefore(eDate) || date.equals(eDate))) {
				results.put(date, entry.getValue());
			}
		}
		
		return results;
	}
	
	private String getFileName() {
		return operationsPath + fileName;
	}
	
	private Integer[] getRatio(List<Map<String,Muster>> musters, String itemID, BigDecimal price) {
		Integer[] ratio = new Integer[] {0,0};
		BigDecimal lowest=null, begin=null;
		Muster m;
		for(Map<String,Muster> ms : musters) {
			m = ms.get(itemID);
			if(m!=null) {
				lowest = (lowest==null || lowest.compareTo(m.getLatestPrice())==1) ? m.getLatestPrice() : lowest;
				//System.out.println(String.format("%s, date=%s, price=%.2f", itemID, m.getDate().toString(), m.getLatestPrice()));
				if(begin==null) {
					begin = m.getLatestPrice();
				}
			}
		}
		
		if(lowest!=null && begin!=null && lowest.compareTo(BigDecimal.ZERO)>0) {
			ratio[0] = Functions.growthRate(price, begin);
			ratio[1] = Functions.growthRate(price, lowest);
		}
		
		//System.out.println(String.format("%s, begin=%.2f, lowest=%.2f, price=%.2f, ratio[0]=%d, ratio[1]=%d", itemID, begin,lowest, price,ratio[0],ratio[1]));

		return ratio;
	}


	@Override
	public List<String> getResults(LocalDate date) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		if(all.get(date)!=null) {
			return all.get(date);
		}else {
			return this.produce(date, false);
		}
	}

	@Override
	public List<String> produce(LocalDate date, boolean write) {
		List<String> breakers = new ArrayList<String>();
		Map<String,Muster> musters;
		musters = kdataService.getMusters(date);
		if(musters!=null && musters.size()>0) {
			List<Muster> tmps = new ArrayList<Muster>(musters.values());
			Collections.sort(tmps, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					if(o1.getHLGap().compareTo(o2.getHLGap())==0){            //横盘波动
						return o1.getLNGap().compareTo(o2.getLNGap());
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());
					}
				}
			});
			
			Muster p;
			Integer[] ratio;
			Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
			List<Map<String,Muster>> previous = kdataService.getPreviousMusters(previous_period, date);
			Integer[] sseiRatio = indexServiceTushare.getSseiGrowthRate(date, 21);
			for(Muster m : tmps) {
				p = previous.get(0).get(m.getItemID());
				if(m!=null && p!=null) {
					ratio = this.getRatio(previous,m.getItemID(),m.getLatestPrice());
					//System.out.format("%s,%s,sseiRatio[0]=%d, sseiRatio[1]=%d,ratio[0]=%d,ratio[1]=%d\n",date.toString(),m.getItemID(),sseiRatio[0],sseiRatio[1],ratio[0],ratio[1]);
					if(ratio[0] > 0
							&& (ratio[0]>=sseiRatio[0] || ratio[1]>=sseiRatio[1])   // 强于大盘
							//&& m.getHLGap()<=55
							&& m.isUpAve(21)
							&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1  //上升趋势
							) {
						breakers.add(m.getItemID());
					}
				}
			}
			
			if(breakers.size()>0 && write) {
				Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
				results.put(date, breakers);
				FileTools.writeMapFile(this.getFileName(), results, true);
			}
			
		}
		
		return breakers;
	}
}
