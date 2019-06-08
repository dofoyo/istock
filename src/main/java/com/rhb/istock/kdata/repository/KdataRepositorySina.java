package com.rhb.istock.kdata.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("kdataRepositorySina")
public class KdataRepositorySina implements KdataRepository{
	@Value("${sinaKdataPath}")
	private String kdataPath;
	
	@Override
	@CacheEvict(value="sinaDailyKdatas",allEntries=true)
	public void evictDailyKDataCache() {}
	
	@Override
	public KdataEntity getDailyKdata(String itemID) {
		KdataEntity kdata = new KdataEntity(itemID);
		
		File dir = new File(this.kdataPath);
		FileFilter fileFilter = new WildcardFileFilter(itemID + "*.txt");
		File[] files = dir.listFiles(fileFilter);
		List<Map<String,String>> bars;
		for (int i = 0; i < files.length; i++) {
			bars = toBars(files[i]);
			for(Map<String,String> bar : bars) {
				kdata.addBar(bar.get("date"), bar.get("open"), bar.get("high"), bar.get("low"), bar.get("close"), bar.get("amount"),bar.get("quantity"));
			}
		}
		
		//System.out.println(kdata);
		
		return kdata;
	}
	
	private List<Map<String,String>> toBars(File file){
		List<Map<String,String>> bars = new ArrayList<Map<String,String>>();
		List<String> lines;
		try {
			lines = FileUtils.readLines(file, "UTF-8");
			Integer length = lines.size();
			for(int i=length-1; i>0; i--) {
				bars.add(toBar(lines.get(i)));
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bars;
	}
	
	private Map<String, String> toBar(String line){
		String[] columns = line.split(",");
		
		Map<String,String> bar = new HashMap<String,String>();
		BigDecimal open = new BigDecimal(columns[1]);
		BigDecimal high = new BigDecimal(columns[2]);
		BigDecimal low = new BigDecimal(columns[4]);
		BigDecimal close = new BigDecimal(columns[3]);
		BigDecimal factor = new BigDecimal(columns[7]);
		String amount = columns[6];
		String quantity = columns[5];
		
		bar.put("date", columns[0]);
		bar.put("open", open.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		bar.put("high", high.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		bar.put("low", low.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		bar.put("close", close.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		bar.put("amount", amount);
		bar.put("quantity", quantity);
			
		return bar;
	}

	@Override
	@Cacheable("sinaDailyKdatas")
	public KdataEntity getDailyKdataByCache(String itemID) {
		return this.getDailyKdata(itemID);
	}

	@Override
	public LocalDate getLatestDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalDate getLatestMusterDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<KdataMusterEntity> getKdataMusters() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void saveLatestMusters(LocalDate date, List<KdataMusterEntity> entities, Integer period) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evictKdataMustersCache() {
		// TODO Auto-generated method stub
		
	}


}
