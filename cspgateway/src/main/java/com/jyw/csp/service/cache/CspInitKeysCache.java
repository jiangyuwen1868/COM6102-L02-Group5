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
import com.jyw.csp.entity.CspInitKeysEntity;
import com.jyw.csp.service.CspInitKeysService;

@Component
public class CspInitKeysCache {

	private final static Logger logger = LoggerFactory.getLogger(CspInitKeysCache.class);
	
	public static CspInitKeysCache initKeysCache;
	
	@Autowired
	public CspInitKeysService initKeysService;
	
	@PostConstruct
	public void onInit() {
		initKeysCache = this;
		initKeysCache.initKeysService = this.initKeysService;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspInitKeysEntity> getInitKeysMap() {
		return (Hashtable<String, CspInitKeysEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_INITKEYS_CACHE_KEY);
	}
	
	public static CspInitKeysEntity getInitKey(String keyid) {
		return getInitKeysMap().get(keyid);
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspInitKeysEntity> list = initKeysCache.initKeysService.getAll();
			
			Hashtable<String, CspInitKeysEntity> ht = new Hashtable<>();
			if (list != null && !list.isEmpty()) {
				logger.debug("----initkeys list----\n" 
						+ JSON.toJSONString(list));
				
				for (int i = 0; i < list.size(); i++) {
					CspInitKeysEntity keys = list.get(i);
					ht.put(keys.getKeyid(), keys);
				}
			}
			ApplicationContext.getAppContext().put(CspConstants.CSP_INITKEYS_CACHE_KEY, ht);
		} catch(Exception e) {
			logger.error("refresh initkeys cache", e);
		}
	}
}
