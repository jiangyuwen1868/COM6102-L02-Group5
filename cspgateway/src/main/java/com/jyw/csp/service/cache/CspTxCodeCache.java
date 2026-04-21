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
import com.jyw.csp.entity.CspTxCodeEntity;
import com.jyw.csp.service.CspTxCodeService;
import com.jyw.csp.util.JsonUtils;

@Component
public class CspTxCodeCache {

	private final static Logger logger = LoggerFactory.getLogger(CspTxCodeCache.class);
	
	public static CspTxCodeCache txCodeCache;
	
	@Autowired
	private CspTxCodeService txCodeService;
	
	@PostConstruct
	public void onInit() {
		txCodeCache = this;
		txCodeCache.txCodeService = this.txCodeService;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspTxCodeEntity> getTxCodeMap() {
		return (Hashtable<String, CspTxCodeEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_TXCODE_CACHE_KEY);
	}
	
	public static CspTxCodeEntity getTxCode(String txcode) {
		return getTxCodeMap().get(txcode);
	}
	
	/**
	 * 获取交易分组ID
	 * @param txcode
	 * @return
	 */
	public static String getGroupId(String txcode) {
		return getTxCodeMap().get(txcode).getGroupid();
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspTxCodeEntity> list = txCodeCache.txCodeService.getAll();
			
			logger.debug("----txcode list----\n" 
							+ JSON.toJSONString(list));
			if (list != null && !list.isEmpty()) {
				Hashtable<String, CspTxCodeEntity> ht = new Hashtable<>();
				
				for (int i = 0; i < list.size(); i++) {
					CspTxCodeEntity txcode = list.get(i);
					ht.put(txcode.getTxcode(), txcode);
				}
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_TXCODE_CACHE_KEY, ht);
			}
		} catch(Exception e) {
			logger.error("refresh txcode cache", e);
		}
	}
}
