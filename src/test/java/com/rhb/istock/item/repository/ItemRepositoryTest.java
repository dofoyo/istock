package com.rhb.istock.item.repository;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ItemRepositoryTest {
	@Autowired
	@Qualifier("componentRepositoryImp")
	ComponentRepository componentRepository;
	
	@Test
	public void getSz50Components() {
		List<Component> items = componentRepository.getSz50Components();
		for(Component item : items) {
			System.out.println(item);
		}
	}

}
