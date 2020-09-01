package com.rhb.istock.trade.turtle.simulation.six.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("turtleSimulationRepository")
public class TurtleSimulationRepository {
	@Value("${reportPath}")
	private String reportPath;
	
	@Value("${dailyMeansFile}")
	private String dailyMeansFile;
	
	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	
	public TreeMap<String,Map<String,String>> getDailyMeans(){
		TreeMap<String,Map<String,String>> means = new TreeMap<String,Map<String,String>>();
		
		String[] lines = FileTools.readTextFile(dailyMeansFile).split("\n");
		String[] columns;
		String date;
		Map<String,String> values;
		for(int i=1; i<lines.length; i++) {
			columns = lines[i].split(",");
			
			date = columns[0];
			
			values = new HashMap<String,String>();
			values.put("bhl", columns[1]);
			values.put("bav", columns[2]);
			values.put("bdt", columns[3]);
			values.put("hlb", columns[4]);
			values.put("avb", columns[5]);
			values.put("dtb", columns[6]);
			
			means.put(date, values);
		}		
		
		return means;

	}
	
	public void saveDailyMeans(Map<LocalDate,Map<String,Integer>> dailyMeans) {
		StringBuffer sb = new StringBuffer("date,bhl,bav,bdt,hlb,avb,dtb\n");
		for(Map.Entry<LocalDate, Map<String,Integer>> entry : dailyMeans.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
			
			sb.append(entry.getValue().get("bhl") + ",");
			sb.append(entry.getValue().get("bav") + ",");
			sb.append(entry.getValue().get("bdt") + ",");
			sb.append(entry.getValue().get("hlb") + ",");
			sb.append(entry.getValue().get("avb") + ",");
			sb.append(entry.getValue().get("dtb") + "\n");
		}
		FileTools.writeTextFile(dailyMeansFile, sb.toString(), false);
	}
	
	public void save(String type, String breakers, String details, String dailyAmounts) {
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_breakers_"+openDuration+"_"+dropDuration+".csv", breakers, false);
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv", details, false);
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_dailyAmount_"+openDuration+"_"+dropDuration+".csv", dailyAmounts, false);
	}
	
	@Cacheable("breakers")
	public Map<LocalDate, List<String>>  getBreakers(String type){
		Map<LocalDate, List<String>> breakers = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_simulation_breakers_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate date;
		List<String> ids;
		for(String line : lines) {
			//System.out.println(line);
			if(line.length()>0 && line.contains(",")) {
				columns = line.split(",");
				date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				ids = new ArrayList<String>();
				for(int i=1; i<columns.length; i++) {
					ids.add(columns[i]);
				}
				breakers.put(date, ids);
			}
		}		
		
		return breakers;
	}
	
	public Map<String, String>  getBuys(String itemID,String type){
		Map<String, String> buys = new HashMap<String, String>();
		
		if("bhl".equals(type)) {
			buys.putAll(this.getAllBuysOfType("bhl", itemID));
		}else if("bav".equals(type)) {
			buys.putAll(this.getAllBuysOfType("bav", itemID));
		}else if("bdt".equals(type)) {
			buys.putAll(this.getAllBuysOfType("bdt", itemID));
		}else if("hlb".equals(type)) {
			buys.putAll(this.getAllBuysOfType("hlb", itemID));
		}else if("avb".equals(type)) {
			buys.putAll(this.getAllBuysOfType("avb", itemID));
		}else if("dtb".equals(type)) {
			buys.putAll(this.getAllBuysOfType("dtb", itemID));
		}
		
		//System.out.println(type);
		//System.out.println(buys);
		
		return buys;
	}
	
	private Map<String, String> getAllBuysOfType(String type, String itemID){
		Map<String, String> buys = new HashMap<String, String>();
		String theFile = reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		for(int j=1; j<lines.length; j++) {
			columns = lines[j].split(",");
			if(columns.length>4 && columns[1].equals(itemID)) {
				buys.put(columns[3], columns[4]);
			}
		}		
		return buys;
	}
	
	public Map<String, String>  getSells(String itemID, String type){
		Map<String, String> sells = new HashMap<String, String>();

		if("bhl".equals(type)) {
			sells.putAll(this.getAllSellsOfType("bhl", itemID));
		}else if("bav".equals(type)) {
			sells.putAll(this.getAllSellsOfType("bav", itemID));
		}else if("bdt".equals(type)) {
			sells.putAll(this.getAllSellsOfType("bdt", itemID));
		}else if("hlb".equals(type)) {
			sells.putAll(this.getAllSellsOfType("hlb", itemID));
		}else if("avb".equals(type)) {
			sells.putAll(this.getAllSellsOfType("avb", itemID));
		}else if("dtb".equals(type)) {
			sells.putAll(this.getAllSellsOfType("dtb", itemID));
		}
		
		//System.out.println(type);
		//System.out.println(sells);
		
		return sells;
	}
	
	private Map<String, String> getAllSellsOfType(String type, String itemID){
		Map<String, String> sells = new HashMap<String, String>();
		String theFile = reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		for(int j=1; j<lines.length; j++) {
			columns = lines[j].split(",");
			if(columns.length>9 && columns[1].equals(itemID) && !"hold".equals(columns[11])) {
				sells.put(columns[8], columns[9]);
			}
		}		
		return sells;
	}
	
	@Cacheable("buys")
	public TreeMap<LocalDate, List<String>>  getBuys(String type){
		TreeMap<LocalDate, List<String>> buys = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate buyDate;
		List<String> ids ;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			buyDate = LocalDate.parse(columns[3],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(buys.containsKey(buyDate)) {
				ids = buys.get(buyDate);
			}else {
				ids = new ArrayList<String>(); 
				buys.put(buyDate, ids);
			}
			ids.add(columns[1] + "," + columns[2] + "," + columns[12] + "," + columns[3]);
		}		
		
		return buys;
	}

	@Cacheable("sells")
	public TreeMap<LocalDate, List<String>>  getSells(String type){
		TreeMap<LocalDate, List<String>> sells = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate sellDate;
		List<String> ids;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			sellDate = LocalDate.parse(columns[8],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(!"hold".equals(columns[11])) {
				if(sells.containsKey(sellDate)) {
					ids = sells.get(sellDate);
				}else {
					ids = new ArrayList<String>(); 
					sells.put(sellDate, ids);
				}
				ids.add(columns[1]);
			}
		}		
		
		return sells;
	}
	
	@Cacheable("amounts")
	public TreeMap<LocalDate, AmountEntity>  getAmounts(String type){
		TreeMap<LocalDate, AmountEntity> amounts = new TreeMap<LocalDate, AmountEntity>();
		
		String theFile = reportPath + "/" + type + "_simulation_dailyAmount_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate date;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			amounts.put(date,new AmountEntity(columns[1],columns[2]));
		}		
		return amounts;
	}
	
	@CacheEvict(value="amounts",allEntries=true)
	public void evictAmountsCache() {}

	@CacheEvict(value="sells",allEntries=true)
	public void evictSellsCache() {}

	@CacheEvict(value="buys",allEntries=true)
	public void evictBuysCache() {}

	@CacheEvict(value="breakers",allEntries=true)
	public void evictBreakersCache() {}
	
}
