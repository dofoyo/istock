package com.rhb.istock.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.account.Account;

public interface Operation {
	/*
	 * top: 买入时,最多买入多少
	 * isAveValue: 买入时，是否做市值平均
	 * quantityType: 0--市值平均，1--固定金额， 2--固定数量
	 */
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType);
}
