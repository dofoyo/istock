package com.rhb.istock.selector.potential;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.selector.potential.PotentialService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PotentialServiceTest {
	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;

	//@Test
	public void getTmpLatestPotentials() {
		List<String> breakers = potentialService.getLatestPotentials();
		System.out.println(breakers);
	}
	
	@Test
	public void getLatestPotentials() {
		List<Potential> ps = potentialService.getPotentials();
		for(Potential p : ps) {
			//System.out.println(p);
		}
		
		System.out.println(ps.size());
	}
	
	//@Test
	public void generateTmpLatestPotentials() {
		potentialService.generateLatestPotentials();
	}
	
}
