package com.jyw.csp.queue;

import com.jyw.csp.entity.CspSrvLogEntity;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.string.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * 交易消费线程（用于消费队列中的数据）
 */
public class ConsurmerThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ConsurmerThread.class);


    /**
     * 加密机交易文件共享目录
     */
    private String sharedDir;
    // 够多少条数据则进行文件修改
    private Integer monitorTranceTotal;

    /**
     * 计数器
     */
    private int counter = 0;

    public ConsurmerThread(String sharedDir, Integer monitorTranceTotal) {
        this.sharedDir = sharedDir;
        this.monitorTranceTotal = monitorTranceTotal;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                // 加锁
                logger.info("监听消费Trans中----");
                CspSrvLogEntity entity = (CspSrvLogEntity) TranceLogQueue.getLogQueue().take();
                logger.info("消费者消费：appId<" + entity.getAppid() + "> reqTime<" + entity.getRecv_time() + ">");
                // 添加日志文件
                addLogFile(entity);

            } catch (InterruptedException e) {
                logger.error("ConsurmerThread线程出现异常----");
                e.printStackTrace();
            }
        }
    }


    /**
     * 生成加密机交易日志
     *
     * @param entity
     */
    private void addLogFile(CspSrvLogEntity entity) {
        logger.info("sharedDir 路径为:" + sharedDir);
        StringBuilder sb = new StringBuilder();
        sb.append("appId<");
        sb.append(entity.getAppid() == null ? "" : entity.getAppid());
        sb.append("> ");
        sb.append("reqTime<");
        sb.append(entity.getRecv_time() == null ? "" : entity.getRecv_time().getTime());
        sb.append("> ");
        sb.append("resTime<");
        sb.append(entity.getResp_time() == null ? "" : entity.getResp_time().getTime());
        sb.append("> ");
        sb.append("reqCode<");
        sb.append(entity.getTxcode() == null ? "" : entity.getTxcode());
        sb.append("> ");
        sb.append("traceId<");
        sb.append(entity.getTrace_id() == null ? "" : entity.getTrace_id());
        sb.append("> ");
        sb.append("errorCode<");
        sb.append(handleErrorCode(entity));
        sb.append("> ");
        sb.append("errorMsg<");
        sb.append(entity.getErrormsg() == null ? "" : entity.getErrormsg());
        sb.append("> ");

        String content = sb.toString();

        try {
            String hostName = LocalMessage.getHostName();
            String fileName = hostName + "_trance.dat";
            String path = sharedDir + System.getProperty("file.separator") + fileName;
            File file = new File(path);

            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            //使用true，即进行append file
            FileWriter fileWritter = new FileWriter(file, true);
            fileWritter.write(content + "\r\n");

            fileWritter.close();
            logger.info(sharedDir + fileName + "生成日志成功");
            // 计数器加1
            counter += 1;
            if (monitorTranceTotal == counter) {
                editFileSuffix(file, "dat", "log");
                // 计数器归零
                counter = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 修改文件后缀
     *
     * @param file 原始文件
     * @param from 后缀名称
     * @param to   要转成的文件后缀
     */
    public void editFileSuffix(File file, String from, String to) {
        String sourceFileName = file.getName();
        logger.info("执行文件后缀修改");
        UUID uuid = java.util.UUID.randomUUID();
        String[] idd = uuid.toString().split("-");
        file.renameTo(new File(file.getParent() + "/" + idd[1] + idd[2] + idd[3] + "_counter_" + sourceFileName.substring(0, sourceFileName.indexOf(from)) + to));
    }

    private String handleErrorCode(CspSrvLogEntity entity) {
        String succesStr = "000000000000";
        String innererrcode = entity.getInnererrcode();
        // 如果内部错误码为空  直接返回 错误码
        if (StringUtils.isEmpty(innererrcode)) {
            return entity.getErrorcode();
        }
        // 如果内部错误码异常直接返回
        if (!succesStr.equals(innererrcode)) {
            return innererrcode;
        }
        String errorcode = entity.getErrorcode();
        return errorcode;
    }
}
