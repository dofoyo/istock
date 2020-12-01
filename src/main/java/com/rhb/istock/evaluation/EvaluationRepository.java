package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
	
	@Cacheable("busis")
	public List<Busi> getBusis(String type) {
		List<Busi> busis = new ArrayList<Busi>();

		LocalDate openDate;
		BigDecimal openPrice;
		BigDecimal highestPrice;
		BigDecimal closePrice;
		Busi busi;
		
		String theFile = evaluationPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 
		//System.out.println(theFile);
		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			openDate = LocalDate.parse(columns[3],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			openPrice = new BigDecimal(columns[4]);
			highestPrice = new BigDecimal(columns[16]);
			closePrice = new BigDecimal(columns[9]);
			busi = new Busi(openDate, openPrice, highestPrice, closePrice);
			busis.add(busi);
		}		
		
		return busis;
	}
	
	@CacheEvict(value="busis",allEntries=true)
	public void evictBusisCache() {}

}
