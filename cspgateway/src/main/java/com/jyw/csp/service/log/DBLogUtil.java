package com.jyw.csp.service.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jyw.csp.context.SessionContext;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;
import com.jyw.csp.entity.CspSrvLogEntity;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.service.CspSrvLogService;
import com.jyw.csp.util.JsonUtils;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.date.DateUtils;

public class DBLogUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(DBLogUtil.class);

	public static void saveDBLog(CspSrvLogService srvLogService, GwRequestMsg gwRequestMsg, GwResponseMsg gwResponseMsg, String txcode, SessionContext context) {
		try {

			// 记录日志
			CspSrvLogEntity entity = new CspSrvLogEntity();
			entity.setLogid(Utils.GUID());
			entity.setAppid(gwRequestMsg.getApp_id());
			entity.setNodeid("cspGateway");
			entity.setBranchid("cspGateway");
			entity.setTrace_id(gwResponseMsg.getSys_evt_trace_id());

			entity.setRecv_time(DateUtils.toDateInMillis(context.getSysRecvTime()));
			entity.setResp_time(DateUtils.toDateInMillis(context.getSysRespTime()));
			entity.setTxcode(txcode);

			String errorcode = gwResponseMsg.getReturn_code();
			String errormsg = gwResponseMsg.getReturn_message();
			String innererrcode = "";
			TxResponseMsg txResponseMsg = gwResponseMsg.getTxResponseMsg();
			if (txResponseMsg != null) {
				innererrcode = txResponseMsg.getMsgHead().getSys_resp_code();
			}
			String reqmsg = "";
			String rspmsg = "";
			String transret = "00";
			if (!ErrorInfo.SUCCESS.getCode().equals(errorcode)) {
				transret = "01";
				reqmsg = JsonUtils.formatJson(gwRequestMsg);
				rspmsg = JsonUtils.formatJson(gwResponseMsg);
			}
			String hostname = LocalMessage.getHostName();
			String hostip = LocalMessage.getLocalIP().get(0);
			String costtimeinfo = context.getStopWatch().toString();

			entity.setTransret(transret);
			entity.setHostname(hostname);
			entity.setHostip(hostip);
			entity.setErrorcode(errorcode);
			entity.setInnererrcode(innererrcode);
			entity.setErrormsg(errormsg);
			entity.setReqmsg(reqmsg);
			entity.setRspmsg(rspmsg);
			entity.setCosttimeinfo(costtimeinfo);

			int ret = srvLogService.save(entity);

			logger.debug("----saveDBLog:" + ret);
		} catch (Exception e) {
			// 忽略错误
			logger.error("saveDBLog", e);
		}
	}
}
