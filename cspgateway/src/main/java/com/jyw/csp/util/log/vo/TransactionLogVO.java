package com.jyw.csp.util.log.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 一笔完整交易日志VO
 *  
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class TransactionLogVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean isTest;
	/**
	 * 交易流水号
	 */
	private String txNo;
	/**
	 * 远程ip地址
	 */
	private String remoteIP;
	
	/**
	 * 是否为交易日志
	 */
	private boolean isTxLog;
	
	/**
	 * 交易代码/服务代码
	 */
	private String txcode;

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	private final List<PlainLogVO> plainLogList = new ArrayList<PlainLogVO>();

	public boolean isTest() {
		return isTest;
	}

	public void setTest(boolean isTest) {
		this.isTest = isTest;
	}

	public void addLog(PlainLogVO vo) {
		plainLogList.add(vo);
	}

	public List<PlainLogVO> getCommonLogList() {
		return plainLogList;
	}

	public String getTxNo() {
		return txNo;
	}

	public void setTxNo(String txNo) {
		this.txNo = txNo;
	}

	public boolean isTxLog() {
		return isTxLog;
	}

	public void setTxLog(boolean isTxLog) {
		this.isTxLog = isTxLog;
	}

	public String getTxcode() {
		return txcode;
	}

	public void setTxcode(String txcode) {
		this.txcode = txcode;
	}

}