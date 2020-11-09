package com.rhb.istock.selector.drum;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DrumServiceTest {
	@Autowired
	@Qualifier("drumService")
	DrumService drumService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Test
	public void getDrumsOfDimensions() {
		LocalDate date = LocalDate.parse("2020-11-06");
		Set<String> ids = drumService.getDrumsOfDimensions(date, 34);
		System.out.println(ids);
		System.out.println(ids.size());
		System.out.println("getDrumsOfDimensions test");

	}
	
	//@Test
	public void getDrumsOfTopDimensions() {
		LocalDate date = LocalDate.parse("2020-09-21");
		List<String> ids = drumService.getDrumsOfTopDimensions(date, null,34);
		System.out.println(ids);
	}

	//@Test
	public void generateDimension() {
		LocalDate date = LocalDate.parse("2020-09-30");
		drumService.generateDimensions(date);
	}
	
	//@Test
	public void generateDimensions() {
		List<LocalDate> dates = kdataService.getMusterDates();
		LocalDate begin = LocalDate.parse("2010-01-01");

		int i=1;
		for(LocalDate date : dates) {
			Progress.show(dates.size(), i++, date.toString());
			if(date.isAfter(begin)) {
				drumService.generateDimensions(date);
			}
		}
	}
	
	//@Test
	public void getDimensions() {
		String name = "HIT电池";
		Map<LocalDate, Integer> result = drumService.getDimension(name);
		System.out.println(result);
	}
}
