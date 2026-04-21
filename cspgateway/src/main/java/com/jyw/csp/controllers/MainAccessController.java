package com.jyw.csp.controllers;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.aop.TxTraceLogAction;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.SessionContext;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;
import com.jyw.csp.datatransform.message.tx.TxRequestMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsgBody;
import com.jyw.csp.datatransform.message.tx.TxResponseMsgBodyCom1;
import com.jyw.csp.datatransform.message.tx.TxResponseMsgHead;
import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.enums.AppAuthType;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.exception.ActionFlowExceedException;
import com.jyw.csp.exception.CommonException;
import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.flowctrl.FlowControl;
import com.jyw.csp.monitor.MonitorFilter;
import com.jyw.csp.mq.msg.AlarmLevel;
import com.jyw.csp.mq.msg.AlarmMsgData;
import com.jyw.csp.mq.msg.AlarmType;
import com.jyw.csp.service.cache.CspAppInfoCache;
import com.jyw.csp.service.cache.CspErrorInfoCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.util.Base64;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.chiper.AESCipherUtils;
import com.jyw.csp.util.chiper.DESedeCipherUtils;
import com.jyw.csp.util.chiper.RSAUtils;
import com.jyw.csp.util.date.DateUtils;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RestController
public class MainAccessController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/mainAccess", consumes = "application/json", produces = "application/json; charset=utf-8")
	//@TxAuthAction
	@RateLimiter(name = "accessRateLimiter", fallbackMethod = "rateLimiterFallback")
    @TimeLimiter(name = "accessTimeLimiter")
	@TxTraceLogAction
	public CompletableFuture<GwResponseMsg> accessServer(@RequestBody String message, HttpServletRequest request) {
		logger.debug("----AccessServer message----\n" + message);
		GwResponseMsg responseMsg = new GwResponseMsg();
		// 特殊处理客户端API心跳发送的探测数据
		if(isClientHeartCheckMsg(message)) {
			responseMsg.setReturn_code(CspConstants.HEARTCHECK_CODE);
			return CompletableFuture.completedFuture(responseMsg);
		}
		MonitorFilter.addTranct();
		SessionContext context = SessionContext.getCurrentContext();
		StopWatch sw = context.getStopWatch();
		GwRequestMsg requestMsg = null;
		String plaintextRequestInfo = null;
		String traceId = BaseController.genSysEvtTraceId();
		String opTpsKey = "";
		try {
			
			requestMsg = JSONObject.parseObject(message, GwRequestMsg.class);
			checkRequestMsg(requestMsg);

			String appid = requestMsg.getApp_id();
			CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);
			
			// 授权验证总开关
            String authEnable = CspSysConfigCache.getStrValue("cspGateway.authEnable", "true");
            if(Boolean.valueOf(authEnable)) {
            	
                String authtype = appInfo.getAuthtype();
                // 需认证
        		if(!AppAuthType.AUTH07.getCode().equals(authtype)) {
	                //IP白名单验证
	                sw.start("doAuthCheckIpWhitelist");
	                boolean authResult = doAuthCheckIpWhitelist(requestMsg, request);
	                sw.stop();
	                if(authResult) {
	                	// 签名授权验证
	                	sw.start("doAuthCheck");
	                    authResult = doAuthCheck(message, requestMsg, request);
	                    sw.stop();
	                }
        		}
            }
			
			
			String authtype = appInfo.getAuthtype();
			String appsecret = appInfo.getAppsecret();
			boolean isResponseInfoEncrypt = false;
			if (CspConstants.SEC_VERSION_02.equals(requestMsg.getSec_version()) 
					|| AppAuthType.AUTH03.getCode().equals(authtype) 
					|| AppAuthType.AUTH04.getCode().equals(authtype)
					|| AppAuthType.AUTH05.getCode().equals(authtype)) {
				isResponseInfoEncrypt = true;
				try {
					sw.start("decrypt");
					plaintextRequestInfo = DESedeCipherUtils.decryptBase64(appsecret, requestMsg.getRequest_info());
					sw.stop();
				} catch(CommonRuntimeException e) {
					throw e;
				} catch (Exception e) {
					logger.error("decrypt request_info", e);
					throw new CommonRuntimeException("XBISDEC10002", "请求数据解密失败");
				}
			} else {
				sw.start("decrypt-b");
				plaintextRequestInfo = Base64.decodeToString(requestMsg.getRequest_info());
				sw.stop();
			}
//			sw.start("sha1");
//			MessageDigest md = MessageDigest.getInstance(CspConstants.SHA1);   
//	        md.update(plaintextRequestInfo.getBytes());
//	        String signature = Utils.bytes2Hex(md.digest());
//	        sw.stop();
//			
//			if(!requestMsg.getSignature().equalsIgnoreCase(signature)) {
//				throw new CommonRuntimeException("XBISDEC10001", "请求数据解密有误");
//			}
			
			boolean verifySign = RSAUtils.verifyBase64(plaintextRequestInfo.getBytes(), CLIENT_PUBLICKEY, requestMsg.getSignature());
			if(!verifySign) {
				throw new CommonRuntimeException("XBISIGN10001", "客户端签名验证失败");
			}
			
			requestMsg.setPlaintextRequestInfo(plaintextRequestInfo);
			TxRequestMsg txRequestMsg = (TxRequestMsg) JSONObject.parseObject(plaintextRequestInfo, TxRequestMsg.class);
			requestMsg.setTxRequestMsg(txRequestMsg);
			
			String txcode = txRequestMsg.getMsgHead().getSys_txcode();
			
			doCheckSrvTxCodeAuth(appid, txcode);
			
			traceId = txRequestMsg.getMsgHead().getSys_evt_trace_id();
			
			opTpsKey = appid + CspConstants.SEPARATOR_CONNECTOR_A + txcode;

			FlowControl.opTpsLock(opTpsKey);
			String txResponseInfo = null;
			if(CspConstants.CSP_TS_TXCODES.contains(txcode)) {
				txResponseInfo = accessToTSA(appid, plaintextRequestInfo, txRequestMsg);
				TxResponseMsg txResponseMsg = new TxResponseMsg();
				TxResponseMsgHead msgHead = new TxResponseMsgHead();
				msgHead.setSys_resp_code(ErrorInfo.SUCCESS.getCode());
				msgHead.setSys_resp_desc(ErrorInfo.SUCCESS.getInfo());
				msgHead.setSys_txcode(txcode);
				
				JSONObject jsonObj = JSON.parseObject(txResponseInfo);
				String errorCode = jsonObj.getString("errorCode");
	        	if(errorCode!=null) {
	        		boolean success = jsonObj.getBooleanValue("success");
	        		if(!success) {
	        			msgHead.setSys_resp_code(errorCode);
	        			msgHead.setSys_resp_desc("密码机处理失败");
	        		}
	        	}
	        	int code = jsonObj.getIntValue("code");
	        	if(code!=200) {
	        		msgHead.setSys_resp_code(String.valueOf(code));
        			msgHead.setSys_resp_desc("密码机处理失败");
	        	}
	        	
	        	txResponseMsg.setMsgHead(msgHead);
	        	TxResponseMsgBody msgBody = new TxResponseMsgBody();
	        	msgBody.setCom1(new TxResponseMsgBodyCom1());
				txResponseMsg.setMsgBody(msgBody);
				responseMsg.setTxResponseMsg(txResponseMsg);
			} else if(txcode.startsWith(CspConstants.CSP_SVS_TXCODES_PREFIX)) {
				txResponseInfo = accessToTSA(appid, plaintextRequestInfo, txRequestMsg);
				TxResponseMsg txResponseMsg = new TxResponseMsg();
				TxResponseMsgHead msgHead = new TxResponseMsgHead();
				msgHead.setSys_resp_code(ErrorInfo.SUCCESS.getCode());
				msgHead.setSys_resp_desc(ErrorInfo.SUCCESS.getInfo());
				msgHead.setSys_txcode(txcode);
				
				JSONObject jsonObj = null;
	        	boolean isError = false;
	        	try {
	        		jsonObj = JSON.parseObject(txResponseInfo);
	        		isError = true;
	        	} catch(Exception e) {}
	        	if(isError) {
	        		String errorCode = jsonObj.getString("code");
	        		String errorMsg = jsonObj.getString("message");
	        		msgHead.setSys_resp_code(errorCode);
        			msgHead.setSys_resp_desc("密码机处理失败-" + errorMsg);
	        	}
	        	txResponseMsg.setMsgHead(msgHead);
	        	TxResponseMsgBody msgBody = new TxResponseMsgBody();
	        	msgBody.setCom1(new TxResponseMsgBodyCom1());
				txResponseMsg.setMsgBody(msgBody);
				responseMsg.setTxResponseMsg(txResponseMsg);
			} else {
				txResponseInfo = accessToService(appid, plaintextRequestInfo, txRequestMsg);
				TxResponseMsg txResponseMsg = JSONObject.parseObject(txResponseInfo, TxResponseMsg.class);
				// 重置Entity 为null，防止controller 转JSON对象报反序列化异常：InvalidDefinitionException
				if(txResponseMsg.getMsgBody()!=null)
					txResponseMsg.getMsgBody().setEntity(null);
				responseMsg.setTxResponseMsg(txResponseMsg);
			}
			
			logger.debug("txResponseInfo-->" + txResponseInfo);
			
			responseMsg.setPlaintextResponseInfo(txResponseInfo);

			if (isResponseInfoEncrypt) {
				try {
					sw.start("encrypt");
					String txResponseInfoB4 = AESCipherUtils.encryptToBase64(appsecret, txResponseInfo);
					sw.stop();
					responseMsg.setResponse_info(txResponseInfoB4);
				} catch (Exception e) {
					throw new CommonRuntimeException("XBISENC10002", "响应数据加密失败");
				}
			} else {
				responseMsg.setResponse_info(Base64.encodeString(txResponseInfo.getBytes()));
			}

			responseMsg.setReturn_code(ErrorInfo.SUCCESS.getCode());
			responseMsg.setReturn_message(ErrorInfo.SUCCESS.getInfo());

			logger.debug(context.getStopWatch().prettyPrint());
		} catch (ActionFlowExceedException e) {
			MonitorFilter.addSysRefuse();
			responseMsg.setReturn_code(e.getCode());
			responseMsg.setReturn_message(e.getMessage());
		} catch (CommonRuntimeException e) {
			MonitorFilter.addTranctError();
			responseMsg.setReturn_code(e.getCode());
			responseMsg.setReturn_message(e.getMessage());
		} catch (CommonException e) {
			MonitorFilter.addTranctError();
			responseMsg.setReturn_code(e.getCode());
			responseMsg.setReturn_message(e.getMessage());
		} catch (Throwable e) {
			MonitorFilter.addSysError();
			responseMsg.setReturn_code(ErrorInfo.UNKNOWN.getCode());
			responseMsg.setReturn_message(ErrorInfo.UNKNOWN.getInfo() + "-" + e.getMessage());
			logger.error(ErrorInfo.UNKNOWN.getInfo(), e);
			
			//未知错误
			AlarmMsgData alarmMsgData = new AlarmMsgData();
			alarmMsgData.setAlarmContent(ErrorInfo.UNKNOWN.getInfo());
			alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
			alarmMsgData.setAlarmLevel(AlarmLevel.WARN.getLevel());
			alarmMsgData.setAlarmType(AlarmType.GATEWAY.getType());
			alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
			alarmMsgData.setAlarmHostDesc("cpsGateway");
			alarmMsgData.setAlarmDesc(e.getMessage());
//			MqProvider.sendAlarmMessage(alarmMsgData);
		} finally {
			responseMsg.setSys_evt_trace_id(traceId);
			long currentTime = System.currentTimeMillis();
			responseMsg.setSrv_costtime(String.valueOf(currentTime - context.getSysRecvTime()));
			context.setGwRequestMsg(requestMsg);
			context.setGwResponseMsg(responseMsg);
			context.setSysRespTime(currentTime);
			// 转译错误信息
			String errormsg = CspErrorInfoCache.getErrorMsg(responseMsg.getReturn_code(), responseMsg.getReturn_message());
			responseMsg.setReturn_message(errormsg);
			
			FlowControl.opTpsUnlock(opTpsKey);
		}
		return CompletableFuture.completedFuture(responseMsg);
	}

	public CompletableFuture<GwResponseMsg> rateLimiterFallback(@RequestBody String message, HttpServletRequest request, RequestNotPermitted ex) {
		logger.warn("Rate limiter fallback triggered: error={}",  ex.getMessage());
		SessionContext context = SessionContext.getCurrentContext();
		String traceId = BaseController.genSysEvtTraceId();
		
		GwResponseMsg responseMsg = new GwResponseMsg();
		
		responseMsg.setSys_evt_trace_id(traceId);
		long currentTime = System.currentTimeMillis();
		responseMsg.setSrv_costtime(String.valueOf(currentTime - context.getSysRecvTime()));
		responseMsg.setReturn_code(CspConstants.RATELIMITER_CODE);
		responseMsg.setReturn_message(CspErrorInfoCache.getErrorMsg(CspConstants.RATELIMITER_CODE, "请求频繁，已启动限流"));
		context.setSysRespTime(currentTime);
		
		return CompletableFuture.completedFuture(responseMsg);
	}
}
