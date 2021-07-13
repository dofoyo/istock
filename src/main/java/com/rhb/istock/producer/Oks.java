package com.rhb.istock.producer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.tushare.FdataRepositoryTushare;
import com.rhb.istock.fdata.tushare.FinaIndicator;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.fina.FinaService;

/*
 * 年报业绩优秀
 * 
 */

@Service("oks")
public class Oks implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Oks.class);

	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "Oks.txt";

	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> ids;
		List<String> buys = new ArrayList<String>();
		
		List<String> itemIDs = itemService.getItemIDs();
		Item item;
/*		List<String> itemIDs = new ArrayList<String>();
		itemIDs.add("sz001206");
*/		Map<String,FinaIndicator> indicators;
		FinaIndicator fi;
		LocalDate date, ipoDate;
		String theID=null;
		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(), i++, id);
			indicators = fdataRepositoryTushare.getIndicators(id, LocalDate.now());
			for(Map.Entry<String, FinaIndicator> entry : indicators.entrySet()) {
				fi = entry.getValue();
				//System.out.println(fi);
				if(fi.isOK() 
						//&& !buys.contains(id)
						) {
					buys.add(id);
					theID = id + "B";
					//System.out.println(" isok = true");
				}
				
				if(!fi.isGood() && buys.contains(id)) {
					buys.remove(id);
					
					//System.out.println(buys.size());
					theID = id + "S";
					//System.out.println(!fi.isGood() && buys.contains(id) ? "!fi.isGood() && buys.contains(id)=true"  : "!fi.isGood() && buys.contains(id)=false");
				}
				
				if(theID!=null) {
					date = LocalDate.parse(fi.getAnn_date(), DateTimeFormatter.ofPattern("yyyyMMdd"));
					item = itemService.getItem(fi.getItemID());
					ipoDate = LocalDate.parse(item.getIpo(), DateTimeFormatter.ofPattern("yyyyMMdd"));
					if(date.isAfter(ipoDate)) {
						ids = results.get(date);
						if(ids==null) {
							ids = new ArrayList<String>();
							results.put(date, ids);
						}
						
						ids.add(theID);
					}
					
					//System.out.println(" add " + theID);
				}
				theID = null;
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
		if(write) {
			Map<LocalDate, List<String>> result = this.produce(null, null);
			return result.get(date);
		}else {
			return this.getResults(date);
		}
		//return this.produce(null, null);
	}
}
