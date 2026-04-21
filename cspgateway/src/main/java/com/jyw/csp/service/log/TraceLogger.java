package com.jyw.csp.service.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TraceLogger {
	protected static final Logger common_logger = LoggerFactory.getLogger(TraceLogger.class);
	private static Map<String,Logger> loggerMap = new HashMap<String,Logger>();
	private static Object lock1 = new Object();
	/**
	 * 打印缓存日志方法
	 * @param traceLog  报文日志缓存类
	 * @return
	 */
	@Async("traceLogExecutor")
	public void dispose(TraceLog traceLog) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		//traceLog.setEndTime(System.currentTimeMillis());

		//1.总开关判定----------------------------------------
		boolean logFlag = true;
		if(traceLog == null || !logFlag)
			return ;


		//3.计算日志名----------------------------------------
		String logName = traceLog.getLogName();
		if(logName.startsWith("/")){
			logName = logName.substring(1);
		}
		logName = logName.replaceAll("/", "_");

		//4.计算各种耗时----------------------------------------
		long startTime = traceLog.getStartTime();
		long endTime = traceLog.getEndTime();
		long httpcost = traceLog.getHttpcost();

		long totalcost = endTime - startTime;

		long codecost = totalcost - httpcost;

		//String logPath=BusinessSysParaUtil.getStrValue("LogPath:1","./logs");
		String logPath=traceLog.getLogPath();
		//String maxFileSize=BusinessSysParaUtil.getStrValue("MaxFileSize:1","100mb");
		String maxFileSize=traceLog.getMaxFileSize();
		//String totalSizeCap=BusinessSysParaUtil.getStrValue("TotalSizeCap:1","100gb");
		String totalSizeCap="100gb";
		//获取日志logger对象----------------------------------------
		Logger logger = null;
		synchronized(lock1) {
			logger = loggerMap.get(logName);
			if(logger == null) {
				logger = MyFileLogger.getLogger(logName,logPath,maxFileSize,totalSizeCap);
				loggerMap.put(logName, logger);
			}
		}

		//7logger写入日志----------------------------------------
		synchronized(logger) {
			logger.info("============ [  summary  ] ============");
			logger.info("Csp-Trace-Id     : " + traceLog.getTraceId());
			logger.info("Request-url      : " + traceLog.getUrl());
			logger.info("Start Time       : " + sdf.format(new Date(startTime)));
			logger.info("End Time         : " + sdf.format(new Date(endTime)));
			logger.info("============ [  request_client  ] ============");
			logger.info("HttpRequestBody_client  : ");
			logger.info(traceLog.getRequestJsonString_client());
			
			if(traceLog.getPlaintextRequestInfo()!=null) {
				logger.info("========= [  request_info PlainText ] ========");
				logger.info("request_info  : ");
				logger.info(traceLog.getPlaintextRequestInfo());
			}
			
			if(traceLog.getRequsetHsmString()!=null)
			{
				logger.info("============ [  request  ] ============");
				logger.info("RequestBody  : ");
				logger.info(traceLog.getRequsetHsmString());
			}
			
			
			logger.info("============ [  detail   ] ============");
			for(String str:traceLog.getLogDetailStrList()){
				logger.info(str);
			}
			if(traceLog.getResponseHsmString()!=null)
			{
				logger.info("============ [ response  ] ============");
				logger.info("ResponseBody : ");
				logger.info(traceLog.getResponseHsmString());
			}
			logger.info("============ [ response_client  ] ============");
			logger.info("HttpResponseBody_client : ");
			logger.info(traceLog.getResponseJsonString_client());
			
			if(traceLog.getPlaintextResponseInfo()!=null) {
				logger.info("========= [  response_info PlainText ] ========");
				logger.info("response_info  : ");
				logger.info(traceLog.getPlaintextResponseInfo());
			}
			
			if(traceLog.getErrormessage()!=null)
			{
				logger.info("============ [ Errormessage  ] ============");
				logger.info(traceLog.getErrormessage());
			}
			logger.info("============ [ time cost ] ============");
			logger.info("Total Use Time   : " + totalcost);
			logger.info("Http Use Time    : " + httpcost);
			logger.info("Code Cost        : " + codecost);
			logger.info("#################################################################");
			logger.info("");
		}

		return;
	}

	
}
