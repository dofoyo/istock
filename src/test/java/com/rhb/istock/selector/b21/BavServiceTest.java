package com.rhb.istock.selector.b21;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BavServiceTest {
	@Autowired
	@Qualifier("b21Service")
	B21Service b21Service;
	
	@Test
	public void generateBAV() {
		LocalDate endDate = LocalDate.parse("2020-06-16");
		b21Service.generateB21(endDate,13);
	}
}
