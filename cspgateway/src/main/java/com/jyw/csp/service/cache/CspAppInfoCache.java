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
import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.service.CspAppInfoService;

@Component
public class CspAppInfoCache {

	private final static Logger logger = LoggerFactory.getLogger(CspAppInfoCache.class);
	
	public static CspAppInfoCache appInfoCache;
	
	@Autowired
	private CspAppInfoService appInfoService;
	
	@PostConstruct
	public void onInit() {
		appInfoCache = this;
		appInfoCache.appInfoService = this.appInfoService;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspAppInfoEntity> getAppInfoMap() {
		return (Hashtable<String, CspAppInfoEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_APPINFO_CACHE_KEY);
	}
	
	public static CspAppInfoEntity getAppInfo(String appid) {
		return getAppInfoMap().get(appid);
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspAppInfoEntity> list = appInfoCache.appInfoService.getAll();
			logger.debug("----appinfo list----\n" 
					+ JSON.toJSONString(list));
			if (list != null && !list.isEmpty()) {
				Hashtable<String, CspAppInfoEntity> ht = new Hashtable<>();
				
				for (int i = 0; i < list.size(); i++) {
					CspAppInfoEntity appInfo = list.get(i);
					ht.put(appInfo.getAppid(), appInfo);
				}
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_APPINFO_CACHE_KEY, ht);
			}
		} catch(Exception e) {
			logger.error("refresh appinfo cache", e);
		}
	}
}
