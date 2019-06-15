package com.rhb.istock.kdata.muster;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MusterRepositoryTest {
	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("musterRepositoryImp")
	MusterRepository musterRepositoryImp;
	
	//@Test
	public void copyTmpMusters() {
		musterRepositoryImp.copyTmpMusters();
	}

	//@Test
	public void deleteFile() {
		LocalDate date = LocalDate.parse("2019-06-14");
		
		String pathAndFile = musterPath + "/" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +  "_55_21_musters.txt";
		
		boolean ok = FileUtils.deleteQuietly(new File(pathAndFile));
		
		System.out.println(ok);

	}
	
	
}
