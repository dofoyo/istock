package com.rhb.istock.trade.balloon.operation.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("balloonOperationRepositoryImp")
public class BalloonOperationRepositoryImp implements BalloonOperationRepository {
	@Value("${bluechipsFile}")
	private String bluechipsFile;
	
	
	@Override
	public List<String> getBluechipIDs() {
		String[] ids = FileUtil.readTextFile(bluechipsFile).split(",");
		return Arrays.asList(ids);
	}

}
