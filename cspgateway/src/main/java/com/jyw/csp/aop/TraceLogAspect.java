package com.jyw.csp.aop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StopWatch.TaskInfo;

import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.SessionContext;
import com.jyw.csp.controllers.BaseController;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;
import com.jyw.csp.entity.CspInitKeysEntity;
import com.jyw.csp.entity.CspSrvLogEntity;
import com.jyw.csp.entity.SysTabVsignEntity;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.mq.msg.TxMsgData;
import com.jyw.csp.queue.TranceLogQueue;
import com.jyw.csp.service.CspSrvLogService;
import com.jyw.csp.service.SysTabVsignService;
import com.jyw.csp.service.cache.CspInitKeysCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.service.log.TraceLog;
import com.jyw.csp.service.log.TraceLogger;
import com.jyw.csp.util.JsonUtils;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.date.DateUtils;
import com.jyw.csp.util.log.thread.MultithreadingLogExecutor;
import com.jyw.csp.util.string.StringUtils;
import com.jyw.csp.vo.CSP000000OutVo;

@Component
@Aspect // 声明一个切面
public class TraceLogAspect implements Ordered {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceLogger traceLogger;
    @Autowired
    private CspSrvLogService srvLogService;
    @Autowired
    private SysTabVsignService sysTabVsignService;

    private final static String LOG_TABLE_NAME = "csp_srv_log";

    @Override
    public int getOrder() {
        return 99;
    }

    @Pointcut("@annotation(com.jyw.csp.aop.TxTraceLogAction)") // 声明一个切点
    public void TxTraceLogPointCut() {
    }

    ;

