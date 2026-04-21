package com.jyw.csp.util.log.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.jyw.csp.util.log.base.LogConfig;
import com.jyw.csp.util.log.vo.PlainLogVO;
import com.jyw.csp.util.log.vo.TransactionLogVO;

/**
 * 
 * 在写第一日志异常的情况下,启动第二日志
 * 
 * <p>Title: 日志文件操作工具类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogFileEngine {
	/**
	 * 1MB内存占用
	 */
	public static final long MB = 1024 * 1024;

	/**
	 * 日志字符编码
	 */
	public static final String ENCODING_CHARSET = LogConfig.encoding_CharSet;

	/**
	 * 日志文件扩展名
	 */
	public static final String LOG_FILE_EXTNAME = ".log";

	/**
	 * 错误日志文件扩展名
	 */
	public static final String ERROR_LOG_FILE_EXTNAME = ".err";
	/**
	 * 普通日志
	 */
	LogFileWriter logFileWriter;
	LogFileWriter logFileWriter_second;

	/**
	 * 错误日志
	 */
	LogFileWriter errLogFileWriter;
	LogFileWriter errLogFileWriter_second;
	boolean isFlushed = false;

	public LogFileEngine() {
		logFileWriter = new LogFileWriter(LOG_FILE_EXTNAME);
		errLogFileWriter = new LogFileWriter(ERROR_LOG_FILE_EXTNAME);
//		logFileWriter_second = new LogFileWriter(LOG_FILE_EXTNAME,LogConfig.logFilePath_second);
//		errLogFileWriter_second = new LogFileWriter(ERROR_LOG_FILE_EXTNAME,LogConfig.logFilePath_second);
	}

	public void closeFile() {
		try {
			logFileWriter.closeFile();
			errLogFileWriter.closeFile();
			if(logFileWriter_second!=null)
				logFileWriter_second.closeFile();
			if(errLogFileWriter_second!=null)
				errLogFileWriter_second.closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void flushFile() {
		try {
			if (!isFlushed) {
				logFileWriter.flush();
				errLogFileWriter.flush();
				if(logFileWriter_second!=null)
					logFileWriter_second.flush();
				if(errLogFileWriter_second!=null)
					errLogFileWriter_second.flush();
				isFlushed = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将对象记录到日志中
	 * 
	 * @param obj
	 */
	public void logToFile(TransactionLogVO txLogVo) {
		boolean isTest = txLogVo.isTest();
		String txNo = txLogVo.getTxNo();
		
		String ip=txLogVo.getRemoteIP();


		List<PlainLogVO> logList = txLogVo.getCommonLogList();
		for (PlainLogVO logVO : logList) {
			if (logVO.getException() != null) {
				StringWriter sw = new StringWriter();
				PrintWriter printer = new PrintWriter(sw);
				logVO.getException().printStackTrace(printer);
				String errorMsg = sw.toString();
				String content = logVO.getLogContent();
				if (content == null) {
					content = errorMsg;
				} else {
					content += "\t" + errorMsg;
				}
				logVO.setLogContent(content);
			}

			if (Level.ERROR.toString().equals(logVO.getLogType())) {
				try {
					errLogFileWriter.log(ip,txNo, logVO, isTest);
				} catch (Exception e) {
					try {
						if(errLogFileWriter_second==null){
							errLogFileWriter_second = new LogFileWriter(ERROR_LOG_FILE_EXTNAME,LogConfig.logFilePath_second);
						}
						errLogFileWriter_second.log(ip,txNo, logVO, isTest);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}else{
				try {
					if(txLogVo.isTxLog()){
						logFileWriter.log(ip, txNo, txLogVo.getTxcode(), logVO);
					}else{
						logFileWriter.log(ip,txNo, logVO, isTest);
					}
				} catch (Exception e) {
					try {
						if(logFileWriter_second==null){
							logFileWriter_second = new LogFileWriter(LOG_FILE_EXTNAME,LogConfig.logFilePath_second);
						}
						if(txLogVo.isTxLog()){
							logFileWriter_second.log(ip, txNo, txLogVo.getTxcode(), logVO);
						}else{
							logFileWriter_second.log(ip,txNo, logVO, isTest);
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		isFlushed = false;
	}
}