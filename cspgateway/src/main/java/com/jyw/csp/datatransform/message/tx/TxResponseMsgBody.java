package com.jyw.csp.datatransform.message.tx;

public class TxResponseMsgBody {

	private TxResponseMsgBodyEntity entity;
	private TxResponseMsgBodyCom1 com1;
	public TxResponseMsgBodyEntity getEntity() {
		return entity;
	}

	public void setEntity(TxResponseMsgBodyEntity entity) {
		this.entity = entity;
	}

	public TxResponseMsgBodyCom1 getCom1() {
		return com1;
	}

	public void setCom1(TxResponseMsgBodyCom1 com1) {
		this.com1 = com1;
	}
}
