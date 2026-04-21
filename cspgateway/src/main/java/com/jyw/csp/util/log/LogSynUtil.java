package com.jyw.csp.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Level;

import com.jyw.csp.util.log.base.LogConfig;
import com.jyw.csp.util.log.engine.LogEngine;
import com.jyw.csp.util.log.vo.PlainLogVO;
import com.jyw.csp.util.log.vo.TransactionLogVO;


/**
 * 异步记录日志，通过后台线程从日志队列获取日志对象记录相应文件
 * 
 * 在开始记录新的日志前，必须首先调用 LogUtil.beginLog()方法
 * 在完成一笔交易后，必须调用 LogUtil.endLog()方法
 * 
 * <p>Title: log工具类（供外部调用）</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogSynUtil {
	private static final ThreadLocal<TransactionLogVO> threadLocal = new ThreadLocal<TransactionLogVO>();

	/**
	 * 开始新的日志，必须在记录任何日志前，首先调用
	 * 
	 */
	public static void beginLog() {
		TransactionLogVO logMsgVo = new TransactionLogVO();
		threadLocal.set(logMsgVo);
	}

	public static void setTest(boolean isTest) {
		threadLocal.get().setTest(isTest);
	}

	/**
	 * 设置当前交易流水号
	 * 
	 * @param txNo
	 */
	public static void setTransNo(String txNo) {
		threadLocal.get().setTxNo(txNo);
	}

	/**
	 * 设置远程连接地址
	 * @param ip
	 */
	public static void setRemoteIP(String ip) {
		threadLocal.get().setRemoteIP(ip);
	}
	public static void setRemoteIP(String ip,int port) {
		threadLocal.get().setRemoteIP(ip+":"+port);
	}
	/**
	 * 记录通用日志
	 * 
	 * @param type
	 * @param msg
	 */
	private static void doLog(String type, String msg) {
		PlainLogVO vo = new PlainLogVO();
		vo.setLogTime(new Date());
		vo.setLogType(type);
		vo.setLogContent(msg);

		threadLocal.get().addLog(vo);
	}

	/**
	 * 记录带有异常的通用日志
	 * 
	 * @param type
	 * @param t
	 */
	private static void doLog(String type, Throwable t) {
		PlainLogVO vo = new PlainLogVO();
		vo.setLogTime(new Date());
		vo.setLogType(type);
		vo.setException(t);

		threadLocal.get().addLog(vo);
	}

	private static void doLog(String type, String msg, Throwable t) {
		PlainLogVO vo = new PlainLogVO();
		vo.setLogTime(new Date());
		vo.setLogType(type);
		vo.setLogContent(msg);
		vo.setException(t);

		threadLocal.get().addLog(vo);
	}

	/**
	 * debug日志
	 */
	public static void debug(String msg) {
		if (LogConfig.logDebugEnabled) {
			doLog(Level.DEBUG.toString(), msg);
		}
	}

	/**
	 * info日志
	 */
	public static void info(String msg) {
		if (LogConfig.logInfoEnabled) {
			doLog(Level.INFO.toString(), msg);
		}
	}

	/**
	 * warn日志
	 */
	public static void warn(String msg) {
		if (LogConfig.logWarnEnabled) {
			doLog(Level.WARN.toString(), msg);
		}
	}

	/**
	 * fatal日志
	 * 
	 * @param msg
	 */
	public static void fatal(String msg) {
		if (LogConfig.logFatalEnabled) {
			doLog(Level.FATAL.toString(), msg);
		}
	}

	/**
	 * fatal日志
	 */
	public static void error(Throwable t) {
		if (LogConfig.logErrorEnabled) {
			doLog(Level.ERROR.toString(), t);
		}
	}

	/**
	 * error日志
	 * 
	 * @param msg
	 * @param t
	 */
	public static void error(String msg, Throwable t) {
		if (LogConfig.logErrorEnabled) {
			doLog(Level.ERROR.toString(), msg, t);
		}
	}
	
	/**
	 * 根据交易码生成记录交易日志
	 * @param msg  交易日志
	 * @param txcode 交易码
	 */
	public static void log(String msg,String txcode){
		//LogSynUtil.beginLog();
		threadLocal.get().setTxLog(true);
		threadLocal.get().setTxcode(txcode);
		PlainLogVO vo = new PlainLogVO();
		vo.setLogTime(new Date());
		vo.setLogType(Level.INFO.toString());
		vo.setLogContent(msg);

		threadLocal.get().addLog(vo);
		//LogSynUtil.endLog();
	}

	/**
	 * 整个交易日志结束
	 */
	public static void endLog() {
		TransactionLogVO vo = threadLocal.get();
		if (vo != null) {
			LogEngine.addTxLog(vo);
			threadLocal.remove();
		}
	}
	
	public static void systemout(String txNo, PlainLogVO logVO){
		StringBuilder buffer = new StringBuilder();

		buffer.append("||");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		String dateTime = sdf.format(logVO.getLogTime());
		buffer.append(dateTime);

		buffer.append("|");
		buffer.append(LogConfig.APP_CODE);
		buffer.append("|");
		buffer.append(LogConfig.SERVER_INSTANCE_CODE);

		buffer.append("|");
		if (txNo != null) {
			buffer.append(txNo);
		}

		buffer.append("|");
		if (logVO.getLogType() != null) {
			buffer.append(logVO.getLogType());
		}

		buffer.append("|");
		if (logVO != null) {
			buffer.append(logVO.getLogContent());
		}
		
		System.out.println(buffer.toString());
	}
}