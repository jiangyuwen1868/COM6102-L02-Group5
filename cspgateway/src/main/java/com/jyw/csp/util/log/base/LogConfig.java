package com.jyw.csp.util.log.base;

import org.springframework.beans.factory.annotation.Value;


/**
 * 应用配置参数:从logcfg.properties中获取
 * 
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogConfig {
	/**
	 * 本机应用实例编码
	 */
	@Value("${APP_CODE:1001}")
	public static String APP_CODE = "1001";

	// 服务器运行实例编号
	public static String SERVER_INSTANCE_CODE = "002";

	// 日志线程池大小
	@Value("${logThreadPoolSize:10}")
	public static int logThreadPoolSize = 10;

	// 本地日志保存地址
	@Value("${logFilePath:./logs/log}")
	public static String logFilePath = "./logs/log";
	
	// 本地日志保存地址（备用）
	@Value("${logFilePath_second:./logs/log2}")
	public static String logFilePath_second = "./logs/log2";
	@Value("${logFileBufferSize:1048576}")
	public static int logFileBufferSize = 1048576;
	@Value("${logFileMaxSize:10485760}")
	public static long logFileMaxSize = 10485760;

	/**
	 * 每个线程批量处理数，超过这个数，则启动一个新的线程
	 */
	@Value("${logThreadBatchCount:10000}")
	public static int logThreadBatchCount = 10000;

	/**
	 * 日志队列栈大小
	 */
	@Value("${logQueueStackSize:10485760}")
	public static int logQueueStackSize = 10485760;
	@Value("${logConsoleEnabled:false}")
	public static boolean logConsoleEnabled = false;

	// 是否记录debug信息
	@Value("${logDebugEnabled:false}")
	public static boolean logDebugEnabled = true;

	// 是否记录info信息
	@Value("${logInfoEnabled:false}")
	public static boolean logInfoEnabled = true;

	// 是否记录wan信息
	@Value("${logWarnEnabled:true}")
	public static boolean logWarnEnabled = true;

	// 是否记录fatal信息
	@Value("${logFatalEnabled:true}")
	public static boolean logFatalEnabled = true;

	// 是否记录error信息
	@Value("${logErrorEnabled:true}")
	public static boolean logErrorEnabled = true;
	
	//是否每一条日志都需要刷新
	@Value("${logFlushEnabled:true}")
	public static boolean logFlushEnabled = true ;
	
	@Value("${logRollDirectoryTime:hour}")
	public static String logRollDirectoryTime;
	@Value("${logRollDirectoryTime:hour}")
	public static String logRollFileTime;

	public static String  encoding_CharSet = "UTF-8";

	
	public static String getLogRollDirectoryTime() {
	    String t = logRollDirectoryTime;
	    if (t.equals("hour"))
	      return "yyyy-MM-dd-HH";
	    if (t.equals("day"))
	      return "yyyy-MM-dd";
	    if (t.equals("minute"))
	      return "yyyy-MM-dd-HH-mm";
	    if (t.equals("midday"))
	      return "yyyy-MM-dd-a";
	    if (t.equals("week"))
	      return "yyyy-ww";
	    if (t.equals("month"))
	      return "yyyy-MM";

	    return "yyyy-MM-dd-HH";
	}
	public static String getLogRollFileTime() {
	    String t = "logRollFileTime";
	    if (t.equals("hour"))
	      return "yyyy-MM-dd-HH";
	    if (t.equals("day"))
	      return "yyyy-MM-dd";
	    if (t.equals("minute"))
	      return "yyyy-MM-dd-HH-mm";
	    if (t.equals("midday"))
	      return "yyyy-MM-dd-a";
	    if (t.equals("week"))
	      return "yyyy-ww";
	    if (t.equals("month"))
	      return "yyyy-MM";

	    return "yyyy-MM-dd-HH";
	  }
}