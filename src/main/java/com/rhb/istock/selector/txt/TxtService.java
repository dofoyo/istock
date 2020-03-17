package com.rhb.istock.selector.txt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("txtService")
public class TxtService {
	@Value("${txtFile}")
	private String txtFile;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	
	public Map<String,String> getIds() {
		Map<String,String> ids = new TreeMap<String,String> ();
		
		String id;
		String source = FileTools.readTextFile(txtFile);
		String[] lines = source.split("\n");
		for(String line : lines) {
			System.out.println(line);
			id = line.substring(0,6);
			if(id.matches("6.*")) {
				id = "sh" + id;
			}else {
				id = "sz" + id;
			}
			ids.put(id,line);
		}
		return ids;
	}
	
	public void getRatio() {
		Map<String,String> ids = this.getIds();
		LocalDate beginDate = LocalDate.parse("2019-01-02");
		LocalDate endDate = LocalDate.parse("2020-03-16");
		Map<String,Muster> ms1 = kdataService.getMusters(beginDate);
		Map<String,Muster> ms2 = kdataService.getMusters(endDate);
		
		Integer ratio;
		BigDecimal p1,p2;
		System.out.println("代码,名称,现价,细分行业,地区,流通股(亿),流通市值Z,流通市值,AB股总市值,市盈(动),资产负债率%,20190102,20200316");
		for(String id : ids.keySet()) {
			if(ms1.get(id)!=null && ms2.get(id)!=null) {
				p1 = ms1.get(id).getLatestPrice();
				p2 = ms2.get(id).getLatestPrice();
				System.out.printf("%s, %s, %.2f, %.2f\n",id,ids.get(id),p1,p2);
			}
		}

		
	}

}
