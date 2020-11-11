package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.ArrayList;
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
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.favor.FavorService;

/*
 * 低价 + 横盘 + 向上破21日线
 * 
 * 股价由高到低排序 + 前21只  + 股价创新高
 */

@Service("b21Favor")
public class B21Favor implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(B21Favor.class);
	
	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;

	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "B21Favor.txt";

	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> breakers;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date = bDate; (date.isBefore(eDate) || date.equals(eDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, fileName + ", " + date.toString());
			breakers = this.getResults(date);
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
		
		List<Muster> musters = favorService.getFavors(date);
		
		if(musters!=null && musters.size()>0) {
			for(Muster m : musters) {
				if(m!=null
						&& m.isJustBreaker() 				//股价破21日线
						&& m.getHLGap()<=55             //涨幅不大
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
