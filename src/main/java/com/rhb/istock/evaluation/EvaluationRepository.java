package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("evaluationRepository")
public class EvaluationRepository {
	@Value("${evaluationPath}")
	private String evaluationPath;

	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	
	@Cacheable("busisDates")
	public List<LocalDate> getDates(boolean desc){
		TreeSet<LocalDate> dates = new TreeSet<LocalDate>();
		
		String theFile = evaluationPath + "/avb_simulation_dailyAmount_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate date;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			dates.add(date);
		}
		
		if(desc) {
			return new ArrayList<LocalDate>(dates.descendingSet());
		}
		
		return new ArrayList<LocalDate>(dates);
	}
	
	@Cacheable("busis")
	public Map<LocalDate, List<Busi>> getBusis(String type) {
		Map<LocalDate, List<Busi>> results = new HashMap<LocalDate, List<Busi>>();
		
		String itemID, itemName;
		LocalDate openDate;
		BigDecimal openPrice;
		LocalDate closeDate;
		BigDecimal closePrice;
		BigDecimal highestPrice;
		BigDecimal quantity;
		Busi busi;
		List<Busi> busis;
		
		String theFile = evaluationPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			itemID = columns[1];
			itemName = columns[2];
			openDate = LocalDate.parse(columns[3],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			openPrice = new BigDecimal(columns[4]);
			quantity = new BigDecimal(columns[5]);
			closeDate = LocalDate.parse(columns[8],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			closePrice = new BigDecimal(columns[9]);
			highestPrice = new BigDecimal(columns[16]);
			busi = new Busi(itemID, itemName, openDate, openPrice, quantity, closeDate, closePrice, highestPrice);
			
			busis = results.get(openDate);
			if(busis==null) {
				 busis = new ArrayList<Busi>();
				 results.put(openDate, busis);
			}
			busis.add(busi);
		}		
		
		return results;
	}
	
	@CacheEvict(value="busis",allEntries=true)
	public void evictBusisCache() {}

	@CacheEvict(value="busisDates",allEntries=true)
	public void evictBusisDatesCache() {}

}
