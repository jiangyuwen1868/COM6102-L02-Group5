package com.jyw.csp.service.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class MyFileLogger {
	public static Logger getLogger(String name,String log_home,String maxFileSize,String totalSizeCap){
		//LoggerContext context = new LoggerContext();
        //Logger logger = context.getLogger("FILE-" + name);
		
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("FILE-" + name);
        
		//Logger logger = (Logger) LoggerFactory.getLogger("FILE-" + name);
	    //LoggerContext context = new LoggerContext(); 
		
        //这里是可以用来设置appender的，在xml配置文件里面，是这种形式：
        // <appender name="error" class="ch.qos.logback.core.rolling.RollingFileAppender">
	    RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
        
        //appender的name属性
        appender.setName("FILE-" + name);
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        appender.setContext(context);
       
        //设置文件名
       /* String fileName = log_home + "/" + name + ".log";
        appender.setFile(OptionHelper.substVars(fileName,context));*/
        appender.setAppend(true);
        appender.setPrudent(false);
        appender.setFile(log_home+"/"+name+".log");
        
        
        //设置文件创建时间及大小的类
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        //设置文件名模式
        String fileNamePattern = log_home + "/%d{yyyy-MM-dd}/"+name + "-%d{yyyy-MM-dd}-%i.log.gz";
        policy.setFileNamePattern(OptionHelper.substVars(fileNamePattern,context));
        //最大日志文件大小
        policy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        //设置最大历史记录为7条??
        policy.setMaxHistory(30);
        //总大小限制
        policy.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
        //设置父节点是appender
        policy.setParent(appender);
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        policy.setContext(context);
        policy.start();
        
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        encoder.setPattern("%msg%n");
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.start();

        //加入下面两个节点
        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        /*if("DEBUG".equals(level)) {
        	logger.setLevel(Level.DEBUG);
        }else if("INFO".equals(level)) {
        	logger.setLevel(Level.INFO);
        }else {
        	logger.setLevel(Level.ERROR);
        }*/
        
        logger.setLevel(Level.INFO);
       
        logger.setAdditive(false);
        logger.addAppender(appender);
      
        
       /* RollingFileAppender appender2 = (RollingFileAppender)logger.getAppender("FILE-" + name);
		System.out.println(appender2);
		System.out.println(appender2.getFile());
        
		logger.debug("FILE-" + name);*/
		
        return logger;  
    }
}
