package com.rhb.istock.selector.aat.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("averageAmountTopRepositoryImp")
public class AverageAmountTopRepositoryImp implements AverageAmountTopRepository {
	@Value("${averageAmountTopsFile}")
	private String averageAmountTopsFile;
	
	@Override
	@Cacheable("averageAmountTops")
	public Map<LocalDate, List<String>> getAverageAmountTops() {
		Map<LocalDate, List<String>> tops = new HashMap<LocalDate, List<String>>();
		
		String[] lines = FileUtil.readTextFile(averageAmountTopsFile).split("\n");
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
		}		
		
		return tops;
	}

}