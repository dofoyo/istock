package com.rhb.istock.selector.hold;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("holdServiceImp")
public class HoldServiceImp implements HoldService {
	@Value("${holdsFile}")
	private String holdsFile;
	
	@Override
	public List<HoldEntity> getHolds() {
		List<HoldEntity> holds = new ArrayList<HoldEntity>();
		HoldEntity hold;
		String[] lines = FileUtil.readTextFile(holdsFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			if(columns.length > 2) {
				hold = new HoldEntity(columns[0], new BigDecimal(columns[1]));
				holds.add(hold);				
			}
		}
		return holds;
	}

}
