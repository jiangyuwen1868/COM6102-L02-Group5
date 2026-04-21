package com.jyw.csp.controllers;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.anydef.gwapi.sdk.core.constant.HttpConstant;
import com.anydef.gwapi.sdk.core.constant.SdkConstant;
import com.anydef.gwapi.sdk.core.enums.Method;
import com.anydef.gwapi.sdk.core.enums.Scheme;
import com.anydef.gwapi.sdk.core.model.ApiRequest;
import com.anydef.gwapi.sdk.core.util.SignUtil;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.SessionContext;
import com.jyw.csp.datatransform.message.gw.GwRequestMsg;
import com.jyw.csp.datatransform.message.tx.TxRequestMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsg;
import com.jyw.csp.datatransform.message.tx.TxResponseMsgHead;
import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.entity.CspAppIpWhitelistEntity;
import com.jyw.csp.entity.CspAppSrvAuthEntity;
import com.jyw.csp.entity.CspHsmGroupEntity;
import com.jyw.csp.entity.CspTxCodeEntity;
import com.jyw.csp.enums.AppAuthType;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.enums.HsmCategory;
import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.mq.msg.AlarmLevel;
import com.jyw.csp.mq.msg.AlarmMsgData;
import com.jyw.csp.mq.msg.AlarmType;
import com.jyw.csp.resource.httpresource.HttpCommPoolService;
import com.jyw.csp.service.CspHsmGroupService;
import com.jyw.csp.service.cache.CspAppInfoCache;
import com.jyw.csp.service.cache.CspAppIpWhitelistCache;
import com.jyw.csp.service.cache.CspAppSrvAuthCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.service.cache.CspTxCodeCache;
import com.jyw.csp.util.Base64;
import com.jyw.csp.util.CacheMap;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.date.DateUtils;
import com.jyw.csp.util.string.StringUtils;
import com.jyw.csp.vo.CSP000000OutVo;

public class BaseController {
	private final static Logger logger = LoggerFactory.getLogger(BaseController.class);
	private static String SECURE_NODE_CODE = "232323";
	private static String HOSTSEQNO_TXMONITOR = "999";
	
	private static int intTraceId = 0;
	
	@Resource
	private CspHsmGroupService hsmGrpupService;
    
//    @Resource
//	private RedisService redisService;
	@Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public static boolean RedisActive = true;//redis是否可用标识
    
 	// 缓存请求唯一流水，用于鉴别重放攻击，时效性前后三分钟
 	private CacheMap<Object, Object> cacheMap = CacheMap.getDefault();
 	
 	public final String CLIENT_PUBLICKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlb76FrYn4PIQUs2XiNF+CvPMP1Oj8ZhDDHjFo69fP3/JMUmWT5Kg0DXdYE5deS6JNrN+bK8p8v0te1biW6SJJBZN9lC+AQ3Ajm3b9gwuyGhAn1bnj1HYU+by7pstoYKFA9jHi63If+o/T2jpPccHpdD47775kE6XwvkGsrMFLQHW/y6Rs4OGrrOvzsQqaG+I6xpjhUdYx9rOzrK9d6rpUR+lOjyx8ZnQ77TYmj57pUwws8uUTrQhRo1Hv/AdKEXK9eQX6FbHy4wj2T0uj2971Cjl6T9sQjZFv29tiq1YmB+o/8yoKHdSBDKgWEm1i9BdUmd8VwLMbHe8daEw8TA26QIDAQAB";

