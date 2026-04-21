package com.jyw.csp.datatransform.message.tx;

import com.alibaba.fastjson.annotation.JSONField;

public class TxRequestMsg {

	@JSONField(name="tx_header")
	private TxRequestMsgHead msgHead;
	@JSONField(name="tx_body")
	private TxRequestMsgBody msgBody;
	public TxRequestMsgHead getMsgHead() {
		return msgHead;
	}
	public void setMsgHead(TxRequestMsgHead msgHead) {
		this.msgHead = msgHead;
	}
	public TxRequestMsgBody getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(TxRequestMsgBody msgBody) {
		this.msgBody = msgBody;
	}
}
