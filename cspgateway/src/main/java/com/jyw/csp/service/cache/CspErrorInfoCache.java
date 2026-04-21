package com.jyw.csp.service.cache;

import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.ApplicationContext;
import com.jyw.csp.entity.CspErrorInfoEntity;
import com.jyw.csp.service.CspErrorInfoService;
import com.jyw.csp.util.string.StringUtils;

@Component
public class CspErrorInfoCache {

	private final static Logger logger = LoggerFactory.getLogger(CspErrorInfoCache.class);
	
	public static CspErrorInfoCache errorInfoCache;
	
	@Autowired
	private CspErrorInfoService errorInfoService;
	
	@PostConstruct
	public void onInit() {
		errorInfoCache = this;
		errorInfoCache.errorInfoService = this.errorInfoService;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspErrorInfoEntity> getErrorInfo() {
		return (Hashtable<String, CspErrorInfoEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_ERRORINFO_CACHE_KEY);
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspErrorInfoEntity> list = errorInfoCache.errorInfoService.getAll();
			logger.debug("----errorinfo list----\n" 
					+ JSON.toJSONString(list));
			if (list != null && !list.isEmpty()) {
				Hashtable<String, CspErrorInfoEntity> ht = new Hashtable<>();
				
				for (int i = 0; i < list.size(); i++) {
					CspErrorInfoEntity errorInfo = list.get(i);
					ht.put(errorInfo.getErrorcode(), errorInfo);
				}
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_ERRORINFO_CACHE_KEY, ht);
			}
		} catch(Exception e) {
			logger.error("refresh errorinfo cache", e);
		}
	}
	
	/**
	 * 获取错误信息
	 * @param errorcode
	 * @return
	 */
	public static String getErrorMsg(String errorcode) {
        return getErrorMsg(errorcode, "数据库未定义错误");
    }
	
	public static String getErrorMsg(String errorcode, String errorMsg) {
		CspErrorInfoEntity errorInfo = getErrorInfo().get(errorcode);
        if (errorInfo == null) {
            return errorMsg;
        }
        
        String errormessage = errorInfo.getErrormsg();
        
        if (errormessage == null || errormessage.equals("")) {
            logger.info("ErrorCode: " + errorcode + "msg is undefined!");
            return errorMsg;
        }
        
        if(CspConstants.ERROR_CODE_ISCONV_Y.equals(errorInfo.getIsconv())) {
        	String convertmsg = errorInfo.getConvertmsg();
        	if(StringUtils.isEmpty(errormessage)) {
        		return errormessage;
        	}
        	return convertmsg;
        }
        
        return errorMsg;
    }
}
