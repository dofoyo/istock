package com.rhb.istock.selector.potential;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.potential.PotentialService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PotentialServiceTest {
	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	//@Test
	public void getLatestPotentials() {
		Map<String,Potential> ps = potentialService.getLatestPotentials();
		System.out.println("There are " + ps.size() + " potentials.");

		List<Potential> potentials = new ArrayList<Potential>(ps.values());
		
		Collections.sort(potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
					if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
						return o2.getAverageAmount().compareTo(o1.getAverageAmount());
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
					}
				}
			
		});
		
		for(Potential p : potentials) {
			if(p.getItemID().startsWith("sh")) {
				System.out.println(p.getItemID());
			}
		}
	}
	
	@Test
	public void getPotentials() {
		LocalDate date = LocalDate.of(2019, 2, 19);
		List<Potential> potentials = new ArrayList<Potential>(potentialService.getPotentials(date).values());
		Collections.sort(potentials, new Comparator<Potential>() {

			@Override
			public int compare(Potential o1, Potential o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
			
		});
		
		
		int i=1;
		for(Potential p : potentials) {
			if(p.isBreaker()) {
				System.out.println(i++);
				System.out.println(p);
				if(p.getItemID().equals("sh601519")) {
					System.out.println("**************");
					break;
				}				
			}

		}
		
	}
}
