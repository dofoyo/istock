package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.muster.MusterEntity;

@Service("kdataRepositoryDzh")
public class KdataRepositoryDzh implements KdataRepository{
	@Value("${dzhKdataPath}")
	private String kdataPath;
	
	@Override
	@CacheEvict(value="dzhDailyKdatas",allEntries=true)
	public void evictKDataCache() {}
	
	@Override
	public KdataEntity getKdata(String itemID) {
		KdataEntity kdata = null;
		
		String file = this.kdataPath + "/" + itemID + ".txt";			
		String[] lines = FileTools.readTextFile(file).split("\n");
		
		Integer length = lines.length;
		
		String[] columns = lines[0].split("\t");
		
		kdata = new KdataEntity(itemID);
		
		LocalDate date;
		BigDecimal open;
		BigDecimal high;
		BigDecimal low;
		BigDecimal close;
		BigDecimal amount;
		BigDecimal quantity;
		
		for(int i=2; i<length; i++) {
			columns = lines[i].split("\t");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
			open = new BigDecimal(columns[1]);
			high = new BigDecimal(columns[2]);
			low = new BigDecimal(columns[3]);
			close = new BigDecimal(columns[4]);
			amount = new BigDecimal(columns[6]);
			quantity = new BigDecimal(columns[7]);

			kdata.addBar(date, open, high, low, close, amount,quantity);
		}
		return kdata;
	}

	@Override
	@Cacheable("dzhDailyKdatas")
	public KdataEntity getKdataByCache(String itemID) {
		return this.getKdata(itemID);
	}

	@Override
	public LocalDate getLastDate() {
		// TODO Auto-generated method stub
		return null;
	}


}
