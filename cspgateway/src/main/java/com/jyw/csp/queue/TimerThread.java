package com.jyw.csp.queue;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

public class TimerThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(TimerThread.class);

    /**
     * 定时器控制值
     */
    private long timerDate;

    /**
     * 共享目录
     */
    private String sharedDir;

    public TimerThread(long timerDate, String sharedDir) {
        this.timerDate = timerDate;
        this.sharedDir = sharedDir;
    }

    @Override
    public void run() {

        // 创建定时器
        java.util.Timer timer = new java.util.Timer();
        // 创建定时器任务
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("定时器时间到了，执行修改文件后缀操作!");
                // 修改文件后缀
                editFile(sharedDir);
            }
        };

        timer.scheduleAtFixedRate(timerTask, new Date(), timerDate);
    }


    /**
     * 替换文件后缀
     * @param srcSource
     */
    private void editFile(String srcSource) {
        File dir = new File(srcSource);
        // 过滤文件
        FileFilter fileFilter = new WildcardFileFilter("*trance.dat");
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            // 旧的文件或目录
            System.out.println(files[i].getParent());
            System.out.println(files[i].getName());
            String fileName = files[i].getName();
            String from = "dat";
            File subFile = files[i];
            String to = "log";
            //
            if (fileName.endsWith(from)) {
                UUID uuid = java.util.UUID.randomUUID();
                String[] idd = uuid.toString().split("-");
                subFile.renameTo(new File(subFile.getParent() + "/" + idd[1] + idd[2] + idd[3] + "_time_" + fileName.substring(0, fileName.indexOf(from)) + to));
            }
        }


    }
}
