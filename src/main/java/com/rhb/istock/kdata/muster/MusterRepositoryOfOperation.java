package com.rhb.istock.kdata.muster;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.muster.MusterEntity;

@Service("musterRepositoryOfOperation")
public class MusterRepositoryOfOperation implements MusterRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;

	@Value("${operationMusterPath}")
	private String musterPath;
	
	//private Map<String,KdataEntity> kdatas = new HashMap<String,KdataEntity>();;

	@Override
	public LocalDate getLastMusterDate() {
		String source = FileTools.readTextFile(musterPath);
		
		if(source==null || source.isEmpty()) {
			System.err.println("can NOT find " + musterPath + "! or the file is empty!");
			return null;
		}

		String[] lines = source.split("\n");
		
		return LocalDate.parse(lines[0].split(",")[0]);
	}

	@Override
	//@Cacheable("musters")
	public List<MusterEntity> getMusters(LocalDate date) {
		List<MusterEntity> entities = new ArrayList<MusterEntity>();
		
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
		//System.out.println(pathAndFile);
		String source = FileTools.readTextFile(pathAndFile);
		//System.out.println(source);
		String[] lines = source.split("\n");
		for(int i=1; i<lines.length; i++) {
			entities.add(new MusterEntity(lines[i]));
		}
		
		return entities;
	}
	
	@Override
	@CacheEvict(value="musters",allEntries=true)
	public void evictMustersCache() {}
	

	@Override
	public void saveMusters(LocalDate date, List<MusterEntity> entities, Integer openPeriod, Integer dropPeriod) {
		StringBuffer sb = new StringBuffer(date.toString() + "," + openPeriod + "\n");
		for(MusterEntity entity : entities) {
			sb.append(entity.toText());
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		
		FileTools.writeTextFile(musterPath, sb.toString(), false);
	}
	
	@Override
	public void saveMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod) {
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
		FileTools.writeTextFile(pathAndFile, entity.toText(), true);
	}


	@Override
	public boolean isMustersExist(LocalDate date) {
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
		File file = new File(pathAndFile);
		return file.exists();
	}

	@Override
	public void cleanMusters() {
		
	}

}
