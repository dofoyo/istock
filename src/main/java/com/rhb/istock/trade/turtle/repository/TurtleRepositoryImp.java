package com.rhb.istock.trade.turtle.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("turtleRepositoryImp")
public class TurtleRepositoryImp implements TurtleRepository{
	@Value("${holdsFile}")
	private String holdsFile;

	@Value("${favorsFile}")
	private String favorsFile;
	
	@Override
	public List<HoldEntity> getHolds() {
		List<HoldEntity> holds = new ArrayList<HoldEntity>();
		HoldEntity hold;
		String[] lines = FileUtil.readTextFile(holdsFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			hold = new HoldEntity(columns[0], new BigDecimal(columns[1]));
			holds.add(hold);
		}
		return holds;
	}
	
	@Override
	public Map<String,String> getFavors(){
		Map<String,String> articles = new HashMap<String,String> ();
		String source = FileUtil.readTextFile(favorsFile);
		//System.out.println(source);
		String[] lines = source.split("\n");
		for(String str : lines) {
			//System.out.println(str);
			String[] ss = str.split(",");
			if(ss.length>1) {
				articles.put(ss[0].toLowerCase(), ss[1]);
			}
		}
		return articles;
	}

}
