package com.rhb.istock.kdata.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@RestController
public class KdataAPI {
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;
	
	@GetMapping("/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID) {
		KdatasView kdatas = ts.getKdatas(itemID);
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
}
