package com.jyw.csp.service.log;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class HttpLog {

	private String queryUrl;
	private int responseCode;
	private long starttime;
	private long endtime;
	private Map<String,String> formMap;
	private Map<String,String> fileMap;
	private String requestString;
	private String responseString;
	private JSONObject requestJson;
	private JSONObject responseJson;

	private String errorMessage;
	public String getQueryUrl() {
		return queryUrl;
	}
	public void setQueryUrl(String queryUrl) {
		this.queryUrl = queryUrl;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public long getStarttime() {
		return starttime;
	}
	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}
	public long getEndtime() {
		return endtime;
	}
	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}
	public String getRequestString() {
		return requestString;
	}
	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}
	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Map<String, String> getFormMap() {
		return formMap;
	}

	public void setFormMap(Map<String, String> formMap) {
		this.formMap = formMap;
	}

	public Map<String, String> getFileMap() {
		return fileMap;
	}

	public void setFileMap(Map<String, String> fileMap) {
		this.fileMap = fileMap;
	}

	public JSONObject getRequestJson() {
		return requestJson;
	}

	public void setRequestJson(JSONObject requestJson) {
		this.requestJson = requestJson;
	}

	public JSONObject getResponseJson() {
		return responseJson;
	}

	public void setResponseJson(JSONObject reponseJson) {
		this.responseJson = reponseJson;
	}
}
