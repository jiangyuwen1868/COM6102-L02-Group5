package com.jyw.csp.util.log.engine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import com.jyw.csp.util.log.base.LogConfig;
import com.jyw.csp.util.log.vo.TransactionLogVO;


/**
 * 
 * 日志引擎
 * 
 * <p>Title: 日志引擎</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogEngine {
	private static LogSender logSender = null;
	
	private static ExecutorService executorService;

	private static String LOG_LOCK = "LOG_LOCK";

	static {
		executorService = Executors
				.newFixedThreadPool(LogConfig.logThreadPoolSize);
		startupNewLogSender();
	}

	/**
	 * 启动新的日志发送器
	 */
	private static void startupNewLogSender() {
		if (logSender != null) {
			logSender.setOffline(true);
		}

		logSender = new LogSender();
		executorService.execute(logSender);

		// System.out.println("startupNewLogSender..........");
	}

	public static int getQueueSize() {
		return logSender.getQueueSize();
	}

	/**
	 * 添加交易日志
	 * 
	 * @param txLogVo
	 */
	public static void addTxLog(TransactionLogVO vo) {
		synchronized (LOG_LOCK) {
			int count = logSender.pushObject(vo);
			if (count > LogConfig.logThreadBatchCount) {
				startupNewLogSender();
			}
		}
	}

	/**
	 * 在系统停止时，执行销毁动作
	 */
	@PreDestroy
	public void preDestroy() {
		logSender.preDestroy();
		executorService.shutdown();
	}

	/**
	 * 删除过期日志文件,定时任务:每天执行一次(凌晨3点执行）
	 * 
	 * 日志有效期：保留一个月
	 * 
	 * spring定时参考：http://blog.sina.com.cn/s/blog_6d59e57d01012v50.html
	 */
	//@Scheduled(cron = "0 0 3 * * *")
	public void deleteExpiredFiles() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
		String date = sdf.format(calendar.getTime());
		String path = LogConfig.logFilePath + LogConfig.APP_CODE
				+ File.separatorChar;
		File pathFile = new File(path);
		String[] dirNames = pathFile.list();
		for (String dirName : dirNames) {
			if (date.compareTo(dirName) >= 0) {
				File dirFile = new File(dirName);
				deleteDir(dirFile);
			}
		}

		FileNoGenerator.clearExpiredData();
	}

	/**
	 * 删除目录
	 * 
	 * @param dirFile
	 */
	private void deleteDir(File dirFile) {
		String[] fileNames = dirFile.list();
		for (String fileName : fileNames) {
			File f = new File(fileName);
			f.delete();
		}

		dirFile.delete();
	}

}