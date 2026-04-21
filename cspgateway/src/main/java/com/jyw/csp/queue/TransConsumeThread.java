package com.jyw.csp.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import com.jyw.csp.context.ApplicationContextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class TransConsumeThread implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TransConsumeThread.class);

    public TransConsumeThread() {

        Properties properties = getProperties("application.yml");

        String sharedDir = properties.getProperty("sharedDir");
        Long timerDate = Long.valueOf(properties.getProperty("timerDate"));
        Integer monitorTranceTotal = Integer.valueOf(properties.getProperty("monitorTranceTotal"));
        logger.info("moveFileTotal配置文件中的：{}", monitorTranceTotal);

        ConsurmerThread consurmer = new ConsurmerThread(sharedDir, monitorTranceTotal);
        Thread thread = new Thread(consurmer);
        thread.start();

        TimerThread timerThread = new TimerThread(timerDate, sharedDir);
        Thread timerThreadStart = new Thread(timerThread);
        timerThreadStart.start();
    }


    @Override
    public void destroy() {
        logger.info("关机了，进行销毁ConsurmerThread");
        ConsurmerThread consurmer = ApplicationContextUtils.getBeanByName(ConsurmerThread.class);
        TimerThread TimerThread = ApplicationContextUtils.getBeanByName(TimerThread.class);
        // 关机时销毁
        consurmer.interrupt();
        TimerThread.interrupt();
    }


    /**
     * 获取配置文件
     *
     * @param fileName
     * @return
     */
    private static Properties getProperties(String fileName) {
        try {
            String outpath = System.getProperty("user.dir") + File.separator + "config" + File.separator;//先读取config目录的，没有再加载classpath的
            Properties properties = new Properties();
            InputStream in = new FileInputStream(new File(outpath + fileName));
            properties.load(in);
            return properties;
        } catch (IOException e) {
            try {
                Properties properties = new Properties();
                InputStream in = TransConsumeThread.class.getClassLoader().getResourceAsStream(fileName);//默认加载classpath的
                properties.load(in);
                return properties;
            } catch (IOException es) {
                logger.error(es.getMessage());
                return null;
            }
        }
    }

}



