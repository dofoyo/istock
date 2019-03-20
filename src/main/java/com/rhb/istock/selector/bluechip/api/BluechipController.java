package com.rhb.istock.selector.bluechip.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.selector.bluechip.BluechipService;

@RestController
public class BluechipController {
	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;
	
	@GetMapping("/bluechips")
	public ResponseContent<List<BluechipView>> getBluechipViews(
			@RequestParam(value="date", defaultValue="") String date
			){
		LocalDate theDate = null;
		if(date.isEmpty()){
			theDate = LocalDate.now();
		}else{
			//System.out.println(date);
			theDate = LocalDate.parse(date.substring(0,10));
		}
		List<BluechipView> bluechips = bluechipService.getBluechipViews(theDate);
		//System.out.println(bluechips.size());
		return new ResponseContent<List<BluechipView>>(ResponseEnum.SUCCESS, bluechips);
	}
	
	
	@GetMapping("/downbluechips")
	public ResponseEntity<InputStreamResource> dwonDzhs(){
		String marketCode;
		StringBuffer sb = new StringBuffer();
		
		List<BluechipView> bluechips = bluechipService.getBluechipViews( LocalDate.now());

		
		//List<TradeRecordDzh> tradeRecordDzhs = tradeRecordService.getDzhs();
		
		
		for(BluechipView view : bluechips) {
			marketCode = view.getCode().indexOf("60")==0 ? "SH" : "SZ";
			sb.append(marketCode);
			sb.append(view.getCode());
			sb.append(",");
		}
		InputStream  in_nocode = new ByteArrayInputStream(sb.toString().getBytes());   
		
        HttpHeaders headers = new HttpHeaders();  
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");  
        headers.add("Content-Disposition", "attachment;filename=bluechips.txt");  
        headers.add("Pragma", "no-cache");  
        headers.add("Expires", "0");  
		
        return ResponseEntity  
                .ok()  
                .headers(headers)  
                .contentLength(sb.length())  
                .contentType(MediaType.parseMediaType("application/octet-stream"))  
                .body(new InputStreamResource(in_nocode));  
		
	}
	

}
