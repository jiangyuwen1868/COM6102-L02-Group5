package com.jyw.csp.datatransform.message.tx;

import com.alibaba.fastjson.annotation.JSONField;

public class TxResponseMsg {

	@JSONField(name="tx_header")
	private TxResponseMsgHead msgHead;
	@JSONField(name="tx_body")
	private TxResponseMsgBody msgBody;
	public TxResponseMsgHead getMsgHead() {
		return msgHead;
	}
	public void setMsgHead(TxResponseMsgHead msgHead) {
		this.msgHead = msgHead;
	}
	public TxResponseMsgBody getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(TxResponseMsgBody msgBody) {
		this.msgBody = msgBody;
	}
}
