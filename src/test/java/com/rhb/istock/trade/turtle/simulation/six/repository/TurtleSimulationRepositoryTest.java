package com.rhb.istock.trade.turtle.simulation.six.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationRepositoryTest {
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	//@Test
	public void getBreakers() {
		System.out.println(turtleSimulationRepository.getBreakers("av"));
	}
	
	//@Test
	public void getAmounts() {
		System.out.println(turtleSimulationRepository.getAmounts("av"));
	}
}
