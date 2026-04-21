package com.jyw.csp.vo;

import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.datatransform.message.tx.TxResponseMsgBodyEntity;

public class CSP000000OutVo implements TxResponseMsgBodyEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7639324428470563328L;
	private String hsmrspdata;
	private JSONObject hsmrspdataJson;

	public String getHsmrspdata() {
		return hsmrspdata;
	}

	public void setHsmrspdata(String hsmrspdata) {
		this.hsmrspdata = hsmrspdata;
	}

	public JSONObject getHsmrspdataJson() {
		return hsmrspdataJson;
	}

	public void setHsmrspdataJson(JSONObject hsmrspdataJson) {
		this.hsmrspdataJson = hsmrspdataJson;
	}
}
