package com.rhb.istock.trade.kelly.repository;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("kellyRepository")
public class KellyRepository {
	@Value("${fvaluesOfHLFile}")
	private String fvaluesOfHLFile;
	private DecimalFormat df = new DecimalFormat("0.00");

	public void saveFvaluesOfHL(TreeMap<LocalDate, BigDecimal> value) {
		this.save(value, fvaluesOfHLFile);
	}
	
	private void save(TreeMap<LocalDate, BigDecimal> values, String fileName) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<LocalDate, BigDecimal> entry : values.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
			sb.append(df.format(entry.getValue()));
			sb.append("\n");
		}
		FileUtil.writeTextFile(fileName, sb.toString(), false);
		
	}
	

	@Cacheable("fvalueOfHL")
	public TreeMap<LocalDate, BigDecimal> getFvaluesOfHL() {
		return this.getValues(fvaluesOfHLFile);
	}	
	
	private TreeMap<LocalDate, BigDecimal> getValues(String fileName) {
		TreeMap<LocalDate, BigDecimal> values = new TreeMap<LocalDate,BigDecimal>();
		
		String[] lines = FileUtil.readTextFile(fileName).split("\n");
		String[] columns;
		LocalDate date;
		BigDecimal value;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			value = new BigDecimal(columns[1]);
			values.put(date, value);
		}		
		
		return values;
	} 

}
