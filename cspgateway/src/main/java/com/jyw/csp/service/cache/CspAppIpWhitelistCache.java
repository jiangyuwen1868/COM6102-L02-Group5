package com.jyw.csp.service.cache;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.ApplicationContext;
import com.jyw.csp.entity.CspAppIpWhitelistEntity;
import com.jyw.csp.service.CspAppIpWhitelistService;

@Component
public class CspAppIpWhitelistCache {

	private final static Logger logger = LoggerFactory.getLogger(CspAppIpWhitelistCache.class);
	
	protected static Hashtable<String, Object> timeTable = new Hashtable<>();
	
	public static CspAppIpWhitelistCache appIpWhitelistCache;
	
	@Autowired
	private CspAppIpWhitelistService appIpWhitelistService;
	
	@PostConstruct
	public void onInit() {
		appIpWhitelistCache = this;
		appIpWhitelistCache.appIpWhitelistService = this.appIpWhitelistService;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, List<CspAppIpWhitelistEntity>> getAppIpWhitelistMap() {
		return (Map<String, List<CspAppIpWhitelistEntity>>)ApplicationContext.getAppContext().get(CspConstants.CSP_APPIPWHITELIST_CACHE_KEY);
	}
	
	public static List<CspAppIpWhitelistEntity> getAppIpWhitelist(String appid) {
		return getAppIpWhitelistMap().get(appid);
	}
	
	/**
	 * 获取定时刷新的应用IP白名单信息
	 * @param appid 应用ID
	 * @param millisecond 刷新频率
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CspAppIpWhitelistEntity> getAppIpWhitelistOfTime(String appid, long millisecond) {

		boolean isNeedUpdate = false;

		String strTime = (String) timeTable.get(appid + "_refreshTime");

		String strNowTime = "";

		if (strTime == null) {
			isNeedUpdate = true;
		} else {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"yyyyMMdd HH:mm:ss");
			Date now = new Date();
			strNowTime = sdf.format(now);
			try {
				if (now.getTime() - sdf.parse(strTime).getTime() > millisecond) {
					isNeedUpdate = true;
				}
			} catch (Exception e) {
				isNeedUpdate = true;
			}
		}
		if (isNeedUpdate) {
			List<CspAppIpWhitelistEntity> list = appIpWhitelistCache.appIpWhitelistService.getList(appid);
			if(list==null) {
				list = getAppIpWhitelist(appid);
			}
			synchronized (timeTable) {
				timeTable.put(appid, list);
				timeTable.put(appid + "_refreshTime", strNowTime);
			}
			return list;
		} else {
			return (List<CspAppIpWhitelistEntity>) timeTable.get(appid);
		}
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspAppIpWhitelistEntity> list = appIpWhitelistCache.appIpWhitelistService.getAll();
			
			if (list != null && !list.isEmpty()) {
				logger.debug("----appipwhitelist list----\n" 
						+ JSON.toJSONString(list));
				//根据appid分组
				Map<String, List<CspAppIpWhitelistEntity>> map = list.stream().collect(Collectors.groupingBy(CspAppIpWhitelistEntity::getAppid));
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_APPIPWHITELIST_CACHE_KEY, map);
			}
		} catch(Exception e) {
			logger.error("refresh appipwhitelist cache", e);
		}
	}
}
