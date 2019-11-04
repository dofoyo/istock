package com.rhb.istock.item.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("componentRepositoryImp")
public class ComponentRepositoryImp implements ComponentRepository {
	@Value("${sz50File}")
	private String sz50File;
	
	@Override
	@Cacheable("sz50Components")
	public List<Component> getSz50Components() {
		List<Component> components = new ArrayList<Component>();
		Component component;
		String[] lines = FileTools.readTextFile(sz50File).split("\n");
		String[] columns;
		String endDate;
		int i=0;
		for(String line : lines) {
			if(i++>0) {
				columns = line.split(",");
				if(columns.length >= 2) {
					component = new Component();
					component.setItemID(columns[0]);
					component.setItemName(columns[1]);
					component.setBeginDate(LocalDate.parse(columns[2], DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					if(columns.length<4) {
						endDate = "2099-01-01";
					}else {
						endDate = columns[3];
					}
					
					component.setEndDate(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					
					components.add(component);				
				}				
			}
		}
		return components;
	}

}
