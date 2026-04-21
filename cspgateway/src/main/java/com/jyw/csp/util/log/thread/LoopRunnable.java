package com.jyw.csp.util.log.thread;

import com.jyw.csp.util.log.LogSynUtil;

/**
 * 异步线程扫描日志队列
 * 
 * <p>Title: 异步调用日志文件操作类</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
abstract public class LoopRunnable implements Runnable {

	protected volatile boolean isRunning = false;
	
	/**
	 * 延迟等待计数：根据当前队列的数据情况进行延迟等待计算
	 */
	private int sleepCount = 0;

	/**
	 * 执行线程：通过循环的方式，一直执行，直到线程被中断或调用了stopService方法
	 */
	protected void doRun() {
		while (isRunning) {
			try {
				if (doRunInWhile()) {
					sleepCount = 0;
				} else {
					// 最多延迟等待一秒
					sleepCount = ++sleepCount % 10;
					sleepInWhile(sleepCount * 100);
				}
			} catch (Exception e) {
				processException(e);
			}
		}
	}

	protected void prepareRun() {
	}

	/**
	 * 处理消息异常
	 * 
	 * @param e
	 */
	protected void processException(Exception e) {
		LogSynUtil.error(this.getClass().getSimpleName(), e);
	}

	/**
	 * 在线程循环内调用，供子类实现业务逻辑。
	 * 
	 * @return true:有可处理的数据，false:无数据，空闲。
	 */
	abstract protected boolean doRunInWhile();

	/**
	 * 延迟指定时间，由于延迟过程中，可能线程有中断或Stopservice，所以内部采用循环的方式累加延迟。
	 * 
	 * @param millis
	 * @return
	 */
	public boolean sleepInWhile(long millis) {
		int step = 200;
		try{
			long count = millis / step;
			for (int i = 0; i < count; i++) {
				if (isRunning) {
					if (millis > 0) {
						Thread.sleep(millis);
					}
				} else {
					return false;
				}
			}
			
			if (millis > 0) {
				Thread.sleep(millis % step);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * spring对象销毁时调用
	 */
	public void stopService() {
		isRunning = false;
	}

	public void run() {
		isRunning = true;
		try {
			prepareRun();
			doRun();
		} finally {
			isRunning = false;
		}
	}

}