	/**
	 * 生成唯一流水编号
	 * @return
	 */
	public static synchronized String genSysEvtTraceId(){
		String strSeconds = String.valueOf(System.currentTimeMillis() / 1000L);
		int iLength = strSeconds.length();
		if (iLength > 10)
		  strSeconds = strSeconds.substring(iLength - 10, iLength);
		else {
		  for (int i = 0; i < 10 - iLength; i++) {
		    strSeconds = "0" + strSeconds;
		  }
		}
		if(intTraceId == 999999){
			intTraceId = 1;
		}else{
			intTraceId++;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(SECURE_NODE_CODE).append(HOSTSEQNO_TXMONITOR).append(strSeconds);
		String strEvtTraceId = String.valueOf(intTraceId);
		iLength = strEvtTraceId.length();
		for (int i = 0; i < 6 - iLength; i++) {
			sb.append("0");
		}
		sb.append(strEvtTraceId);
		return sb.toString();
	}
	
	/**
	 * 请求报文合法校验
	 * @param requestMsg
	 * @throws CommonRuntimeException
	 */
	protected void checkRequestMsg(GwRequestMsg requestMsg) throws CommonRuntimeException {
		String appid = requestMsg.getApp_id();
		String signature = requestMsg.getSignature();
		String sec_version = requestMsg.getSec_version();
		String request_info = requestMsg.getRequest_info();
		if(StringUtils.checkEmpty(appid)) {
			throw new CommonRuntimeException("XDCTS1001001", "接口报文app_id不能为空");
		}
		if(StringUtils.checkEmpty(request_info)) {
			throw new CommonRuntimeException("XDCTS1001002", "接口报文request_info不能为空");
		}
		
		CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);
		if (appInfo == null) {
			throw new CommonRuntimeException("XDBF99129999", "AppId(" + appid + ")未经授权");
		}
		String authtype = appInfo.getAuthtype();
		if(AppAuthType.AUTH02.getCode().equals(authtype) ||
				AppAuthType.AUTH03.getCode().equals(authtype) ||
				AppAuthType.AUTH06.getCode().equals(authtype)) {
			if(StringUtils.checkEmpty(signature)) {
				throw new CommonRuntimeException("XDCTS1001003", "接口报文signature不能为空");
			}
		}
		
		if(StringUtils.checkEmpty(sec_version)) {
			throw new CommonRuntimeException("XDCTS1001004", "接口报文sec_version不能为空");
		}
		
		if(!(CspConstants.SEC_VERSION_01.equals(sec_version) 
				|| CspConstants.SEC_VERSION_02.equals(sec_version))) {
			throw new CommonRuntimeException("XDCTS1001005", "接口报文sec_version不合法");
		}
	}
	
	
	  /**
     * 请求数据签名、身份认证、防篡改、防重放检查
     * @param message
     * @param gwRequestMsg
     * @return
     * @throws Exception
     */
	protected boolean doAuthCheck(String message, GwRequestMsg gwRequestMsg, HttpServletRequest request) throws Exception {
    	
    	String scheme = request.getScheme();
		String method = request.getMethod();
		String host = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_HOST);
		String path = request.getContextPath() + request.getServletPath();
		String requestContentType = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE);
		String acceptContentType = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT);
		//AppId
		String appid = request.getHeader(SdkConstant.CLOUDAPI_X_CA_KEY);
		
		System.out.println("--------TxAuthCheck headers-------");
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
		    String headerName = headerNames.nextElement();
		    String headerValue = request.getHeader(headerName);
		    System.out.println("请求头：" + headerName + "，值：" + headerValue);
		}

		logger.debug("--------TxAuthCheck-------");
		logger.debug("request scheme:" + scheme);
		logger.debug("request method:" + method);
		logger.debug("request host:" + host);
		logger.debug("request path:" + path);
		logger.debug("request appid:" + appid);
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
		String appStatus = appInfo.getAppstatus();
		if(!CspConstants.APP_STATUS_2.equals(appStatus)) {
			throw new CommonRuntimeException("XDBF99139999","AppId(" + appid + ")未开通");
		}
		
		logger.debug("appSecret:" + appSecret);
		
		ApiRequest apiReq = new ApiRequest("http".equalsIgnoreCase(scheme)?Scheme.HTTP:Scheme.HTTPS, Method.POST_BODY, host, path, message.getBytes("UTF-8"));
		
		Date current = new Date();
		/*
         * 获取请求头中的时间戳
         */
        String header_date = request.getHeader(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP);
        logger.debug("request timestamp:" + header_date);
        if (!StringUtils.hasLength(header_date)) {
            throw new CommonRuntimeException("XTLF99020099", "请求header中的Date数据为空");
        }
        long header_date_ms = Long.parseLong(header_date);
        long timediff = current.getTime() - header_date_ms;
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyyMMddHHmmss", Locale.CHINA);
        logger.debug("System currentTime:" + formatter.format(current) + ",timediff:" + timediff);

        String enableTime = CspSysConfigCache.getStrValue("cspGateway.authEnableTime", "true");
        if (Boolean.valueOf(enableTime)) {
            if (timediff >= 0) {
                if (timediff > CspSysConfigCache.getIntValue("cspGateway.authValidAfterTime", 60 * 3) * 1000) {
                    throw new CommonRuntimeException("XTLF99025096", "时间戳验证失败，交易拒绝。");
                }
            } else {
                if (Math.abs(timediff) > CspSysConfigCache.getIntValue("cspGateway.authValidBeforTime", 60) * 1000) {
                    throw new CommonRuntimeException("XTLF99025095", "时间戳无效，交易拒绝。");
                }
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
        String nonce = request.getHeader(SdkConstant.CLOUDAPI_X_CA_NONCE);
        logger.debug("request nonce:" + nonce);
        if(!StringUtils.hasLength(nonce)) {
        	throw new CommonRuntimeException("XTLF89024099","请求header一次性随机数为空");
        }
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_NONCE, nonce);

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
    		logger.debug("MD5 querys:" + querys);
            content_md5S = getMD5WithBase64Encode(querys.getBytes(SdkConstant.CLOUDAPI_ENCODING));
        }
        
        //验证请求报文数据是否被篡改
        String content_md5C = request.getHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5);
        /**
         * 设置header content MD5
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5, content_md5S);
        logger.debug("content_md5C:" + content_md5C);
        logger.debug("content_md5S:" + content_md5S);
        if(!StringUtils.hasLength(content_md5C)) {
			throw new CommonRuntimeException("XTLF99022099","请求header数据内容摘要值为空");
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
        
        logger.debug("ApiRequest Info:" + apiReq.toString());
        
        //验证签名是否正确
        String signatureC = request.getHeader(SdkConstant.CLOUDAPI_X_CA_SIGNATURE);
        logger.debug("signatureC:" + signatureC);
        logger.debug("signatureS:" + signatureS);
        if(!StringUtils.hasLength(signatureC)) {
			throw new CommonRuntimeException("XTLF99023099","请求header签名数据为空");
		}
        if(!signatureC.equals(signatureS)) {
        	throw new CommonRuntimeException("XTLF99024099","请求header签名数据验证失败");
        }
        
        //验证请求是否为重放攻击
        String redisNonce = (String) redisTemplate.opsForValue().get(nonce);
        logger.debug("redisNonce:" + redisNonce);
        if(RedisActive) {
        	if(!StringUtils.checkEmpty(redisNonce)) {
        		throw new CommonRuntimeException("XTLF99025099","请求数据无效，重复使用");
        	}
        	redisTemplate.opsForValue().set(nonce, nonce, Duration.ofHours(24));
        } else {
        	redisNonce = (String)cacheMap.get(nonce);
        	if(!StringUtils.checkEmpty(redisNonce)) {
        		throw new CommonRuntimeException("XTLF99027099","请求数据无效，重复使用");
        	}
        	cacheMap.put(nonce, nonce);
        	
        	//redis不可用
        	AlarmMsgData alarmMsgData = new AlarmMsgData();
			alarmMsgData.setAlarmContent("Redis服务不可用");
			alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
			alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
			alarmMsgData.setAlarmType(AlarmType.REDIS.getType());
			alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
			alarmMsgData.setAlarmHostDesc("cpsGateway");
			alarmMsgData.setAlarmDesc("获取Redis连接失败");
//			MqProvider.sendAlarmMessage(alarmMsgData);
        }
        
		return true;
    }
    
    /**
     * 交易请求应用白名单检查
     * @param gwRequestMsg
     * @return
     * @throws Exception
     */
	protected boolean doAuthCheckIpWhitelist(GwRequestMsg gwRequestMsg, HttpServletRequest request) throws Exception {
    	
    	String appid = gwRequestMsg.getApp_id();
    	
    	CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(gwRequestMsg.getApp_id());
    	if(appInfo==null) {
			throw new CommonRuntimeException("XDBF99129999","AppId(" + appid + ")未经授权");
		}
    	
        String authtype = appInfo.getAuthtype();
		if(!(AppAuthType.AUTH01.getCode().equals(authtype) ||
				AppAuthType.AUTH02.getCode().equals(authtype) ||
				AppAuthType.AUTH04.getCode().equals(authtype))) {
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
	 * 应用服务授权清单检查
	 * @param appid
	 * @param txcode
	 * @return
	 */
	protected boolean doCheckSrvTxCodeAuth(String appid, String txcode) {
		//指令管理交易码无需授权
		if(CspConstants.CSP_HSMCMD_MANAGER_TXCODES.contains(txcode)) {
			return true;
		}
		List<CspAppSrvAuthEntity> list = CspAppSrvAuthCache.getAppSrvAuthListOfTime(appid, 5 * 60 * 1000);
		if(list==null || list.isEmpty()) {
			throw new CommonRuntimeException("XSRV10010001","密码服务(" + txcode + ")未授权");
		} else {
			boolean isAuth = false;
			for(CspAppSrvAuthEntity entity: list) {
				if(entity.getTxcode().equals(txcode)) {
					isAuth = true;
					break;
				}
			}
			if(!isAuth) {
				throw new CommonRuntimeException("XSRV10010002","密码服务(" + txcode + ")未授权");
			}
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
	

	/**
	 * 请求转发至服务层
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	protected String accessToService(String appid, String requestMsg, TxRequestMsg txRequestMsg) throws Exception {
		StopWatch sw = SessionContext.getCurrentContext().getStopWatch();
		sw.start(CspConstants.CSP_TX_HTTP_WATCH_NAME);
		try {
			// 获取请求数据txcode值
//			JSONObject json = JSONObject.parseObject(requestMsg);
//			String txcode = json.getString(CspConstants.CSP_TXCODE_NODE_NAME);
            String txcode = txRequestMsg.getMsgHead().getSys_txcode();
            
            CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);

            CspTxCodeEntity txCodeEntity = CspTxCodeCache.getTxCode(txcode);

            if (txCodeEntity == null) {
                throw new CommonRuntimeException("XDBTS1001102", "交易服务(" + txcode + ")未配置");
            }
            
            String groupId = txCodeEntity.getGroupid();
            if(StringUtils.isNotEmpty(appInfo.getSrvgroups())) {
            	//优先使用应用关联的服务分组
            	String[] srvGroupIds = appInfo.getSrvgroups().split(",");
                SecureRandom random = new SecureRandom();
                groupId = srvGroupIds[random.nextInt(srvGroupIds.length)];
            }
            
            logger.debug("accessToService get groupId:" + groupId);

            String responseMsg = HttpCommPoolService.HTTPCommPool.sendAndWait(groupId, requestMsg);

            if (responseMsg == null) {
                AlarmMsgData alarmMsgData = new AlarmMsgData();
                alarmMsgData.setAlarmContent("密码服务网络不可达，资源组：" + groupId);
                alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
                alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
                alarmMsgData.setAlarmType(AlarmType.NET.getType());
                alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
                alarmMsgData.setAlarmHostDesc("cpsGateway");
                alarmMsgData.setAlarmDesc("请求响应数据为空");
//                MqProvider.sendAlarmMessage(alarmMsgData);

                throw new CommonRuntimeException("XDCTS1001000", "交易服务(" + txcode + ")返回数据为空");
            }
            return responseMsg;
        } finally {
            sw.stop();
        }
    }

    /**
     * 请求转发至服务层通用交易接口
     *
     * @param params
     * @return
     */
    public static TxResponseMsg accessToServiceComTx(Map<String, Object> params) {
        String txcode = "CSP000000";
        TxResponseMsg txResponseMsg = new TxResponseMsg();
        TxResponseMsgHead msgHead = new TxResponseMsgHead();
        try {

            CspTxCodeEntity txCodeEntity = CspTxCodeCache.getTxCode(txcode);

            if (txCodeEntity == null) {
                throw new CommonRuntimeException("XDBTS1001102", "交易服务(" + txcode + ")未配置");
            }

            JSONObject txJson = new JSONObject(true);
            Map<String, Object> txHeaderMap = new HashMap<>();
            txHeaderMap.put(CspConstants.SYS_PKG_VERSION, CspConstants.SYS_PKG_VERSION_01);
            txHeaderMap.put(CspConstants.SYS_REQ_TIME, new DateUtils().getYYYYMMDDhhmmssmis());
            txHeaderMap.put(CspConstants.SYS_EVT_TRACE_ID, Utils.GUID());
            txHeaderMap.put(CspConstants.SYS_TX_CODE, txcode);
            txHeaderMap.put(CspConstants.SYS_PKG_STS_TYPE, CspConstants.SYS_PKG_STS_TYPE_00);
            txJson.put(CspConstants.TX_HEADER, txHeaderMap);

            JSONObject txBodyJson = new JSONObject(true);
            //加密机指令总处理交易
            JSONObject mParams = new JSONObject(true);
            mParams.put("designID", CspSysConfigCache.getStrValue("sys.default.designID", "csp"));
            mParams.put("nodeID", CspSysConfigCache.getStrValue("sys.default.nodeID", "csp"));
            mParams.put("keyModelID", CspSysConfigCache.getStrValue("sys.default.keyModelID", "csp"));

            mParams.put(CspConstants.ENTITY_PARAMS, params);
            txBodyJson.put(CspConstants.ENTITY, mParams);

            JSONObject txBodyCom1 = new JSONObject(true);
            txBodyCom1.put(CspConstants.APPID, CspSysConfigCache.getStrValue("sys.default.appID", "csp"));
            txBodyCom1.put(CspConstants.CHANNELTXCODE, CspConstants.CHANNELTXCODE_DEFUALT);
            txBodyCom1.put(CspConstants.TENANTID, CspSysConfigCache.getStrValue("sys.default.tenantID", "csp"));


            txBodyJson.put(CspConstants.COM1, txBodyCom1);
            txJson.put(CspConstants.TX_BODY, txBodyJson);

            String txMsg = JSON.toJSONString(txJson, SerializerFeature.WriteMapNullValue);
            logger.debug("-----accessToServiceComTx txMsg-----\n" + txMsg);

            String responseMsg = HttpCommPoolService.HTTPCommPool.sendAndWait(txCodeEntity.getGroupid(), txMsg);

            if (responseMsg == null) {
                AlarmMsgData alarmMsgData = new AlarmMsgData();
                alarmMsgData.setAlarmContent("密码服务网络不可达，资源组：" + txCodeEntity.getGroupid());
                alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
                alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
                alarmMsgData.setAlarmType(AlarmType.NET.getType());
                alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
                alarmMsgData.setAlarmHostDesc("cpsGateway");
                alarmMsgData.setAlarmDesc("请求响应数据为空");
//                MqProvider.sendAlarmMessage(alarmMsgData);

                msgHead.setSys_resp_code("XDCTS1001000");
                msgHead.setSys_resp_desc("交易服务(" + txcode + ")返回数据为空");
                msgHead.setSys_txcode(txcode);
                txResponseMsg.setMsgHead(msgHead);
            } else {
                txResponseMsg = JSONObject.parseObject(responseMsg, TxResponseMsg.class);
                if (ErrorInfo.SUCCESS.getCode().equals(txResponseMsg.getMsgHead().getSys_resp_code())) {
                    JSONObject jsonObj = JSON.parseObject(responseMsg);
                    JSONObject rspHsmDataJson = jsonObj.getJSONObject(CspConstants.TX_BODY).getJSONObject(CspConstants.ENTITY).getJSONObject(CspConstants.HSMRSPDATA);

                    CSP000000OutVo outVo = new CSP000000OutVo();
                    outVo.setHsmrspdataJson(rspHsmDataJson);
                    outVo.setHsmrspdata(rspHsmDataJson.toJSONString());
                    txResponseMsg.getMsgBody().setEntity(outVo);
                }
            }
        } catch (Exception e) {
            msgHead.setSys_resp_code(ErrorInfo.UNKNOWN.getCode());
            msgHead.setSys_resp_desc(ErrorInfo.UNKNOWN.getInfo());
            msgHead.setSys_txcode(txcode);
            txResponseMsg.setMsgHead(msgHead);

            logger.error("accessToServiceComTx", e);
        }

        return txResponseMsg;
	}
	
	/**
	 * 请求转发至时间戳服务器
	 * @param txRequestMsg
	 * @return
	 * @throws Exception
	 */
	protected String accessToTSA(String appid, String requestMsg, TxRequestMsg txRequestMsg) throws Exception {
		StopWatch sw = SessionContext.getCurrentContext().getStopWatch();
		sw.start(CspConstants.CSP_TX_HTTP_WATCH_NAME);
		try {
			// 获取请求数据txcode值
			JSONObject json = JSONObject.parseObject(requestMsg);
//			String txcode = json.getString(CspConstants.CSP_TXCODE_NODE_NAME);
			String txcode = txRequestMsg.getMsgHead().getSys_txcode();
			
			CspTxCodeEntity txCodeEntity = CspTxCodeCache.getTxCode(txcode);
			
			if (txCodeEntity == null) {
				throw new CommonRuntimeException("XDBTS1001102", "交易服务(" + txcode +")未配置");
			}
			
			CspAppInfoEntity appInfo = CspAppInfoCache.getAppInfo(appid);
			String groupId = String.valueOf(getTsHsmGroupId(appInfo));
			JSONObject entityJson = json.getJSONObject(CspConstants.TX_BODY).getJSONObject(CspConstants.ENTITY);
			String sendData = entityJson.toJSONString();
			// 签名验签服务特殊处理
			if(txcode.startsWith(CspConstants.CSP_SVS_TXCODES_PREFIX)) {
				StringBuilder requestParam = new StringBuilder();
				Iterator<String> it = entityJson.keySet().iterator();
				while(it.hasNext()) {
					try {
	        		String key = it.next();
	        		String value = String.valueOf(entityJson.get(key));
	        		requestParam.append(key).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
					} catch (Exception e) {
					}
				}
				sendData = requestParam.toString();
			}
			
			logger.debug("accessToTSA--> groupId:{} sendData:{}", groupId, sendData);
			String responseMsg = HttpCommPoolService.HTTPCommPool.sendAndWait(groupId, txCodeEntity.getPtxcode(), sendData);
			
			if (responseMsg == null) {
				AlarmMsgData alarmMsgData = new AlarmMsgData();
				alarmMsgData.setAlarmContent("密码服务网络不可达，资源组：" + groupId);
				alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
				alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
				alarmMsgData.setAlarmType(AlarmType.NET.getType());
				alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
				alarmMsgData.setAlarmHostDesc("cpsGateway");
				alarmMsgData.setAlarmDesc("请求响应数据为空");
//				MqProvider.sendAlarmMessage(alarmMsgData);
				
				throw new CommonRuntimeException("XDCTS1001000", "交易服务(" + txcode +")返回数据为空");
			}
			return responseMsg;
		} finally {
			sw.stop();
		}
	}
	
	// 查询时间戳服务器资源组
	protected int getTsHsmGroupId(CspAppInfoEntity appInfo) {
        try {
        	String hsmGroups = appInfo.getHsmgroups();
            String[] hsmGroupIds = hsmGroups.split(",");
            List<String> tsHsmGroupIds = new ArrayList<String>();
            for(String hsmGroupId : hsmGroupIds) {
            	CspHsmGroupEntity hsmGroupEntity = hsmGrpupService.getById(Long.parseLong(hsmGroupId));
            	if(HsmCategory.CATEGORY04.getCode().equals(hsmGroupEntity.getHsmGroupCategory())
            			|| HsmCategory.CATEGORY03.getCode().equals(hsmGroupEntity.getHsmGroupCategory())) {
            		tsHsmGroupIds.add(hsmGroupId);
            	}
            }
            hsmGroupIds = tsHsmGroupIds.toArray(hsmGroupIds);
            Random random = new Random();
            return Integer.parseInt(hsmGroupIds[random.nextInt(hsmGroupIds.length)]);
        } catch (NumberFormatException e) {
            throw new CommonRuntimeException("XDBTSHSM1102", "应用绑定密码机分组非法");
        }
    }
	
	/**
	 * 判断请求数据是否为客户端API心跳数据
	 * @param message
	 * @return
	 */
	public boolean isClientHeartCheckMsg(String message) {
		if(StringUtils.isEmpty(message)) {
			return true;
		}
		for(String checkstr : CspConstants.CLIENTAPI_HEARTCHECK_FLAGS) {
			if(message.toLowerCase().contains(checkstr.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
