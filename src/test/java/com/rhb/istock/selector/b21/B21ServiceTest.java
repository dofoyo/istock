package com.rhb.istock.selector.b21;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class B21ServiceTest {
	@Autowired
	@Qualifier("b21Service")
	B21Service b21Service;
	
	@Test
	public void test() {
		List<String> ids = new ArrayList<String>();
		ids.add("sh603488");
		LocalDate date = LocalDate.now();
		Map<String,String> ms = b21Service.getStates(ids, date);
		System.out.println(ms);
	}
}
