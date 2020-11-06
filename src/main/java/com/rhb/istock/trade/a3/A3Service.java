package com.rhb.istock.trade.a3;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.fina.FinaService;

@Service("a3Service")
public class A3Service {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	@Value("${operationsFile}")
	private String operationsFile;
	
	public void run(LocalDate bDate, LocalDate eDate) {
		A3 a3 = new A3();
		a3.setConfig(kdataService, finaService, indexServiceTushare, bDate, eDate);
		Map<LocalDate, Map<String,List<String>>> operations = a3.generateOperations();
		
		//FileTools.write(operations.toString(), operationsFile, "UTF-8");
		
		//System.out.println(operations);
		
		
		A3Account account = new A3Account();
		account.setConfig(kdataService, finaService, indexServiceTushare, bDate, eDate, operations);
		account.operate();
		
	}
	

	
}