    @Around("TxTraceLogPointCut()")
    public Object doAroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        logger.debug("==========TraceLogAspect->doAround begin");
        SessionContext context = SessionContext.getCurrentContext();
        Object object = null;
        try {
        	object = pjp.proceed();
        	
        	
        	if (object instanceof CompletableFuture) {
                CompletableFuture<GwResponseMsg> future = (CompletableFuture<GwResponseMsg>) object;
                // 在 whenComplete 回调中获取实际的 GwResponseMsg
                future.whenComplete((gwResponseMsg, throwable) -> {
                    if (throwable == null) {
                        // 这里拿到了真实的 GwResponseMsg 对象
                    	try {
                        	if(CspConstants.HEARTCHECK_CODE.equals(gwResponseMsg.getReturn_code()) || 
                        			CspConstants.RATELIMITER_CODE.equals(gwResponseMsg.getReturn_code())) {
                        		return;
                        	}
                            StopWatch sw = context.getStopWatch();
                            String messageBody = null;
                            GwRequestMsg gwRequestMsg = context.getGwRequestMsg();
                            HttpServletRequest request = null;

                            // 访问目标方法的参数：
                            Object[] args = pjp.getArgs();
                            for (Object arg : args) {
                                if ((arg instanceof GwRequestMsg) && gwRequestMsg == null) {
                                    gwRequestMsg = (GwRequestMsg) arg;
                                } else if (arg instanceof String) {
                                    messageBody = (String) arg;
                                    if (gwRequestMsg == null)
                                        gwRequestMsg = JSONObject.parseObject(messageBody, GwRequestMsg.class);
                                } else if (arg instanceof HttpServletRequest) {
                                    request = (HttpServletRequest) arg;
                                }
                            }

                            TraceLog traceLog = new TraceLog();
                            traceLog.setMaxValueLength(CspSysConfigCache.getIntValue("cspGateway.txLogFileMaxValueLength", -1));
                            traceLog.setLevel(CspSysConfigCache.getStrValue("cspGateway.txFileLogLevel", "DEBUG"));
                            traceLog.setRequestJsonString_client(messageBody);
                            traceLog.setStartTime(context.getSysRecvTime());
                            traceLog.setLogPath(CspSysConfigCache.getStrValue("cspGateway.txLogFilePath", "./logs"));
                            traceLog.setMaxFileSize(CspSysConfigCache.getStrValue("cspGateway.txLogFileMaxSize", "1gb"));
                            traceLog.setTraceId(gwResponseMsg.getSys_evt_trace_id());
                            traceLog.setUrl(request.getContextPath() + request.getServletPath());
                            traceLog.setPlaintextRequestInfo(gwRequestMsg.getPlaintextRequestInfo());

                            TaskInfo[] taskInfos = sw.getTaskInfo();
                            for (TaskInfo t : taskInfos) {
                                if (CspConstants.CSP_TX_HTTP_WATCH_NAME.equals(t.getTaskName())) {
                                    traceLog.setHttpcost(t.getTimeMillis());
                                    break;
                                }
                            }

                            String txcode = "cspTxFailed";
                            try {
                                txcode = gwRequestMsg.getTxRequestMsg().getMsgHead().getSys_txcode();
                            } catch (Exception e) {
                                // 授权认证失败无法获取到TxRequestMsg
                            }

                            traceLog.setLogName(txcode);
                            traceLog.setErrormessage(gwResponseMsg.getReturn_message());
                            traceLog.setResponseJsonString_client(JSONObject.toJSON(gwResponseMsg).toString());
                            traceLog.setPlaintextResponseInfo(gwResponseMsg.getPlaintextResponseInfo());
                            traceLog.setEndTime(System.currentTimeMillis());

                            String logFileEnable = CspSysConfigCache.getStrValue("cspGateway.txLogFileEnable", "true");
                            if (Boolean.valueOf(logFileEnable)) {
                                traceLogger.dispose(traceLog);
                            }

                            String logDBEnable = CspSysConfigCache.getStrValue("cspGateway.txDBLogEnable", "true");
                            if (Boolean.valueOf(logDBEnable)) {

                                final GwRequestMsg gwRequestMsgTmp = gwRequestMsg;
                                final GwResponseMsg gwResponseMsgTmp = gwResponseMsg;
                                final String txcodeTmp = txcode;
                                MultithreadingLogExecutor.addLogTask(new Runnable() {

                                    @Override
                                    public void run() {
                                        saveDBLog(gwRequestMsgTmp, gwResponseMsgTmp, txcodeTmp, context);
                                        
                                        TxMsgData txMsgData = new TxMsgData();
                                        txMsgData.setAppid(gwRequestMsgTmp.getApp_id());
                                        txMsgData.setTxcode(txcodeTmp);

                                        String errorcode = gwResponseMsgTmp.getReturn_code();
                                        String innererrcode = "";
                                        TxResponseMsg txResponseMsg = gwResponseMsgTmp.getTxResponseMsg();

                                        String branchId = "AnyCSP";
                                        if (txResponseMsg != null) {
                                            innererrcode = txResponseMsg.getMsgHead().getSys_resp_code();
                                            String rsp_branchId = txResponseMsg.getMsgBody().getCom1().getTenantId();
                                            if (!StringUtils.isEmpty(rsp_branchId)) {
                                                branchId = rsp_branchId;
                                            }
                                        }
                                        txMsgData.setBranchid(branchId);
                                        if (ErrorInfo.SUCCESS.getCode().equals(errorcode) &&
                                                ErrorInfo.SUCCESS.getCode().equals(innererrcode)) {
                                            txMsgData.setStatus("00");
                                        } else {
                                            txMsgData.setStatus("01");
                                        }

//                                        MqProvider.sendTxMessage(txMsgData);
                                    }
                                });
                            }

                            // 是否记录交易日志
                            String fileLogSignEnable = CspSysConfigCache.getStrValue("cspGateway.trans.CSMPFileLogEnable", "false");
                            if (Boolean.valueOf(fileLogSignEnable)) {

                                final GwRequestMsg gwRequestMsgTmp = gwRequestMsg;
                                final GwResponseMsg gwResponseMsgTmp = gwResponseMsg;
                                final String txcodeTmp = txcode;
                                MultithreadingLogExecutor.addLogTask(new Runnable() {

                                    @Override
                                    public void run() {
                                        // 添加交易日志
                                        saveTranceLog(gwRequestMsgTmp, gwResponseMsgTmp, txcodeTmp, context);
                                    }
                                });
                            }

                        } catch (Exception e) {
                            // 忽略错误
                            logger.error("TraceLogAspect", e);
                        }
                    } else {
                    	logger.error("TraceLogAspect", throwable);
                    }
                });
            }
        	
        	
            return object;
        } catch (Throwable e) {
            throw e;
        } finally {
        	
            
        }
    }

    private void saveDBLog(GwRequestMsg gwRequestMsg, GwResponseMsg gwResponseMsg, String txcode, SessionContext context) {
        try {
            //指令管理交易码无需记录日志
            if (CspConstants.CSP_HSMCMD_MANAGER_TXCODES.contains(txcode)) {
                return;
            }
            // 是否只记录错误日志流水
            String logDBEnableForError = CspSysConfigCache.getStrValue("cspGateway.txDBLogEnableForError", "false");
            // 记录日志
            CspSrvLogEntity entity = new CspSrvLogEntity();
            entity.setLogid(Utils.GUID());
            entity.setAppid(gwRequestMsg.getApp_id());

            TxResponseMsg txResponseMsg = gwResponseMsg.getTxResponseMsg();
            String errorcode = gwResponseMsg.getReturn_code();
            String errormsg = gwResponseMsg.getReturn_message();
            String innererrcode = "";
            if (txResponseMsg != null) {
                innererrcode = txResponseMsg.getMsgHead().getSys_resp_code();
            }
            String reqmsg = "";
            String rspmsg = "";
            String transret = "00";
            if (!ErrorInfo.SUCCESS.getCode().equals(errorcode) ||
                    !ErrorInfo.SUCCESS.getCode().equals(innererrcode)) {
                transret = "01";
                reqmsg = JsonUtils.formatJson(gwRequestMsg.getPlaintextRequestInfo());
                rspmsg = JsonUtils.formatJson(gwResponseMsg.getPlaintextResponseInfo());
            }
            if (!StringUtils.isEmpty(innererrcode) && !ErrorInfo.SUCCESS.getCode().equals(innererrcode)) {
                transret = "02";
                errormsg = txResponseMsg.getMsgHead().getSys_resp_desc();
            }

            if (Boolean.valueOf(logDBEnableForError) && "00".equals(transret)) {
                return;
            }

            String nodeId = "cspGateway";
            String branchId = "AnyCSP";
            if (txResponseMsg != null) {
                String rsp_nodeId = txResponseMsg.getMsgBody().getCom1().getNodeId();
                if (!StringUtils.isEmpty(rsp_nodeId)) {
                    nodeId = rsp_nodeId;
                }
                String rsp_branchId = txResponseMsg.getMsgBody().getCom1().getTenantId();
                if (!StringUtils.isEmpty(rsp_branchId)) {
                    branchId = rsp_branchId;
                }
            }
            entity.setNodeid(nodeId);
            entity.setBranchid(branchId);
            entity.setTrace_id(gwResponseMsg.getSys_evt_trace_id());

            entity.setRecv_time(DateUtils.toDateInMillis(context.getSysRecvTime()));
            entity.setResp_time(DateUtils.toDateInMillis(context.getSysRespTime()));
            entity.setTxcode(txcode);

            String hostname = LocalMessage.getHostName();
            String hostip = LocalMessage.getLocalIP().get(0);
            //String costtimeinfo = context.getStopWatch().toString();
            StopWatch sw = context.getStopWatch();
            StringBuilder costtimeinfo = new StringBuilder("StopWatch: running time = " + sw.getTotalTimeMillis() + " ms");
            for (TaskInfo task : sw.getTaskInfo()) {
                costtimeinfo.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeMillis()).append(" ms");
                long percent = Math.round(100.0 * task.getTimeMillis() / sw.getTotalTimeMillis());
                costtimeinfo.append(" = ").append(percent).append('%');
            }

            entity.setTransret(transret);
            entity.setHostname(hostname);
            entity.setHostip(hostip);
            entity.setErrorcode(errorcode);
            entity.setInnererrcode(innererrcode);
            entity.setErrormsg(errormsg);
            entity.setReqmsg(reqmsg);
            entity.setRspmsg(rspmsg);
            entity.setCosttimeinfo(costtimeinfo.toString());

            // 是否进行数据库记录签名
            String DBLogTabSignEnable = CspSysConfigCache.getStrValue("sys.DBLogTabSignEnable", "true");
            if (Boolean.valueOf(DBLogTabSignEnable)) {
                //行记录信息签名
                CspInitKeysEntity initKeysEntity = CspInitKeysCache.getInitKey(CspConstants.CSP_TAB_DATA_SIGN_KEYID);
                String cipherPrivateKey = "";
                if (initKeysEntity != null) {
                    cipherPrivateKey = initKeysEntity.getKey_ciphertextvalue();
                }
                String signData = entity.toSignString();

                int keytLength = cipherPrivateKey.length() / 2;
                String signDataHex = Utils.bytes2Hex(signData.getBytes());
                int dataLength = signDataHex.length() / 2;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("methodname", "sm2Sign");
                params.put("keylen", String.valueOf(keytLength));
                params.put("key", cipherPrivateKey);
                params.put("datalen", String.valueOf(dataLength));
                params.put("data", signDataHex);
                params.put("keyalg", "SM2");

                params.put("designID", CspSysConfigCache.getStrValue("sys.default.designID", "csp"));
                params.put("nodeID", CspSysConfigCache.getStrValue("sys.default.nodeID", "csp"));
                params.put("keyModelID", CspSysConfigCache.getStrValue("sys.default.keyModelID", "csp"));

                String signature = new String();
                TxResponseMsg txResMsg = BaseController.accessToServiceComTx(params);
                if (ErrorInfo.SUCCESS.getCode().equals(txResMsg.getMsgHead().getSys_resp_code())) {
                    CSP000000OutVo outVo = (CSP000000OutVo) txResMsg.getMsgBody().getEntity();
                    if (outVo != null && CspConstants.SUCCESS_HSM_CODE.equals(outVo.getHsmrspdataJson().getString(CspConstants.ERRCODE))) {
                        String r = outVo.getHsmrspdataJson().getString("signR");
                        String s = outVo.getHsmrspdataJson().getString("signS");
                        signature = r + s;
                        entity.setSignature(signature);
                    } else {
                        logger.error("CspSrvLogService.save for Signature failed, 加密机处理出错，返回码：" + outVo.getHsmrspdataJson().getString(CspConstants.ERRCODE));
                    }
                } else {
                    logger.error("CspSrvLogService.save for Signature failed, errcode:" + txResMsg.getMsgHead().getSys_resp_code() + "@" + txResMsg.getMsgHead().getSys_resp_desc());
                }

                synchronized (signature) {
                    int ret = srvLogService.save(entity);
                    if (ret > 0) {
                        //纵向签名
                        signData = srvLogService.count().toString();
                        signDataHex = Utils.bytes2Hex(signData.getBytes());
                        params.put("datalen", String.valueOf(signDataHex.length() / 2));
                        params.put("data", signDataHex);
                        txResMsg = BaseController.accessToServiceComTx(params);

                        if (ErrorInfo.SUCCESS.getCode().equals(txResMsg.getMsgHead().getSys_resp_code())) {
                            CSP000000OutVo outVo = (CSP000000OutVo) txResMsg.getMsgBody().getEntity();
                            if (outVo != null && CspConstants.SUCCESS_HSM_CODE.equals(outVo.getHsmrspdataJson().getString(CspConstants.ERRCODE))) {
                                String r = outVo.getHsmrspdataJson().getString("signR");
                                String s = outVo.getHsmrspdataJson().getString("signS");
                                signature = r + s;
                                entity.setSignature(signature);
                            } else {
                                logger.error("CspSrvLogService.count for Signature failed, 加密机处理出错，返回码：" + outVo.getHsmrspdataJson().getString(CspConstants.ERRCODE));
                            }
                        } else {
                            logger.error("CspSrvLogService.count for Signature failed, errcode:" + txResMsg.getMsgHead().getSys_resp_code() + "@" + txResMsg.getMsgHead().getSys_resp_desc());
                        }
                        SysTabVsignEntity tabVsignEntity = new SysTabVsignEntity();
                        tabVsignEntity.setTablename(LOG_TABLE_NAME);
                        tabVsignEntity.setSignature(signature);
                        sysTabVsignService.saveOrUpdate(tabVsignEntity);
                    }
                    logger.debug("----saveDBLog:" + ret);
                }
            } else {
                int ret = srvLogService.save(entity);
                logger.debug("----saveDBLog:" + ret);
            }

        } catch (Exception e) {
            // 忽略错误
            logger.error("saveDBLog", e);
        }
    }

    private void saveTranceLog(GwRequestMsg gwRequestMsg, GwResponseMsg gwResponseMsg, String txcode, SessionContext context) {
        try {
            //指令管理交易码无需记录日志
            if (CspConstants.CSP_HSMCMD_MANAGER_TXCODES.contains(txcode)) {
                return;
            }
            // 记录日志
            CspSrvLogEntity entity = new CspSrvLogEntity();
            entity.setLogid(Utils.GUID());
            entity.setAppid(gwRequestMsg.getApp_id());

            TxResponseMsg txResponseMsg = gwResponseMsg.getTxResponseMsg();
            String errorcode = gwResponseMsg.getReturn_code();
            String errormsg = gwResponseMsg.getReturn_message();
            String innererrcode = "";
            if (txResponseMsg != null) {
                innererrcode = txResponseMsg.getMsgHead().getSys_resp_code();
            }
            String reqmsg = "";
            String rspmsg = "";
            String transret = "00";
            if (!ErrorInfo.SUCCESS.getCode().equals(errorcode) ||
                    !ErrorInfo.SUCCESS.getCode().equals(innererrcode)) {
                transret = "01";
                reqmsg = JsonUtils.formatJson(gwRequestMsg.getPlaintextRequestInfo());
                rspmsg = JsonUtils.formatJson(gwResponseMsg.getPlaintextResponseInfo());
            }
            if (!StringUtils.isEmpty(innererrcode) && !ErrorInfo.SUCCESS.getCode().equals(innererrcode)) {
                transret = "02";
                errormsg = txResponseMsg.getMsgHead().getSys_resp_desc();
            }

            String nodeId = "cspGateway";
            String branchId = "AnyCSP";
            if (txResponseMsg != null) {
                String rsp_nodeId = txResponseMsg.getMsgBody().getCom1().getNodeId();
                if (!StringUtils.isEmpty(rsp_nodeId)) {
                    nodeId = rsp_nodeId;
                }
                String rsp_branchId = txResponseMsg.getMsgBody().getCom1().getTenantId();
                if (!StringUtils.isEmpty(rsp_branchId)) {
                    branchId = rsp_branchId;
                }
            }
            entity.setNodeid(nodeId);
            entity.setBranchid(branchId);
            entity.setTrace_id(gwResponseMsg.getSys_evt_trace_id());

            entity.setRecv_time(DateUtils.toDateInMillis(context.getSysRecvTime()));
            entity.setResp_time(DateUtils.toDateInMillis(context.getSysRespTime()));
            entity.setTxcode(txcode);

            String hostname = LocalMessage.getHostName();
            String hostip = LocalMessage.getLocalIP().get(0);
            //String costtimeinfo = context.getStopWatch().toString();
            StopWatch sw = context.getStopWatch();
            StringBuilder costtimeinfo = new StringBuilder("StopWatch: running time = " + sw.getTotalTimeMillis() + " ms");
            for (TaskInfo task : sw.getTaskInfo()) {
                costtimeinfo.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeMillis()).append(" ms");
                long percent = Math.round(100.0 * task.getTimeMillis() / sw.getTotalTimeMillis());
                costtimeinfo.append(" = ").append(percent).append('%');
            }

            entity.setTransret(transret);
            entity.setHostname(hostname);
            entity.setHostip(hostip);
            entity.setErrorcode(errorcode);
            entity.setInnererrcode(innererrcode);
            entity.setErrormsg(errormsg);
            entity.setReqmsg(reqmsg);
            entity.setRspmsg(rspmsg);
            entity.setCosttimeinfo(costtimeinfo.toString());
            // 往队列中写数据
            TranceLogQueue.getLogQueue().put(entity);
        } catch (Exception e) {
            // 忽略错误
            logger.error("saveTranceLog", e);
        }
    }


}
