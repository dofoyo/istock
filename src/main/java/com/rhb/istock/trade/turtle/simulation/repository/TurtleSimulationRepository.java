package com.rhb.istock.trade.turtle.simulation.repository;

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
	
	public void save(String type, String breakers, String details, String dailyAmounts) {
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_breakers.csv", breakers, false);
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_detail.csv", details, false);
		FileTools.writeTextFile(reportPath + "/" + type + "_simulation_dailyAmount.csv", dailyAmounts, false);
	}
	
	@Cacheable("breakers")
	public Map<LocalDate, List<String>>  getBreakers(String type){
		Map<LocalDate, List<String>> breakers = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_simulation_breakers.csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate date;
		List<String> ids;
		for(String line : lines) {
			//System.out.println(line);
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			ids = new ArrayList<String>();
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			breakers.put(date, ids);
		}		
		
		return breakers;
	}
	
	public Map<String, String>  getAllBuys(String itemID){
		Map<String, String> buys = new HashMap<String, String>();
		
		buys.putAll(this.getAllBuysOfType("bhl", itemID));
		buys.putAll(this.getAllBuysOfType("bav", itemID));
		buys.putAll(this.getAllBuysOfType("bdt", itemID));
		buys.putAll(this.getAllBuysOfType("hlb", itemID));
		buys.putAll(this.getAllBuysOfType("avb", itemID));
		buys.putAll(this.getAllBuysOfType("dtb", itemID));
		
		return buys;
	}
	
	private Map<String, String> getAllBuysOfType(String type, String itemID){
		Map<String, String> buys = new HashMap<String, String>();
		String theFile = reportPath + "/" + type + "_simulation_detail.csv"; 
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
	
	public Map<String, String>  getAllSells(String itemID){
		Map<String, String> sells = new HashMap<String, String>();
		
		sells.putAll(this.getAllSellsOfType("bhl", itemID));
		sells.putAll(this.getAllSellsOfType("bav", itemID));
		sells.putAll(this.getAllSellsOfType("bdt", itemID));
		sells.putAll(this.getAllSellsOfType("hlb", itemID));
		sells.putAll(this.getAllSellsOfType("avb", itemID));
		sells.putAll(this.getAllSellsOfType("dtb", itemID));
		
		return sells;
	}
	
	private Map<String, String> getAllSellsOfType(String type, String itemID){
		Map<String, String> sells = new HashMap<String, String>();
		String theFile = reportPath + "/" + type + "_simulation_detail.csv"; 
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
		
		String theFile = reportPath + "/" + type + "_simulation_detail.csv"; 
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
			ids.add(columns[1]);
		}		
		
		return buys;
	}

	@Cacheable("sells")
	public Map<LocalDate, List<String>>  getSells(String type){
		Map<LocalDate, List<String>> sells = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_simulation_detail.csv"; 
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
		
		String theFile = reportPath + "/" + type + "_simulation_dailyAmount.csv"; 
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
