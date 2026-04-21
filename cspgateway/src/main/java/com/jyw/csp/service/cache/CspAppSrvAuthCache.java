package com.jyw.csp.service.cache;

import java.util.Date;
import java.util.HashMap;
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
import com.jyw.csp.entity.CspAppSrvAuthEntity;
import com.jyw.csp.service.CspAppSrvAuthService;

@Component
public class CspAppSrvAuthCache {

	private final static Logger logger = LoggerFactory.getLogger(CspAppSrvAuthCache.class);
	
	protected static Hashtable<String, Object> timeTable = new Hashtable<>();
	
	public static CspAppSrvAuthCache appSrvAuthCache;
	
	@Autowired
	private CspAppSrvAuthService appSrvAuthService;
	
	@PostConstruct
	public void onInit() {
		appSrvAuthCache = this;
		appSrvAuthCache.appSrvAuthService = this.appSrvAuthService;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, List<CspAppSrvAuthEntity>> getAppSrvAuthMap() {
		return (Map<String, List<CspAppSrvAuthEntity>>)ApplicationContext.getAppContext().get(CspConstants.CSP_APPSRVAUTH_CACHE_KEY);
	}
	
	public static List<CspAppSrvAuthEntity> getAppSrvAuthList(String appid) {
		return getAppSrvAuthMap().get(appid);
	}
	
	/**
	 * 获取定时刷新的应用服务授权信息
	 * @param appid 应用ID
	 * @param millisecond 刷新频率
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CspAppSrvAuthEntity> getAppSrvAuthListOfTime(String appid, long millisecond) {

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
			List<CspAppSrvAuthEntity> list = appSrvAuthCache.appSrvAuthService.getList(appid);
			if(list==null) {
				list = getAppSrvAuthList(appid);
			}
			synchronized (timeTable) {
				timeTable.put(appid, list);
				timeTable.put(appid + "_refreshTime", strNowTime);
			}
			return list;
		} else {
			return (List<CspAppSrvAuthEntity>) timeTable.get(appid);
		}
	}
	
	/**
	 * 刷新缓存
	 */
	public static void refresh() {
		try {
			List<CspAppSrvAuthEntity> list = appSrvAuthCache.appSrvAuthService.getAll();
			Map<String, List<CspAppSrvAuthEntity>> map = new HashMap<>();
			if(list!=null && !list.isEmpty()) {
				logger.debug("----appsrvauth list----\n" 
						+ JSON.toJSONString(list));
				//根据appid分组
				map = list.stream().collect(Collectors.groupingBy(CspAppSrvAuthEntity::getAppid));
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_APPSRVAUTH_CACHE_KEY, map);
			}
		} catch(Exception e) {
			logger.error("refresh appsrvauth cache", e);
		}
	}
}
