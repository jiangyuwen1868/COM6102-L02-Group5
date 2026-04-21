package com.jyw.csp.flowctrl;

import java.util.concurrent.atomic.AtomicInteger;

import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.util.string.StringUtils;

public class FlowCtrlPrimaryKeyVo {

	private String appid;
	private String txcode;
	
	private AtomicInteger atomicInteger = new AtomicInteger(1);
	
	public FlowCtrlPrimaryKeyVo(String ctrlPrimaryKey) {
		try {
			if(!StringUtils.checkEmpty(ctrlPrimaryKey)) {
				String[] keys = ctrlPrimaryKey.split(CspConstants.SEPARATOR_CONNECTOR_A);
				this.appid = keys[0];
				this.txcode = keys[1];
			}
		} catch(Exception e) {
		}
	}
	
	public FlowCtrlPrimaryKeyVo(String appid, String txcode) {
		this.appid = appid;
		this.txcode = txcode;
	}
	
	public Integer get() {
		return atomicInteger.get();
	}
	
	public Integer incrementAndGet() {
		return atomicInteger.incrementAndGet();
	}
	
	public Integer decrementAndGet() {
		return atomicInteger.decrementAndGet();
	}
	
	public void reset() {
		atomicInteger = null;
		atomicInteger = new AtomicInteger(1);
	}
	
	public String getFlowCtrlKey() {
		return this.appid + CspConstants.SEPARATOR_CONNECTOR_A + this.txcode;
	}
	
	@Override
	public String toString() {
		return this.appid + CspConstants.SEPARATOR_CONNECTOR_A + this.txcode;
	}
}
