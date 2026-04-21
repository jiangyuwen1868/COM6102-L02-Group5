package com.jyw.csp.aop;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.alibaba.fastjson.JSONObject;
import com.anydef.gwapi.sdk.core.constant.HttpConstant;
import com.anydef.gwapi.sdk.core.constant.SdkConstant;
import com.anydef.gwapi.sdk.core.enums.Method;
import com.anydef.gwapi.sdk.core.enums.Scheme;
import com.anydef.gwapi.sdk.core.model.ApiRequest;
import com.anydef.gwapi.sdk.core.util.SignUtil;
import com.jyw.csp.context.SessionContext;
import com.jyw.csp.controllers.BaseController;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;
import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.entity.CspAppIpWhitelistEntity;
import com.jyw.csp.enums.AppAuthType;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.exception.CommonException;
import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.monitor.MonitorFilter;
import com.jyw.csp.service.CspSrvLogService;
import com.jyw.csp.service.cache.CspAppInfoCache;
import com.jyw.csp.service.cache.CspAppIpWhitelistCache;
import com.jyw.csp.service.cache.CspErrorInfoCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.service.log.DBLogUtil;
import com.jyw.csp.service.log.TraceLog;
import com.jyw.csp.service.log.TraceLogger;
import com.jyw.csp.util.Base64;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.log.LogUtil;
import com.jyw.csp.util.log.thread.MultithreadingLogExecutor;
import com.jyw.csp.util.string.StringUtils;

@Component
@Aspect//声明一个切面
@Order(0)
public class TxAuthAspect {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TraceLogger traceLogger;
	
	@Autowired
	private CspSrvLogService srvLogService;

	@Pointcut("@annotation(com.jyw.csp.aop.TxAuthAction)")//声明一个切点
    public  void TxAuthPointCut(){};

