package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.FinancialStatement;
import com.rhb.istock.fdata.FinancialStatementService;
import com.rhb.istock.selector.bluechip.repository.BluechipEntity;
import com.rhb.istock.selector.bluechip.repository.BluechipRepository;

@Service("bluechipServiceImp")
public class BluechipServiceImp implements BluechipService {
	@Value("${bluechipsFile}")
	private String bluechipsFile;
	
	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;
	
	@Autowired
	@Qualifier("bluechipRepositoryImp")
	BluechipRepository bluechipRepository;
	
	private List<Bluechip> bluechips = null;
	
	@Override
	public void generateBluechip() {
		System.out.println("generate Bluechips begin ....");
		
		Map<String,BluechipEntity>bluechips = new HashMap<String,BluechipEntity>();
		BluechipEntity bluechip;
		
		int i=0;
		List<OkDto> dtos = getOks();
		for(OkDto dto : dtos){
			Progress.show(dtos.size(), i++, dto.getStockcode());
			
			if(bluechips.containsKey(dto.getStockcode())){
				bluechip = bluechips.get(dto.getStockcode());
			}else{
				bluechip = new BluechipEntity();
				bluechip.setCode(dto.getStockcode());
				
			}
			bluechip.addOkYear(dto.getYear());
			bluechip.addReportDate(dto.getYear(),dto.getReportdate());
			
			bluechips.put(dto.getStockcode(), bluechip);

		}
		
		bluechipRepository.save(bluechips.values());
		//System.out.println("there are " + dtos.size() + " OkfinanceStatementDtos.");
		System.out.println("there are " + bluechips.size() + " bluechips.");
		System.out.println("..................generate Bluechips end");
	}
	
	private List<OkDto> getOks() {
		List<OkDto> dtos = new LinkedList<OkDto>();
		List<Integer> years;
		FinancialStatement fs;
		FinancialReport fReport;
		Map<Integer,String> reportDates;
		OkDto dto;
		
		Set<String> codes = financialStatementService.getReportedStockCodes();
		int i=0;
		for(String code : codes){
			Progress.show(codes.size(), i++, code);

			fs = financialStatementService.getFinancialStatement(code);
			if(fs!=null) {
				reportDates = financialStatementService.getReportDates(code);
				
				fReport = new FinancialReport();
				fReport.setBalancesheets(fs.getBalancesheets());
				fReport.setCashflows(fs.getCashflows());
				fReport.setProfitstatements(fs.getProfitstatements());

				years = fReport.getPeriods();
				for(Integer year : years){
					if(fReport.isOK(year)){
						dto = new OkDto();
						dto.setStockcode(code);
						dto.setYear(year);
						dto.setReportdate(reportDates==null ? null : reportDates.get(year));
						dtos.add(dto);
					}
				}				
			}
		}
		
		return dtos;
	}
	
	//@Override
	public void init(){
		System.out.println("init Bluechips begin ....");
		this.bluechips = new ArrayList<Bluechip>();
		Set<BluechipEntity> entities = bluechipRepository.getBluechips();
		Bluechip bluechip;
		LocalDate date;
		int i=0;
		for(BluechipEntity entity : entities){
			Progress.show(entities.size(), i++, entity.getCode());
			
			bluechip = new Bluechip();
			bluechip.setCode(entity.getCode());
			bluechip.setName(entity.getName());
			bluechip.setOkYears(entity.getOkYears());

			if(entity.getReportDates()!=null) {
				for(Map.Entry<Integer, String> entry : entity.getReportDates().entrySet()){
					date = entry.getValue()==null ? LocalDate.of(entry.getKey(), 4, 30) : LocalDate.parse(entry.getValue());
					bluechip.addReportDate(entry.getKey(), date);
				}
			}
			this.bluechips.add(bluechip);					
		}
		
		System.out.println("there are " + entities.size() + " bluechips.");
		System.out.println("...........init Bluechips end ");
	}
	
	/*
	 * 选股策略：
	 * 1、当年入选
	 * 2、近三年二次入选
	 * 满足以上一条即可
	 * 
	 * 例如：传入的日期是2018年3月24日
	 * 则如下条件选中：
	 * 1、2017年年报OK（已发布2017年年报）
	 * 2、2016年和2015年OK
	 * 满足以上一条即可
	 * 
	 * 或
	 * 
	 * 1、2016年年报OK（还未发布2017年年报）
	 * 2、2015年和2014年OK
	 * 满足以上一条即可
	 * 
	 * 
	 */
	private List<Bluechip> getBluechips(LocalDate date) {
		if(this.bluechips == null){
			init();
		}
		
		Integer year = date.getYear() - 1; //当前只能依据往年的年报进行判断。

		boolean isgood = false;
		
		List<Bluechip> bs = new ArrayList<Bluechip>();
		for(Bluechip bluechip : this.bluechips){
			isgood = false;
			if(bluechip.hasReported(date)){
				if(bluechip.isOk(year) || (bluechip.isOk(year-1) && bluechip.isOk(year-2))){
					isgood = true;
				}
			}else{
				if(bluechip.isOk(year-1) || (bluechip.isOk(year-2) && bluechip.isOk(year-3))){
					isgood = true;
				}				
			}
			
			if(isgood){
				bs.add(bluechip);
			}
		}
		
		return bs;
	}

	@Override
	public List<String> getBluechipIDs(LocalDate date) {
		List<String> ids = new ArrayList<String>();
		List<Bluechip> bluechips = this.getBluechips(date);
		
		for(Bluechip bluechip : bluechips) {
			ids.add(bluechip.getCode().indexOf("60")==0 ? "sh"+bluechip.getCode() : "sz"+bluechip.getCode());
		}
		
		return ids;
	}
	
	class OkDto {
		private String stockcode;
		private Integer year;
		private String reportdate;
		
		public String getStockcode() {
			return stockcode;
		}
		public void setStockcode(String stockcode) {
			this.stockcode = stockcode;
		}
		public Integer getYear() {
			return year;
		}
		public void setYear(Integer year) {
			this.year = year;
		}
		public String getReportdate() {
			return reportdate;
		}
		public void setReportdate(String reportdate) {
			this.reportdate = reportdate;
		}
		@Override
		public String toString() {
			return "OkfinanceStatementDto [stockcode=" + stockcode + ", year=" + year + ", reportdate=" + reportdate + "]";
		}
	}
}
