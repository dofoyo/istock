package com.rhb.istock.kdata.repository;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataRepositoryTest {
	@Autowired
	@Qualifier("kdataRepositorySina")
	KdataRepository kdataRepositorySina;

	@Autowired
	@Qualifier("kdataRepositoryDzh")
	KdataRepository kdataRepositoryDzh;

	@Autowired
	@Qualifier("kdataRepositoryTushare")
	KdataRepository kdataRepositoryTushare;
	
	String id="sz300059";
	LocalDate date = LocalDate.parse("2017-02-17");
	
	@Test
	public void testGetDailyKdata() {
		System.out.println("kdataRepositoryDzh");
		KdataEntity kdata1 = kdataRepositoryDzh.getDailyKdata(id);
		System.out.println(kdata1.getBar(date));

		System.out.println("kdataRepositorySina");
		KdataEntity kdata2 = kdataRepositorySina.getDailyKdata(id);
		System.out.println(kdata2.getBar(date));

		System.out.println("kdataRepositoryTushare");
		KdataEntity kdata3 = kdataRepositoryTushare.getDailyKdata(id);
		System.out.println(kdata3.getBar(date));
	
	}
	
}