    @Around("TxAuthPointCut()")
    public Object doAroundAdvice(ProceedingJoinPoint point) {
    	logger.debug("==========TxAuthAspect->doAround begin");
    	
    	SessionContext context = SessionContext.getCurrentContext();
    	StopWatch sw = context.getStopWatch();
        String name = point.getSignature().getName();//方法名称
        logger.debug("ProceedingJoinPoint Name:" + name);
        String messageBody = null;
        GwRequestMsg gwRequestMsg = null;
        GwResponseMsg gwResponseMsg = null;
        HttpServletRequest request = null;
        try {
            //访问目标方法的参数：
            Object[] args = point.getArgs();
            for (Object arg : args) {
                if (arg instanceof GwRequestMsg) {
                	gwRequestMsg = (GwRequestMsg) arg;
                }
                if(arg instanceof String) {
                	messageBody = (String)arg;
                	gwRequestMsg = JSONObject.parseObject(messageBody, GwRequestMsg.class);
                }
                if(arg instanceof HttpServletRequest) {
                	request = (HttpServletRequest) arg;
                }
            }
            if (gwRequestMsg == null) {
            	gwResponseMsg = new GwResponseMsg();
            	gwResponseMsg.setReturn_code(ErrorInfo.REQUEST_EMPTY.getCode());
            	gwResponseMsg.setReturn_message(ErrorInfo.REQUEST_EMPTY.getInfo());
            	gwResponseMsg.setSys_evt_trace_id(BaseController.genSysEvtTraceId());
            	gwResponseMsg.setSrv_costtime(String.valueOf(System.currentTimeMillis() - context.getSysRecvTime()));
                return gwResponseMsg;
            	
            }
            
            // 授权验证总开关
            String authEnable = CspSysConfigCache.getStrValue("cspGateway.authEnable", "true");
            if(!Boolean.valueOf(authEnable)) {
            	return point.proceed();
            }
            
            String appid = gwRequestMsg.getApp_id();
            CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);
            if(appInfo==null) {
    			throw new CommonRuntimeException("XDBF99129999","AppId(" + appid + ")未经授权");
    		}
            String authtype = appInfo.getAuthtype();
    		if(AppAuthType.AUTH07.getCode().equals(authtype)) {
    			return point.proceed(); //无认证
            }
            
            //IP白名单验证
            sw.start("doAuthCheckIpWhitelist");
            boolean authResult = doAuthCheckIpWhitelist(request, gwRequestMsg);
            sw.stop();
            if(authResult) {
            	// 签名授权验证
            	sw.start("doAuthCheck");
                authResult = doAuthCheck(request, messageBody, gwRequestMsg);
                sw.stop();
            }
            
            
            if(authResult) {
            	return point.proceed();
            }
            
            if (gwResponseMsg == null) {
            	gwResponseMsg = new GwResponseMsg();
            	gwResponseMsg.setReturn_code(ErrorInfo.UNKNOWN.getCode());
            	gwResponseMsg.setReturn_message(ErrorInfo.UNKNOWN.getInfo());
            	gwResponseMsg.setSys_evt_trace_id(BaseController.genSysEvtTraceId());
            	gwResponseMsg.setSrv_costtime(String.valueOf(System.currentTimeMillis() - context.getSysRecvTime()));

                return gwResponseMsg;
            }

        } catch (Throwable e) {
        	gwResponseMsg = new GwResponseMsg();
            if (e instanceof CommonException) {
            	MonitorFilter.addTranctError();
            	gwResponseMsg.setReturn_code(((CommonException) e).getCode());
            	gwResponseMsg.setReturn_message(e.getMessage());
            } else if (e instanceof CommonRuntimeException) {  //验证失败
            	MonitorFilter.addTranctError();
            	gwResponseMsg.setReturn_code(((CommonRuntimeException) e).getCode());
            	gwResponseMsg.setReturn_message(e.getMessage());
            } else {
            	MonitorFilter.addSysError();
            	gwResponseMsg.setReturn_code(ErrorInfo.UNKNOWN.getCode());
            	gwResponseMsg.setReturn_message(ErrorInfo.UNKNOWN.getInfo());
            	
            	logger.debug("An exception ({}) has been throwing in ({})", e, point.getSignature());
            	logger.error("TxAuthAction", e);
            }
        } finally {
       
	        //验证失败
	        gwResponseMsg.setSys_evt_trace_id(BaseController.genSysEvtTraceId());
	        long currentTime = System.currentTimeMillis();
	    	gwResponseMsg.setSrv_costtime(String.valueOf(currentTime - context.getSysRecvTime()));
	    	context.setSysRespTime(currentTime);
	    	
	    	// 转译错误信息
			String errormsg = CspErrorInfoCache.getErrorMsg(gwResponseMsg.getReturn_code(), gwResponseMsg.getReturn_message());
			gwResponseMsg.setReturn_message(errormsg);
        
	        
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
			
			
			String txcode = "cspTxFailed";

			traceLog.setLogName(txcode);
			traceLog.setErrormessage(gwResponseMsg.getReturn_message());
			traceLog.setResponseJsonString_client(JSONObject.toJSON(gwResponseMsg).toString());
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
						DBLogUtil.saveDBLog(srvLogService, gwRequestMsgTmp, gwResponseMsgTmp, txcodeTmp, context);
					}
				});
			}
        
        }
        logger.debug("==========TxAuthAspect->doAround end");
        return gwResponseMsg;
        
    }
    
    /**
     * 请求数据签名、身份认证、防篡改、防重放检查
     * @param request
     * @param message
     * @param gwRequestMsg
     * @return
     * @throws Exception
     */
    private boolean doAuthCheck(HttpServletRequest request, String message, GwRequestMsg gwRequestMsg) throws Exception {
    	
    	String scheme = request.getScheme();
		String method = request.getMethod();
		String host = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_HOST);
		String path = request.getContextPath() + request.getServletPath();
		String requestContentType = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE);
		String acceptContentType = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT);
		//AppId
		String appid = request.getHeader(SdkConstant.CLOUDAPI_X_CA_KEY);
		LogUtil.debug("--------TxAuthCheck-------");
		LogUtil.debug("request scheme:" + scheme);
		LogUtil.debug("request method:" + method);
		LogUtil.debug("request host:" + host);
		LogUtil.debug("request path:" + path);
		LogUtil.debug("request appid:" + appid);
		if(StringUtils.checkEmpty(host)) {
			host = "";
		}
		
		if(!StringUtils.hasLength(appid)) {
			appid = gwRequestMsg.getApp_id();
		}
		
		CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);
		
		if(appInfo==null) {
			throw new CommonRuntimeException("XDBF99129999","AppId(" + appid + ")未经授权");
		}
		
		String authtype = appInfo.getAuthtype();
		if(!(AppAuthType.AUTH02.getCode().equals(authtype) ||
				AppAuthType.AUTH03.getCode().equals(authtype) ||
				AppAuthType.AUTH04.getCode().equals(authtype) ||
				AppAuthType.AUTH06.getCode().equals(authtype))) {
        	return true;
        }
		
		//根据 AppId 查询 AppSecret
		String appSecret = appInfo.getAppsecret();
		String keyStatus = appInfo.getAppstatus();
		if("0".equals(keyStatus)) {
			throw new CommonRuntimeException("XDBF99139999","AppId(" + appid + ")未开通");
		}
		
		LogUtil.debug("appSecret:" + appSecret);
		
		ApiRequest apiReq = new ApiRequest("http".equalsIgnoreCase(scheme)?Scheme.HTTP:Scheme.HTTPS, Method.POST_BODY, host, path, message.getBytes("UTF-8"));
		
		Date current = new Date();
		/*
         * 获取请求头中的时间戳
         */
		String header_date = request.getHeader(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP);
		if(!StringUtils.hasLength(header_date)) {
			throw new CommonRuntimeException("XTLF99020099","请求header中的Date数据为空");
		}
		long header_date_ms = Long.parseLong(header_date);
		long timediff = current.getTime() - header_date_ms;
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyyMMddHHmmss", Locale.CHINA);
		LogUtil.debug("System currentTime:" + formatter.format(current) + ",timediff:" + timediff);
		if(timediff>=0) {
			if(timediff>CspSysConfigCache.getIntValue("cspGateway.authValidAfterTime", 60 * 3) * 1000) {
				throw new CommonRuntimeException("XTLF99025096", "时间戳验证失败，交易拒绝。");
			}
		} else {
			if(Math.abs(timediff)>CspSysConfigCache.getIntValue("cspGateway.authValidBeforTime", 60) * 1000) {
				throw new CommonRuntimeException("XTLF99025095", "时间戳无效，交易拒绝。");
			}
		}
		/*
         * 获取请求头中的时间戳
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE, request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE));

        /*
         * 获取请求头中的时间戳，以timeIntervalSince1970的形式
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP, request.getHeader(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP));

        /*
         * 请求放重放Nonce,15分钟内保持唯一,建议使用UUID
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_NONCE, request.getHeader(SdkConstant.CLOUDAPI_X_CA_NONCE));

        /*
         * 获取请求头中的UserAgent
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_USER_AGENT, SdkConstant.CLOUDAPI_USER_AGENT);

        /*
         * 获取请求头中的主机地址
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_HOST, host);

        /*
         * 获取请求头中的Api绑定的的AppKey
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_KEY, request.getHeader(SdkConstant.CLOUDAPI_X_CA_KEY));

        /*
         * 获取签名版本号
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_VERSION, SdkConstant.CLOUDAPI_CA_VERSION_VALUE);

        /*
         * 获取请求数据类型
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE,
        		requestContentType);

        /*
         * 获取应答数据类型
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT, acceptContentType);

        String content_md5S = "";
        if (/*MapUtils.isEmpty(apiReq.getFormParams()) &&*/ ArrayUtils.isNotEmpty(apiReq.getBody())) {
            /*
             *  如果类型为byte数组的body不为空
             *  将body中的内容MD5算法加密后再采用BASE64方法Encode成字符串，放入HTTP头中
             *  做内容校验，避免内容在网络中被篡改
             */
            content_md5S = getMD5WithBase64Encode(apiReq.getBody());
        }
        
        else if (( MapUtils.isNotEmpty(apiReq.getFormParams()) 
        		|| MapUtils.isNotEmpty(apiReq.getQuerys()) ) 
        		&& ArrayUtils.isEmpty(apiReq.getBody())) {
            /*
             *  如果类型为byte数组的body为空，请求queryParam、formParam数据不为空
             *  将queryParam、formParam数据中的内容MD5算法加密后再采用BASE64方法Encode成字符串，放入HTTP头中
             *  做内容校验，避免内容在网络中被篡改
             */
        	String querys = SignUtil.buildResource(apiReq.getQuerys(), apiReq.getFormParams());
    		LogUtil.debug("MD5 querys:" + querys);
            content_md5S = getMD5WithBase64Encode(querys.getBytes(SdkConstant.CLOUDAPI_ENCODING));
        }
        
        //验证请求报文数据是否被篡改
        String content_md5C = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5);
        /**
         * 设置header content MD5
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5, content_md5S);
        LogUtil.debug("content_md5C:" + content_md5C);
        LogUtil.debug("content_md5S:" + content_md5S);
        if(!StringUtils.hasLength(content_md5C)) {
			throw new CommonRuntimeException("XTLF99025099","请求header数据内容摘要值为空");
		}
        if(!content_md5C.equals(content_md5S)) {
        	throw new CommonRuntimeException("XTLF99026099","请求数据被篡改，交易拒绝");
        }

        String pathWithPathParameter = combinePathParam(apiReq.getPath(), request);
        /*
         *  将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串用hmacSha256算法双向加密进行签名
         *  签名内容放到Http头中，用作服务器校验
         */
        String signatureS = SignUtil
                .sign(apiReq.getMethod().getName(), appSecret, apiReq.getHeaders(), pathWithPathParameter,
                        apiReq.getQuerys(), apiReq.getFormParams());

        for (Map.Entry<String, String> entry : apiReq.getHeaders().entrySet()) {

            // 因http协议头使用ISO-8859-1字符集，不支持中文，所以需要将header中的中文通过UTF-8.encode()，再使用ISO-8859-1.decode()后传输对应的，
            // 服务器端需要将所有header使用ISO-8859-1.encode()，再使用UTF-8.decode()，以还原中文
            if (StringUtils.isNotEmpty(entry.getValue())) {
                entry.setValue(new String(entry.getValue().getBytes(SdkConstant.CLOUDAPI_ENCODING),
                    SdkConstant.CLOUDAPI_HEADER_ENCODING));
            }
        }
        
        LogUtil.debug("ApiRequest Info:" + apiReq.toString());
        
        //验证签名是否正确
        String signatureC = request.getHeader(SdkConstant.CLOUDAPI_X_CA_SIGNATURE);
        LogUtil.debug("signatureC:" + signatureC);
        LogUtil.debug("signatureS:" + signatureS);
        if(!StringUtils.hasLength(signatureC)) {
			throw new CommonRuntimeException("XTLF99023099","请求header签名数据为空");
		}
        if(!signatureC.equals(signatureS)) {
        	throw new CommonRuntimeException("XTLF99024099","请求header签名数据验证失败");
        }
        
        //验证请求是否为重放攻击
		return true;
    }
    
    /**
     * 交易请求应用白名单检查
     * @param request
     * @param gwRequestMsg
     * @return
     * @throws Exception
     */
    private boolean doAuthCheckIpWhitelist(HttpServletRequest request, GwRequestMsg gwRequestMsg) throws Exception {
    	
    	String appid = gwRequestMsg.getApp_id();
    	
    	CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(gwRequestMsg.getApp_id());
    	if(appInfo==null) {
			throw new CommonRuntimeException("XDBF99129999","AppId(" + appid + ")未经授权");
		}
    	
        String authtype = appInfo.getAuthtype();
		if(!(AppAuthType.AUTH01.getCode().equals(authtype) ||
				AppAuthType.AUTH02.getCode().equals(authtype) ||
				AppAuthType.AUTH03.getCode().equals(authtype))) {
			return true;
		}
    	
    	List<CspAppIpWhitelistEntity> list = CspAppIpWhitelistCache.getAppIpWhitelist(appid);
    	if(list==null) {
    		return true;
    	}
    	String clientIp = Utils.getClientIpAddr(request);
    	logger.debug("----doAuthCheckIpWhitelist getClientIp:" + clientIp);
    	if(StringUtils.checkEmpty(clientIp)) {
    		return true;
    	}
    	boolean isMatch = false;
    	for(int i=0;i<list.size();i++) {
    		CspAppIpWhitelistEntity entity = list.get(i);
    		isMatch = StringUtils.isMatch(entity.getIp(), clientIp);
    		if(isMatch) {
    			break;
    		}
    	}
    	
    	if(!isMatch) {
    		throw new CommonRuntimeException("XTIP10010001","IP地址(" + clientIp + ")未授权");
    	}
    	
    	return true;
    }
    
    
    /**
     * 先进行MD5再进行Base64编码获取摘要字符串
     */
    private static String getMD5WithBase64Encode(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can not be null");
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(bytes);
            byte[] md5Result = md.digest();
            String base64Result = Base64.encodeString(md5Result);
            /*
             * 正常情况下，base64的结果为24位，因与客户端有约定，在超过24位的情况下，截取前24位
             */
            return base64Result.length() > 24 ? base64Result.substring(0, 23) : base64Result;
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown algorithm MD5");
        }
    }
    
    private static String combinePathParam(String path, HttpServletRequest request) {
        Map<?,?> pathParams = request.getParameterMap();
    	if (pathParams == null) {
            return path;
        }

        for (Object obj : pathParams.keySet()) {
        	if(obj instanceof String) {
        		String key = (String) obj;
        		path = path.replace("[" + key + "]", (String)pathParams.get(obj));
        	}
            
        }
        return path;
    }
}
