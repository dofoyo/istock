package com.rhb.istock.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.account.Account;

public interface Operation {
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label);
}
