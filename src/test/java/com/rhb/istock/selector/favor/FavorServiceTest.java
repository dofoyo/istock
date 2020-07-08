package com.rhb.istock.selector.favor;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FavorServiceTest {
	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorServiceImp;
	
	@Test
	public void getIds() {
		Map<String,String> ids = favorServiceImp.getFavors();
		System.out.println(ids);
	}

}
