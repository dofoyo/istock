package com.rhb.istock.selector.drum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.item.Dimension;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DrumServiceTest {
	@Autowired
	@Qualifier("drumService")
	DrumService drumService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Test
	public void getIndustry_count() {
		LocalDate date = LocalDate.parse("2020-09-21");
		List<String> ids = drumService.getDrumsOfTopDimensions(date);
		System.out.println(ids);
	}
	
	
	
	//@Test
	public void generateDrums() {
		LocalDate beginDate = LocalDate.parse("2020-09-01");
		LocalDate endDate = LocalDate.parse("2020-09-22");
		drumService.generateDrums(beginDate, endDate);
	}
	

}
