package com.rhb.istock.selector.txt;

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
public class BavServiceTest {
	@Autowired
	@Qualifier("txtService")
	TxtService txtService;
	
	//@Test
	public void getIds() {
		Map<String,String> ids = txtService.getIds();
		System.out.println(ids);
	}
	
	@Test
	public void getRatio() {
		txtService.getRatio();
	}
}
