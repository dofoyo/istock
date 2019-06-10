package com.rhb.istock.trade.turtle.simulation.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
	
	@Cacheable("breakers")
	public Map<LocalDate, List<String>>  getBreakers(String type, String year){
		Map<LocalDate, List<String>> breakers = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_" + year + "_simulation_breakers.csv"; 
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
	
	@Cacheable("buys")
	public Map<LocalDate, List<String>>  getBuys(String type, String year){
		Map<LocalDate, List<String>> buys = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_" + year + "_simulation_detail.csv"; 
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
	public Map<LocalDate, List<String>>  getSells(String type, String year){
		Map<LocalDate, List<String>> sells = new TreeMap<LocalDate, List<String>>();
		
		String theFile = reportPath + "/" + type + "_" + year + "_simulation_detail.csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		LocalDate sellDate;
		List<String> ids;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			sellDate = LocalDate.parse(columns[8],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(sells.containsKey(sellDate)) {
				ids = sells.get(sellDate);
			}else {
				ids = new ArrayList<String>(); 
				sells.put(sellDate, ids);
			}
			ids.add(columns[1]);
		}		
		
		return sells;
	}
	
	@Cacheable("amounts")
	public Map<LocalDate, AmountEntity>  getAmounts(String type, String year){
		Map<LocalDate, AmountEntity> amounts = new TreeMap<LocalDate, AmountEntity>();
		
		String theFile = reportPath + "/" + type + "_" + year + "_simulation_dailyAmount.csv"; 
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
