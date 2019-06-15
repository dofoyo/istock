package com.rhb.istock.trade.kelly;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KellyServiceTest {
	@Autowired
	@Qualifier("kellyService")
	KellyService kellyService;
	
	@Test
	public void test() {
	}
	
}
