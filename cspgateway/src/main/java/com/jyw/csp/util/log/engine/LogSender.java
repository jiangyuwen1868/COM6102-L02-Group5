package com.jyw.csp.util.log.engine;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.jyw.csp.util.log.thread.LoopRunnable;
import com.jyw.csp.util.log.vo.TransactionLogVO;


/**
 * 
 * 日志消息发送器线程：管理队列数据，并负责将日志消息发送到日志执行线程
 * 
 * <p>Title: 日志消息发送器线程</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LogSender extends LoopRunnable {

	// 需要发送到jms的消息队列
	private final ConcurrentLinkedQueue<TransactionLogVO> queue = new ConcurrentLinkedQueue<TransactionLogVO>();

	LogFileEngine logFileEngine;
	
	private volatile boolean offline = false;

	@Override
	protected void prepareRun() {
		logFileEngine = new LogFileEngine();
	}

	/**
	 * 将日志对象压入队列中
	 * 
	 * @param txLogVo
	 */
	public int pushObject(TransactionLogVO txLogVo) {
		queue.offer(txLogVo);
		return queue.size();
	}

	@Override
	public void stopService() {
		super.stopService();
		logFileEngine.closeFile();

		// System.out.println("sender stop:" + senderNum);
	}

	public void preDestroy() {
		offline = true;
	}

	public int getQueueSize() {
		return queue.size();
	}

	/**
	 * 线程执行循环内逻辑
	 */
	@Override
	protected boolean doRunInWhile() {
		TransactionLogVO txLogVo = queue.poll();
		if (txLogVo != null) {
			logFileEngine.logToFile(txLogVo);
			return true;
		} else {
			logFileEngine.flushFile();
			// 如果是脱机状态，则执行完毕就停止线程
			if (offline) {
				stopService();
				System.out.println(Thread.currentThread().getName()
						+ " LogSender run ok!");
			}
			return false;
		}
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

}