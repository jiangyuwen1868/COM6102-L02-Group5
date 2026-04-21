package com.jyw.csp.datatransform.message.gw;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;

public class GwResponseMsg {

	private String return_code;
	private String return_message;
	private String sys_evt_trace_id;
	private String srv_costtime;
	private String response_info;
	
	@JSONField(serialize = false)
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonIgnore  //返回时排除掉这个字段
	private TxResponseMsg txResponseMsg;
	@JSONField(serialize = false)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnore  //返回时排除掉这个字段
	private String plaintextResponseInfo;
	
	public String getReturn_code() {
		return return_code;
	}
	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}
	public String getReturn_message() {
		return return_message;
	}
	public void setReturn_message(String return_message) {
		this.return_message = return_message;
	}
	public String getSys_evt_trace_id() {
		return sys_evt_trace_id;
	}
	public void setSys_evt_trace_id(String sys_evt_trace_id) {
		this.sys_evt_trace_id = sys_evt_trace_id;
	}
	public String getSrv_costtime() {
		return srv_costtime;
	}
	public void setSrv_costtime(String srv_costtime) {
		this.srv_costtime = srv_costtime;
	}
	public String getResponse_info() {
		return response_info;
	}
	public void setResponse_info(String response_info) {
		this.response_info = response_info;
	}
	public TxResponseMsg getTxResponseMsg() {
		return txResponseMsg;
	}
	public void setTxResponseMsg(TxResponseMsg txResponseMsg) {
		this.txResponseMsg = txResponseMsg;
	}
	public String getPlaintextResponseInfo() {
		return plaintextResponseInfo;
	}
	public void setPlaintextResponseInfo(String plaintextResponseInfo) {
		this.plaintextResponseInfo = plaintextResponseInfo;
	}
}
