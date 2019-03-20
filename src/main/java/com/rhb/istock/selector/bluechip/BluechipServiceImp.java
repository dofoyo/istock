package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.FinancialStatementService;
import com.rhb.istock.fdata.OkfinanceStatementDto;
import com.rhb.istock.fdata.repository.ReportDateRepository;
import com.rhb.istock.selector.bluechip.api.BluechipView;
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
	
	@Autowired
	@Qualifier("reportDateRepositoryImp")
	ReportDateRepository reportDateRepository;
	
	private List<Bluechip> bluechips = null;
	
	@Override
	public void generateBluechip() {
		System.out.println("generate Bluechips begin ....");
		
		Map<String,BluechipEntity>bluechips = new HashMap<String,BluechipEntity>();
		BluechipEntity bluechip;
		
		int i=0;
		List<OkfinanceStatementDto> dtos = financialStatementService.getOks();
		for(OkfinanceStatementDto dto : dtos){
			Progress.show(dtos.size(), i++, dto.getStockcode());
			
			if(bluechips.containsKey(dto.getStockcode())){
				bluechip = bluechips.get(dto.getStockcode());
			}else{
				
				bluechip = new BluechipEntity();
				bluechip.setCode(dto.getStockcode());
				
			}
			bluechip.addOkYear(dto.getYear());
			bluechip.setReportDates(reportDateRepository.getReportDates(dto.getStockcode()));
			
			bluechips.put(dto.getStockcode(), bluechip);

		}
		
		bluechipRepository.save(bluechips.values());
		//System.out.println("there are " + dtos.size() + " OkfinanceStatementDtos.");
		System.out.println("there are " + bluechips.size() + " bluechips.");
		System.out.println("..................generate Bluechips end");
		
	}
	
	@Override
	public void init(){
		System.out.println("init Bluechips begin ....");
		this.bluechips = new ArrayList<Bluechip>();
		Set<BluechipEntity> entities = bluechipRepository.getBluechips();
		Bluechip bluechip;
		Set<Integer> okYears;
		int i=0;
		for(BluechipEntity entity : entities){
			//System.out.println(i++ + "/" + entities.size());
			bluechip = new Bluechip();
			bluechip.setCode(entity.getCode());
			bluechip.setName(entity.getName());
			bluechip.setOkYears(entity.getOkYears());

			if(entity.getReportDates()!=null) {
				for(Map.Entry<Integer, String> entry : entity.getReportDates().entrySet()){
					bluechip.addReportDate(entry.getKey(), LocalDate.parse(entry.getValue()));
				}
			}
			this.bluechips.add(bluechip);					
		}
		
		System.out.println("there are " + entities.size() + " bluechips.");
		System.out.println("...........init Bluechips end ");
	}
	
	@Override
	public List<BluechipDto> getBluechips() {
		if(this.bluechips == null){
			init();
		}
		
		List<BluechipDto> dtos = new ArrayList<BluechipDto>();
		for(Bluechip bluechip : this.bluechips){
			BluechipDto dto = this.getDto(bluechip);
			dtos.add(dto);
		}
		
		return dtos;
		
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
	private List<BluechipDto> getBluechipDtos(LocalDate date) {
		if(this.bluechips == null){
			init();
		}
		
		Integer year = date.getYear() - 1; //当前只能依据往年的年报进行判断。

		boolean isgood = false;
		
		List<BluechipDto> dtos = new ArrayList<BluechipDto>();
		for(Bluechip bluechip : this.bluechips){
			isgood = false;
			//System.out.println(bluechip);
			//if(bluechip.getIpoDate()!=null && date.isAfter(bluechip.getIpoDate())){
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
					BluechipDto dto = this.getDto(bluechip);
					dtos.add(dto);
				}
			//}
		}
		
		return dtos;
	}

	@Override
	public List<BluechipDto> getBluechips(LocalDate date) {
		List<BluechipDto> bluechips = this.getBluechipDtos(date);
/*		
		StringBuffer sb = new StringBuffer();
		String id;
		for(BluechipDto bluechip : bluechips) {
			id = bluechip.getCode().indexOf("60")==0 ? "sh"+bluechip.getCode() : "sz"+bluechip.getCode();
			sb.append(id);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		FileUtil.writeTextFile(bluechipsFile, sb.toString(), false);
*/		
		return bluechips;
	}
	
	
	
	private BluechipDto getDto(Bluechip bluechip){
		BluechipDto dto = new BluechipDto();
		dto.setCode(bluechip.getCode());
		dto.setName(bluechip.getName());
		dto.setIpoDate(bluechip.getIpoDate()==null ? null : bluechip.getIpoDate().toString());
		dto.setOkYears(bluechip.getOkYears());
		//dto.setReportDates(bluechip.getReportDates());
		for(Map.Entry<Integer, LocalDate> entry : bluechip.getReportDates().entrySet()){
			dto.addReportDate(entry.getKey(), entry.getValue().toString());
		}
		return dto;

	}

	@Override
	public boolean inGoodPeriod(String stockcode, LocalDate date) {
		boolean flag = false;
		
		List<BluechipDto> dtos = getBluechipDtos(date);
		
		for(BluechipDto dto : dtos){
			if(dto.getCode().equals(stockcode)){
				flag = true;
				break;
			}
		}
		
		return flag;
	}

	@Override
	public List<BluechipView> getBluechipViews(LocalDate date) {
		//System.out.println("getBluechipViews(" + date.toString() + ")");
		List<BluechipView> views = new ArrayList<BluechipView>();

		//System.out.println("getBluechipDtos start ...");  
		//long startTime = System.currentTimeMillis(); // 获取开始时间  
		List<BluechipDto> bluechips = this.getBluechipDtos(date);
		//long endTime = System.currentTimeMillis(); // 获取结束时间  
	    //System.out.println("getBluechipDtos over. 程序运行时间： " + (endTime - startTime) + "ms");  

		for(BluechipDto bluechipDto : bluechips){
			BluechipView view = new BluechipView();
			view.setCode(bluechipDto.getCode());
			view.setName(bluechipDto.getName());
			view.setOkYears(bluechipDto.getOkYearString());
			view.setDate(date.toString());
			view.setIpoDate(bluechipDto.getIpoDate());
			
			views.add(view);
		}
		
		Collections.sort(views, new Comparator<BluechipView>(){

			@Override
			public int compare(BluechipView arg0, BluechipView arg1) {
				return arg1.getUpProbability().compareTo(arg0.getUpProbability());
			}
			
		});
		return views;
	}

	@Override
	public BluechipDto getBluechips(String stockcode) {
		BluechipDto bd = null;
		List<BluechipDto> dtos = getBluechips();
		for(BluechipDto dto : dtos){
			if(dto.getCode().equals(stockcode)){
				bd = dto;
			}
		}
		return bd;
	}

	@Override
	public List<BluechipView> getBluechipViews() {
		//System.out.println("getBluechipViews(" + date.toString() + ")");
		List<BluechipView> views = new ArrayList<BluechipView>();

		//System.out.println("getBluechipDtos start ...");  
		//long startTime = System.currentTimeMillis(); // 获取开始时间  
		List<BluechipDto> bluechips = this.getBluechips();
		//long endTime = System.currentTimeMillis(); // 获取结束时间  
	    //System.out.println("getBluechipDtos over. 程序运行时间： " + (endTime - startTime) + "ms");  

		for(BluechipDto bluechipDto : bluechips){
			BluechipView view = new BluechipView();
			view.setCode(bluechipDto.getCode());
			view.setName(bluechipDto.getName());
			view.setOkYears(bluechipDto.getOkYearString());
			//view.setDate(date.toString());
			view.setIpoDate(bluechipDto.getIpoDate());
			
/*			tradeRecordDto = tradeRecordService.getTradeRecordsDTO(bluechipDto.getCode());
			if(tradeRecordDto != null) {
				view.setUpProbability(tradeRecordDto.getSimilarTradeRecordEntity(date).getUpProbability());
				view.setAboveAv120Days(tradeRecordDto.getSimilarTradeRecordEntity(date).getAboveAv120Days());
				view.setBiasOfAv120(tradeRecordDto.getSimilarTradeRecordEntity(date).getBiasOfAv120());
				view.setBiasOfMidPrice(tradeRecordDto.getSimilarTradeRecordEntity(date).getBiasOfMidPrice());
			}*/
			
			views.add(view);
		}
		
		Collections.sort(views, new Comparator<BluechipView>(){

			@Override
			public int compare(BluechipView arg0, BluechipView arg1) {
				return arg1.getUpProbability().compareTo(arg0.getUpProbability());
			}
			
		});
		return views;
	}

	@Override
	public List<String> getBluechipIDs(LocalDate date) {
		List<String> ids = new ArrayList<String>();
		List<BluechipDto> bluechips = this.getBluechipDtos(date);
		
		for(BluechipDto bluechip : bluechips) {
			ids.add(bluechip.getCode().indexOf("60")==0 ? "sh"+bluechip.getCode() : "sz"+bluechip.getCode());
		}
		
		return ids;
	}

}
