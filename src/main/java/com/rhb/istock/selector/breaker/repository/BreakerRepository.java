package com.rhb.istock.selector.breaker.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("breakerRepository")
public class BreakerRepository {
	@Value("${breakersFile}")
	private String breakersFile;

	@Value("${breakersSortByHLFile}")
	private String breakersSortByHLFile;

	@Value("${breakersSortByDTFile}")
	private String breakersSortByDTFile;

	@Value("${breakersSortByAVFile}")
	private String breakersSortByAVFile;
	
	
	public void saveBreakersSortByAV(TreeMap<LocalDate, List<String>> breakers) {
		this.save(breakers, breakersSortByAVFile);
	}
	
	public void saveBreakersSortByDT(TreeMap<LocalDate, List<String>> breakers) {
		this.save(breakers, breakersSortByDTFile);
	}
	
	public void saveBreakersSortByHL(TreeMap<LocalDate, List<String>> breakers) {
		this.save(breakers, breakersSortByHLFile);
	}
	
	public void saveBreakers(TreeMap<LocalDate, List<String>> breakers) {
		this.save(breakers, breakersFile);
	}
	
	private void save(TreeMap<LocalDate, List<String>> ids, String fileName) {
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : ids.keySet()) {
			sb.append(date);
			sb.append(",");
			for(String id : ids.get(date)) {
				sb.append(id);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		FileUtil.writeTextFile(fileName, sb.toString(), false);
		
	}
	
	@Cacheable("breaks")
	public TreeMap<LocalDate, List<String>> getBreakerIDs() {
		return this.getBreakerIDs(breakersFile);
	}
	
	@Cacheable("breaksSortByAV")
	public TreeMap<LocalDate, List<String>> getBreakersSortByAV() {
		return this.getBreakerIDs(breakersSortByAVFile);
	}	
	
	@Cacheable("breaksSortByHL")
	public TreeMap<LocalDate, List<String>> getBreakersSortByHL() {
		return this.getBreakerIDs(breakersSortByHLFile);
	}	
	
	@Cacheable("breaksSortByDT")
	public TreeMap<LocalDate, List<String>> getBreakersSortByDT() {
		return this.getBreakerIDs(breakersSortByDTFile);
	}	
	
	private TreeMap<LocalDate, List<String>> getBreakerIDs(String fileName) {
		TreeMap<LocalDate, List<String>> breakers = new TreeMap<LocalDate, List<String>>();
		
		String[] lines = FileUtil.readTextFile(fileName).split("\n");
		String[] columns;
		LocalDate date;
		List<String> ids;
		for(String line : lines) {
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
}
