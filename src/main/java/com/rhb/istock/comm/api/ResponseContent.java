package com.rhb.istock.comm.api;

public class ResponseContent<T> {
	private String code;
	private String msg;
	private T content;
	
	public ResponseContent(ResponseEnum responseEnum, T content){
		this.code = responseEnum.getCode();
		this.msg = responseEnum.getMsg();
		this.content = content;
	}
	
	public String getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}


	public T getContent() {
		return content;
	}

}
