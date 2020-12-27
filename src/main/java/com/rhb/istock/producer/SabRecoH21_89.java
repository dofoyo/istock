package com.rhb.istock.producer;

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
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

/*
 * sab + 绝对高价
 * 
 */

@Service("sabRecoH21_89")
public class SabRecoH21_89 implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(SabRecoH21_89.class);
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "SabRecoH21_89.txt";
	private Integer pool = 21;
	private Integer period = 89;

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
		
		List<Muster> tmps;
		Muster muster;
		List<String> recommendations;

		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters!=null && musters.size()>0) {
/*			recommendations = finaService.getHighRecommendations(date, 10000, 13); //推荐买入
			tmps = new ArrayList<Muster>();
			for(String id : recommendations) {
				muster = musters.get(id);
				if(muster!=null) {
					tmps.add(muster);
				}
			}
			
			Collections.sort(tmps, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //价格大到小排序
				}
			});
			
			List<Muster> ms = tmps.subList(0, tmps.size()>=pool ? pool : tmps.size());    //最高价的前21只
*/	
			
			tmps = new ArrayList<Muster>(musters.values());
			Collections.sort(tmps, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //价格大到小排序
				}
			});
			
			recommendations = finaService.getHighRecommendations(date, 10000, 13); //推荐买入
			List<Muster> ms = new ArrayList<Muster>();
			for(int i=0; i<tmps.size() && i<pool; i++) {
				muster = tmps.get(i);
				if(muster!=null && recommendations.contains(muster.getItemID())) {
					ms.add(muster);
				}
			}

			
			for(Muster m : ms) {
				if(m!=null
						&& m.isDropLowest(period) 				//股价创新新低
						&& m.getN21Gap()<=-13
						) {
					breakers.add(m.getItemID());
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
