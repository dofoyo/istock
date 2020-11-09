package com.rhb.istock.fund;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;

@Service("fundRepositoryTushare")
public class FundRepositoryTushare {
	@Value("${fundsFile}")
	private String fundsFile;

	@Value("${fundsPath}")
	private String fundsPath;
	
	public	Set<FundBasic> getFundBasics(){
		Set<FundBasic> ics = new HashSet<FundBasic>();
		
		if(FileTools.isExists(fundsFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fundsFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				FundBasic basic;
				String  ts_code,name,management,fund_type,status;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					ts_code = item.get(0).toString();
					name = item.get(1).toString();
					management = item.get(2).toString();
					fund_type = item.get(4).toString();
					status = item.get(18).toString();
					basic = new FundBasic(ts_code,name,management,fund_type,status);
					ics.add(basic);
				}
			}
		}
		
		return ics;
	}
	
	public	Set<FundPortfolio> getFundPortfolios(String ts_code){
		Set<FundPortfolio> ics = new HashSet<FundPortfolio>();
		String dataFile = fundsPath + "/" + ts_code + ".json";
		
		if(FileTools.isExists(dataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(dataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				FundPortfolio portfolio;
				String  ann_date,end_date,symbol;
				BigDecimal mkv,amount,stk_mkv_ratio,stk_float_ratio;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					//ts_code = item.get(0).toString();
					ann_date = item.get(1).toString();
					end_date = item.get(2).toString();
					symbol = item.get(3).toString();
					mkv = item.get(4).toString().equals("null")? BigDecimal.ZERO : item.getBigDecimal(4);
					amount = item.get(5).toString().equals("null")? BigDecimal.ZERO : item.getBigDecimal(5);
					stk_mkv_ratio = item.get(6).toString().equals("null")? BigDecimal.ZERO : item.getBigDecimal(6);
					stk_float_ratio = item.get(7).toString().equals("null")? BigDecimal.ZERO : item.getBigDecimal(7);
					portfolio = new FundPortfolio(ts_code,ann_date,end_date,symbol,mkv,amount,stk_mkv_ratio,stk_float_ratio);
					ics.add(portfolio);
				}
			}
		}
		
		return ics;
	}
	
	public Map<String, ItemPortfolio> getItemPortfolioes(String period) {
		Map<String, ItemPortfolio> itemPortfolioes = new HashMap<String,ItemPortfolio>();
		ItemPortfolio itemPortfolio;
		
		Set<FundPortfolio> fundPortfolioes;
		Set<FundBasic> basics = this.getFundBasics();
		int i=1;
		for(FundBasic basic : basics) {
			Progress.show(basics.size(), i++, basic.getName());
			fundPortfolioes = this.getFundPortfolios(basic.getTs_code());
			if(fundPortfolioes!=null && fundPortfolioes.size()>0) {
				for(FundPortfolio fundPortfolio : fundPortfolioes) {
					if(fundPortfolio.getEnd_date().equals(period)){
						itemPortfolio = itemPortfolioes.get(fundPortfolio.getSymbol());
						if(itemPortfolio==null) {
							itemPortfolio = new ItemPortfolio();
							itemPortfolio.setSymbol(fundPortfolio.getSymbol());
							itemPortfolio.setEnd_date(fundPortfolio.getEnd_date());
							itemPortfolio.setAmount(fundPortfolio.getAmount());
							itemPortfolio.setStk_mkv_ratio(fundPortfolio.getStk_mkv_ratio());
						}else {
							itemPortfolio.addAmount(fundPortfolio.getAmount());
							itemPortfolio.addStk_mkv_ratio(fundPortfolio.getStk_mkv_ratio());
						}
						itemPortfolioes.put(fundPortfolio.getSymbol(),itemPortfolio);
					}
				}
			}
		}
		
		return itemPortfolioes;
	}	
	
}
