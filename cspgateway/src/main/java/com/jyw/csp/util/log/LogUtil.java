package com.jyw.csp.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jyw.csp.util.log.base.LogConfig;
import com.jyw.csp.util.log.base.LoggerSingleFile;


/**
 * 同步记录日志，对于高并发会影响服务器性能
 * 使用log4j提供不同级别的日志记录工具类
 * 
 * <p>Title: </p>
 * <p>Description: 日志工具类，use log4j</p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogUtil {
	private static final Logger log = LoggerFactory.getLogger(LogUtil.class);
	private static LoggerSingleFile loggers = null;

	
	public static synchronized LoggerSingleFile createLogger() throws Exception{
	    if (loggers == null) {
	    	loggers = new LoggerSingleFile();
	    }
	    return loggers;
	}
	
	public static void log(String msg){
		log(msg,null);
	}
	
	/**
	 * 将日志信息msg记录到txcode.log日志文件
	 * @param msg
	 * @param txcode
	 * @return
	 */
	public static boolean log(String msg, String txcode){
		if (LogConfig.logConsoleEnabled) {
			System.out.println(msg);
		}
		try{
			if (loggers == null) {
		    	loggers = new LoggerSingleFile();
		    }
			if(txcode==null || "".equals(txcode)){
				txcode = "SUAP";
			}
//			if("1".equals(SysParaUtil.getStrPara("IS_OFF_TRANSLOG", "0"))) {
//				return true;
//			}
			return loggers.log(msg, txcode);
//			if("0".equals(SysParaUtil.getStrPara("IS_OFF_TRANSLOG", "1"))) {
//				transLogger.setLevel(Level.OFF);
//	        } else {
//	        	transLogger.setLevel(Level.INFO);
//	        }
//			transLogger.info(msg);
//			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 实现debug的方法，记录Debug级别日志
	 * 
	 * @param s
	 */
	public static void debug(String s) {
		if (LogConfig.logConsoleEnabled) {
			System.out.println(s);
		}

		if (!LogConfig.logDebugEnabled) {
			return;
		}

		log.debug(s);
	}

	/**
	 * 记录Info级别日志
	 * @param s
	 */
	public static void info(String s) {
		if (LogConfig.logConsoleEnabled) {
			System.out.println(s);
		}
		
		if (!LogConfig.logInfoEnabled) {
			return;
		}

		log.info(s);
	}

	/**
	 * 记录Warn级别日志
	 * @param s
	 */
	public static void warn(String s) {
		if (LogConfig.logConsoleEnabled) {
			System.out.println(s);
		}
		
		if (!LogConfig.logWarnEnabled) {
			return;
		}
		
		log.warn(s);
	}

	/**
	 * 记录Error级别日志
	 * @param s
	 */
	public static void error(String s) {
		if (LogConfig.logConsoleEnabled) {
			System.out.println(s);
		}
		
		if (!LogConfig.logErrorEnabled) {
			return;
		}
		
		log.error(s);
	}
	
	/**
	 * 记录Error级别日志
	 * @param s
	 * @param tr 异常对象
	 */
	public static void error(String s,Throwable tr) {
		if (!LogConfig.logErrorEnabled) {
			return;
		}
		
		log.error(s, tr);
	}
	
	/**
	 * 记录Error级别日志
	 * @param tr 异常对象
	 */
	public static void error(Throwable tr) {
		if (!LogConfig.logErrorEnabled) {
			return;
		}
		
		log.error("", tr);
	}
}