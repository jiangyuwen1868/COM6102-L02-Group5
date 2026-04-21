package com.jyw.csp.datatransform.message.tx;

public class TxRequestMsgBodyCom1 {

	private String tenantId;
	private String branchId;
	private String nodeId;
	private String channelTxCode;
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getBranchId() {
		return branchId;
	}
	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}
	public String getChannelTxCode() {
		return channelTxCode;
	}
	public void setChannelTxCode(String channelTxCode) {
		this.channelTxCode = channelTxCode;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}
