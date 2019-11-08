package com.rhb.istock.selector.potential;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.KdataService;
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
	
	//@Test
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
	
	//@Test
	public void getIndustryPotentials() {
		LocalDate date = LocalDate.of(2019, 9, 27);

		Map<String, TreeSet<Potential>> potentials = potentialService.getIndustryPotentials(date);
		
		List<Map.Entry<String,TreeSet<Potential>>> list = new ArrayList<Map.Entry<String,TreeSet<Potential>>>(potentials.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<String,TreeSet<Potential>>>(){
			@Override
			public int compare(Entry<String, TreeSet<Potential>> o1, Entry<String, TreeSet<Potential>> o2) {
				Integer size1 = o1.getValue().size();
				Integer size2 = o2.getValue().size();
				return size2.compareTo(size1);
			}
		});
		
		for(Map.Entry<String,TreeSet<Potential>> entry : list) {
			System.out.println(String.format("%s %d", entry.getKey(),entry.getValue().size()));
			for(Potential p : entry.getValue()) {
				System.out.print(String.format("%s(%d,%.2f)", p.getItemName(),p.getLNGap(),p.getLatestPrice()));
			}
			System.out.println("");
		}
		
	}
}
