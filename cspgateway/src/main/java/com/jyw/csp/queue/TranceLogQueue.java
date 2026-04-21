package com.jyw.csp.queue;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Configuration
@Component
public class TranceLogQueue {

    private static BlockingQueue<String> logQueue;

    public static BlockingQueue getLogQueue() {
        if (null == logQueue) {
            logQueue = new LinkedBlockingQueue<>();
        }
        return logQueue;
    }

    private void putStr(String str) {
        try {
            logQueue.put(str);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getStr() {
        String take = null;
        try {
            take = logQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return take;
    }
}
