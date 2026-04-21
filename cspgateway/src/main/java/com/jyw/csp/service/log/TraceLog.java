package com.jyw.csp.service.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.util.JsonUtils;
import com.jyw.csp.util.string.StringUtils;

public class TraceLog {

	protected static final Logger common_logger = LoggerFactory.getLogger(TraceLog.class);

	//需要设置
	private String traceId;
	private String url="";
	private String logName;
	private int maxValueLength = -1;
	private int level = LogLevel.ERROR;
	private String errormessage;
	private long startTime = System.currentTimeMillis();
	private long endTime;
	private long sendHsmTime;
	private String logPath;
	private String maxFileSize;
	
	
	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public long getSendHsmTime() {
		return sendHsmTime;
	}

	public void setSendHsmTime(long sendHsmTime) {
		this.sendHsmTime = sendHsmTime;
	}


	private String plaintextRequestInfo;
	
	private String plaintextResponseInfo;
	
	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getUrl() {
		return url;
	}
	/**
	 * 设置请求url，用于打印
	 * @param url
	 */
	public void setUrl(String url) {
		common_logger.debug("url: "+url);
		if(url!=null)
		{
			this.url =this.url+" "+url;
		}
	}

	public int getMaxValueLength() {
		return maxValueLength;
	}

	public void setMaxValueLength(int maxValueLength) {
		this.maxValueLength = maxValueLength;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(String levelStr) {
		if(StringUtils.isNotEmpty(levelStr)){
			levelStr = levelStr.toUpperCase();
			if("DEBUG".equals(levelStr)) {
				this.level = LogLevel.DEBUG;
			}else if("INFO".equals(levelStr)){
				this.level = LogLevel.INFO;
			}else{
				this.level = LogLevel.ERROR;
			}
		}
		//this.level = level;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	private String requsetHsmString;
	private String responseHsmString;
	
	private String requestJsonString_client;
	private String responseJsonString_client;
	
	public String getRequestJsonString_client() {
		return requestJsonString_client;
	}
	public String getResponseJsonString_client() {
		return responseJsonString_client;
	}
	public void setResponseJsonString_client(String responseJsonString_client) {
		this.responseJsonString_client =  JsonUtils.formatJson(responseJsonString_client,maxValueLength);
		common_logger.debug("ResponseJson_client:");
		common_logger.debug(this.responseJsonString_client);
	}
	public void setRequestJsonString_client(String requestJsonString_client) {
		this.requestJsonString_client =  JsonUtils.formatJson(requestJsonString_client,maxValueLength);
		common_logger.debug("RequestJson_client:");
		common_logger.debug(this.requestJsonString_client);
	}
	public String getRequsetHsmString() {
		return requsetHsmString;
	}

	public void setRequsetHsmString(String requsetHsmString) {
		this.requsetHsmString = requsetHsmString;
	}

	public String getResponseHsmString() {
		return responseHsmString;
	}

	public void setResponseHsmString(String responseHsmString) {
		this.responseHsmString = responseHsmString;
	}

	
	public String getPlaintextRequestInfo() {
		return plaintextRequestInfo;
	}

	public void setPlaintextRequestInfo(String plainText) {
		this.plaintextRequestInfo =  JsonUtils.formatJson(plainText,maxValueLength);
		common_logger.debug("decrypt request for plainText :");
		common_logger.debug(this.plaintextRequestInfo);
	}
	
	

	public String getPlaintextResponseInfo() {
		return plaintextResponseInfo;
	}

	public void setPlaintextResponseInfo(String plaintextResponseInfo) {
		this.plaintextResponseInfo = JsonUtils.formatJson(plaintextResponseInfo,maxValueLength);
		common_logger.debug("decrypt response for plainText :");
		common_logger.debug(this.plaintextResponseInfo);
	}

	public String getErrormessage() {
		return errormessage;
	}

	public void setErrormessage(String errormessage) {
		this.errormessage = errormessage;
	}

	public void debug(Object obj){
		if(LogLevel.DEBUG >= level)
			diposeLogMessage(obj,"DEBUG");
	}
	public void info(Object obj){
		if(LogLevel.INFO >= level)
			diposeLogMessage(obj,"INFO ");
	}
	public void error(Object obj){
		if(LogLevel.ERROR >= level)
			diposeLogMessage(obj,"ERROR");
	}

	private List<String> logDetailStrList = new ArrayList<String>();
	private void setlogDetailStr(String str){
		logDetailStrList.add(str);
		common_logger.debug(str);
	}
	public List<String> getLogDetailStrList(){
		return this.logDetailStrList;
	}

	private int index = 0;
	private long httpcost = 0L;

	public long getHttpcost() {
		return httpcost;
	}
	public void  setHttpcost(long httpcost)
	{
		this.httpcost=httpcost;
	}

	public void diposeLogMessage(Object obj, String levelName){
		String logHead = "["+(++index)+"]";
		logHead = logHead+" ["+levelName+"] ";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		if( obj instanceof HttpLog){
			HttpLog httplog = (HttpLog)obj;

			long thisHttpCost = httplog.getEndtime() - httplog.getStarttime();
			httpcost = httpcost + thisHttpCost;

			setlogDetailStr(logHead+"======== call http ========");
			String httpheadstr = String.format("StartTime:%s  EndTime:%s  UseTime(ms):%s" ,
					sdf.format(new Date(httplog.getStarttime())),
					sdf.format(new Date(httplog.getStarttime())),
					thisHttpCost
			);
			setlogDetailStr(httpheadstr);
			setlogDetailStr("queryUrl    : " + httplog.getQueryUrl());
			setlogDetailStr("requestBody : ");
			Map<String,String> formMap = httplog.getFormMap();
			if(formMap != null && formMap.size() != 0){
				setlogDetailStr("--post form--");
				for (Map.Entry<String,String> entry : formMap.entrySet()){
					setlogDetailStr(entry.getKey()+"="+getCompressString(entry.getValue(),maxValueLength));
				}
			}
			Map<String,String> fileMap = httplog.getFileMap();
			if(fileMap != null && fileMap.size() != 0){
				setlogDetailStr("--multipart/form-data--");
				for (Map.Entry<String,String> entry : fileMap.entrySet()){
					setlogDetailStr(entry.getKey()+"="+getCompressString(entry.getValue(),maxValueLength));
				}
			}

			String requestString = httplog.getRequestString();
			if(requestString!=null){
				if(requestString.startsWith("{") && requestString.endsWith("}")) {
					setlogDetailStr(JsonUtils.formatJson(requestString, maxValueLength));
				}else {
					setlogDetailStr(getCompressString(requestString,maxValueLength));
				}
			}

			JSONObject requestJson = httplog.getRequestJson();
			if(requestJson!=null){
				setlogDetailStr(JsonUtils.formatJson(requestJson, maxValueLength));
			}

			setlogDetailStr("responseBody: ");
			String responseString = httplog.getResponseString();
			if(responseString!=null){
				if(responseString.startsWith("{") && responseString.endsWith("}")) {
					setlogDetailStr(JsonUtils.formatJson(responseString, maxValueLength));
				}else {
					setlogDetailStr(getCompressString(responseString, maxValueLength));
				}
			}

			JSONObject responseJson = httplog.getResponseJson();
			if(responseJson!=null){
				setlogDetailStr(JsonUtils.formatJson(responseJson, maxValueLength));
			}


			setlogDetailStr("errorMessage: " + httplog.getErrorMessage());
		}else if( obj instanceof Exception){
			setlogDetailStr(logHead+ getExceptionMessage((Exception)obj));
		}else if( obj instanceof JSONObject){
			setlogDetailStr(logHead+"\n"+ JsonUtils.formatJson((JSONObject)obj, maxValueLength));
		}else{
			if(obj == null){
				obj = "null";
			}
			setlogDetailStr(logHead+ getCompressString(obj.toString(),maxValueLength));
		}
	}

	public static String getExceptionMessage(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return e.toString()+"\n"+sw.toString();
	}

	public String getCompressString(String str,int maxLength){
		if(str == null || maxLength <= -1){
			return str;
		}

		if(str.length()> maxLength){
			String result = "|$| omitted print length "+str.length() + " |$|";
			return result;
		}else {
			return str;
		}

	}
}
