package com.rhb.istock.kdata.repository;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.ParseString;
import com.rhb.istock.kdata.muster.MusterEntity;

@Service("kdataRepository163")
public class KdataRepository163 implements KdataRepository {
	@Value("${kdataPath163}")
	private String kdataPath;
	
	@Override
	@Cacheable("dailyKdata163")
	public KdataEntity getKdataByCache(String itemID) {
		return this.getKdata(itemID);
	}

	@Override
	public KdataEntity getKdata(String itemID) {
		KdataEntity kdata = new KdataEntity(itemID);

		String kdataFile = kdataPath + "/" + itemID + ".csv";
		
		File file = new File(kdataFile);
		if(!file.exists()){
			System.out.println(kdataFile + "  do NOT exist, download ....");
			return null;
		}
		
		LocalDate date;
		BigDecimal open,high,low,close,amount,quantity;
		String[] columns = null;
		String[] lines = FileTools.readTextFile(kdataFile).split("\n");
		for(int i=lines.length-1; i>0; i--){  //文档是倒序，要变成顺序,排除第一行
			columns = lines[i].split(","); 
			
			//System.out.println(lines[i]);
			
			date = ParseString.toLocalDate(columns[0]);
			close = ParseString.toBigDecimal(columns[3]);
			high = ParseString.toBigDecimal(columns[4]);
			low = ParseString.toBigDecimal(columns[5]);
			open = ParseString.toBigDecimal(columns[6]);
			quantity = ParseString.toBigDecimal(columns[10]);
			amount = ParseString.toBigDecimal(columns[11]);

			
			
			kdata.addBar(date,open,high,low,close,amount,quantity);

		}

		return kdata;
	}

	@Override
	@CacheEvict(value="dailyKdata163",allEntries=true)
	public void evictKDataCache() {}

	@Override
	public LocalDate getLastDate() {
		return this.getKdataByCache("sh000001").getLastDate();
	}


}
