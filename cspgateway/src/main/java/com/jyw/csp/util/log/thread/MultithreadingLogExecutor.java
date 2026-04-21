package com.jyw.csp.util.log.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadingLogExecutor {
	private static final ExecutorService executorService = Executors.newFixedThreadPool(100);

	public static void shutdown() {
		executorService.shutdown();
	}

	public static void addLogTask(Runnable logTask) {
		executorService.execute(logTask);
	}
}
