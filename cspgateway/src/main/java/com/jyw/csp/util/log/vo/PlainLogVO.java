package com.jyw.csp.util.log.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 普通日志VO
 *  
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class PlainLogVO implements Serializable {

	private static final long serialVersionUID = 1L;

	// 日志时间
	private Date logTime;

	// 日志类型
	private String logType;

	// 日志内容
	private String logContent;

	// 异常
	private transient Throwable exception;

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getLogContent() {
		return logContent;
	}

	public void setLogContent(String logContent) {
		this.logContent = logContent;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

}