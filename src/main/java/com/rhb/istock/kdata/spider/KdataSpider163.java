package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpDownload;
import com.rhb.istock.kdata.KdataServiceImp;

@Service("kdataSpider163")
public class KdataSpider163 implements KdataSpider {
	@Value("${kdataPath163}")
	private String kdataPath;
	
	protected static final Logger logger = LoggerFactory.getLogger(KdataSpider163.class);
	
	@Override
	public void downKdatas(List<String> ids) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void downKdatas(String itemID) throws Exception {
		String code = itemID.substring(2);
		String marketCode = (itemID.indexOf("sh")==0 ? "0" : "1") + code;
		
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate today = LocalDate.now();
		
		
		//String start = Integer.toString(year-2) + "0101";
		//String start = today.minusDays(250).format(df);
		String start = "19990101";
		String end = today.format(df);
		
		//http://quotes.money.163.com/service/chddata.html?code=1300384&start=20181001&end=20190211&fields=TCLOSE
		String url = "http://quotes.money.163.com/service/chddata.html?code="+marketCode+"&start="+start+"&end="+end;

		//System.out.println(url);
		
		String pathAndfileName = kdataPath + "/"  + itemID + ".csv";

		//System.out.println("save trade record: " + pathAndfileName);
		HttpDownload.saveToFile(url, pathAndfileName);
		
		logger.info("KdataService.downSSEI  done!  ..........");


	}

	@Override
	public void downKdatas(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downFactors(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downFactors(String id) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downFactors(List<String> ids) throws Exception {
		// TODO Auto-generated method stub
		
	}


}
