package com.rhb.istock.selector.potential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

	@Test
	public void getLatestPotentials() {
		Map<String,Potential> ps = potentialService.getLatestPotentials();
		System.out.println("There are " + ps.size() + " potentials.");

		List<Potential> potentials = new ArrayList<Potential>(ps.values());
		
		Collections.sort(potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
			
		});
		
		for(Potential p : potentials) {
			if(p.getItemID().startsWith("sh")) {
				System.out.println(p.getItemID());
			}
		}
		
	}
	
}
