package com.rhb.istock.kdata.muster;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
	
	@Override
	public List<MusterEntity> getMusters(LocalDate date) {
		List<MusterEntity> entities = new ArrayList<MusterEntity>();
		
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
		//System.out.println(pathAndFile);
		
		File f = new File(pathAndFile);
		if(f.exists()) {
			String source = FileTools.readTextFile(pathAndFile);
			//System.out.println(source);
			String[] lines = source.split("\n");
			for(int i=1; i<lines.length; i++) {
				entities.add(new MusterEntity(lines[i]));
			}			
		}
		
		return entities;
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
	public void cleanTmpMusters() {
		try {
			FileUtils.cleanDirectory(new File(tmpMusterPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveTmpMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod) {
		String pathAndFile = tmpMusterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
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

}
