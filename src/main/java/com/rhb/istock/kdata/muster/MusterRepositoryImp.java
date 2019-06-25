package com.rhb.istock.kdata.muster;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.muster.MusterEntity;

@Service("musterRepositoryImp")
public class MusterRepositoryImp implements MusterRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;

	@Value("${musterPath}")
	private String musterPath;
	
	@Value("${tmpMusterPath}")
	private String tmpMusterPath;
	
	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	
	@Override
	public Map<String, MusterEntity> getMusters(LocalDate date) {
		Map<String, MusterEntity> entities = new HashMap<String, MusterEntity>();
		
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + openDuration+"_" + dropDuration + "_musters.txt";
		//System.out.println(pathAndFile);
		
		File f = new File(pathAndFile);
		if(f.exists()) {
			String source = FileTools.readTextFile(pathAndFile);
			//System.out.println(source);
			String[] columns;
			String[] lines = source.split("\n");
			for(int i=1; i<lines.length; i++) {
				columns = lines[i].split(",");
				entities.put(columns[0], new MusterEntity(lines[i]));
			}			
		}
		
		return entities;
	}
	
	@Override
	public void saveMuster(LocalDate date, MusterEntity entity) {
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_" + openDuration+"_" + dropDuration + "_musters.txt";
		FileTools.writeTextFile(pathAndFile, entity.toText(), true);
	}


	@Override
	public boolean isMustersExist(LocalDate date) {
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_" + openDuration+"_" + dropDuration + "_musters.txt";
		File file = new File(pathAndFile);
		return file.exists();
	}

	@Override
	public void cleanTmpMusters() {
		try {
			FileUtils.cleanDirectory(new File(tmpMusterPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveTmpMuster(LocalDate date, MusterEntity entity) {
		String pathAndFile = tmpMusterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_" + openDuration+"_" + dropDuration + "_musters.txt";
		FileTools.writeTextFile(pathAndFile, entity.toText(), true);
	}

	@Override
	public void copyTmpMusters() {
		try {
			FileUtils.copyDirectory(new File(tmpMusterPath), new File(musterPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveMusters(LocalDate date,List<MusterEntity> musterEntities) {
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_" + openDuration+"_" + dropDuration + "_musters.txt";
		
		FileUtils.deleteQuietly(new File(pathAndFile));
		
		for(MusterEntity entity : musterEntities) {
			this.saveMuster(date, entity);
		}
		
	}

	@Override
	public List<LocalDate> getMusterDates(LocalDate beginDate, LocalDate endDate) {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		LocalDate date;
		List<File> files = FileTools.getFiles(musterPath, null, true);
		for(File file : files) {
			date = LocalDate.parse(file.getName().substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
			if(beginDate==null && endDate==null) {
				dates.add(date);
			}else if(beginDate==null && endDate!=null && (date.isBefore(endDate) || date.equals(endDate))){
				dates.add(date);
			}else if(beginDate!=null && endDate==null && (date.isAfter(beginDate) || date.equals(beginDate))){
				dates.add(date);
			}else if((date.isAfter(beginDate) || date.equals(beginDate)) && (date.isBefore(endDate) || date.equals(endDate))){
				dates.add(date);
			}
			
			//System.out.println(date);
		}
		
		Collections.sort(dates, new Comparator<LocalDate>() {
			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				return o1.compareTo(o2);
			}
		});
		return dates;
	}

}
