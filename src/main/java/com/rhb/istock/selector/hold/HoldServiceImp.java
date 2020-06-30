package com.rhb.istock.selector.hold;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("holdServiceImp")
public class HoldServiceImp implements HoldService {
	@Value("${holdsFile}")
	private String holdsFile;
	
	@Override
	public List<HoldEntity> getHolds() {
		List<HoldEntity> holds = new ArrayList<HoldEntity>();
		HoldEntity hold;
		String[] lines = FileTools.readTextFile(holdsFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			if(columns.length >= 2) {
				hold = new HoldEntity();
				hold.setDate(columns[0]);
				hold.setItemID(columns[1]);
				hold.setItemName(columns[2]);
				hold.setPrice(columns[3]);
				hold.setLabel(columns[4]);
				holds.add(hold);				
			}
		}
		return holds;
	}

}
