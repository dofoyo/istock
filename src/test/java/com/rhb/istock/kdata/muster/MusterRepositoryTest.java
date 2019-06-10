package com.rhb.istock.kdata.muster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MusterRepositoryTest {
	@Autowired
	@Qualifier("musterRepositoryImp")
	MusterRepository musterRepositoryImp;
	
	@Test
	public void copyTmpMusters() {
		musterRepositoryImp.copyTmpMusters();
	}
}
