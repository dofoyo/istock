package com.rhb.istock.trade.twin.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.trade.twin.Wfeature;

@Service("twinRepositoryImp")
public class TwinRepositoryImp implements TwinRepository {
	@Value("${twinOpenListFile}")
	private String twinOpenListFile;

	@Value("${twinDropListFile}")
	private String twinDropListFile;
	
	
	@Override
	public void saveOpens(TreeMap<LocalDate, TreeSet<Wfeature>> result) {
		write(twinOpenListFile, result);
	}

	@Override
	public void saveDrops(TreeMap<LocalDate, TreeSet<Wfeature>> result) {
		write(twinDropListFile, result);
	}
	
	@Override
	@Cacheable("openList")
	public TreeMap<LocalDate,List<String>> getOpens() {
		return getList(twinOpenListFile);
	}

	@Override
	@Cacheable("dropList")
	public TreeMap<LocalDate,List<String>> getDrops() {
		return getList(twinDropListFile);
	}
	
	private void write(String file, TreeMap<LocalDate, TreeSet<Wfeature>> results) {
		StringBuffer sb = new StringBuffer();
		TreeSet<Wfeature> values;
		for(Map.Entry<LocalDate, TreeSet<Wfeature>> entry : results.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
			
			values = entry.getValue();
			
			for(Wfeature value : values) {
				sb.append(value.getItemID());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
	
		FileUtil.writeTextFile(file, sb.toString(), false);
		
	}
	
	private TreeMap<LocalDate, List<String>> getList(String file){
		TreeMap<LocalDate, List<String>> tops = new TreeMap<LocalDate, List<String>>();
		
		String[] lines = FileUtil.readTextFile(file).split("\n");
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
			tops.put(date, ids);
			//System.out.println(date + "," + ids.size());
		}
		
		return tops;
	}

}
