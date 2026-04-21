package com.jyw.csp.service.cache;

import java.util.Date;
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
import com.jyw.csp.entity.CspSysConfigEntity;
import com.jyw.csp.service.CspSysConfigService;
import com.jyw.csp.util.string.StringUtils;

@Component
public class CspSysConfigCache {
	
	private final static Logger logger = LoggerFactory.getLogger(CspSysConfigCache.class);
	
	public static CspSysConfigCache sysConfigCache;
	
	@Autowired
	private CspSysConfigService sysConfigService;

	protected static Hashtable<String,String> countTable = new Hashtable<String,String>();

	protected static Hashtable<String,String> timeTable = new Hashtable<String,String>();
	
	// 100000次数刷新一次内存的参数列表

	private final static String countCachePara100000 = " ,INL_CARDBIN,";
	
	@PostConstruct
	public void onInit() {
		sysConfigCache = this;
		sysConfigCache.sysConfigService = this.sysConfigService;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspSysConfigEntity> getSysConfigMap() {
		return (Hashtable<String, CspSysConfigEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_SYSCONFIG_CACHE_KEY);
	}
	
	/**
	 * 获取配置参数Int类型值
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getIntValue(String key, int defaultValue) {
		CspSysConfigEntity sysConfig = getSysConfigMap().get(key);
		if(sysConfig == null) {
			return defaultValue;
		}
		String value = sysConfig.getConfig_value();
		
		if(StringUtils.checkEmpty(value)) {
			return defaultValue;
		}
		
		try {
			return new Integer(value).intValue();
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * 获取配置值参数字符串类型值
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getStrValue(String key, String defaultValue) {
		CspSysConfigEntity sysConfig = getSysConfigMap().get(key);
		if(sysConfig == null) {
			return defaultValue;
		}
		String value = sysConfig.getConfig_value();
		
		if(StringUtils.checkEmpty(value)) {
			return defaultValue;
		}
		
		if(countCachePara100000.indexOf("," + key + ",") >= 0) {
			return getStrValueOfCountCache(key, defaultValue, 100000);
		}
		
		return value;
	}
	
	/**
	 *  从缓存中取数，当超过maxCount次时，从数据库重新查询获取值。
	 * @param key 配置参数key
	 * @param defaultValue 默认值
	 * @param maxCount 最大读取数
	 * @return
	 */
	public static String getStrValueOfCountCache(String key,
			String defaultValue, int maxCount) {
		String s = (String) countTable.get(key);

		if (s == null)

			s = "0";

		int keyCount = Integer.parseInt(s);

		if (keyCount >= maxCount) {
			
			String value = "";

			CspSysConfigEntity sysConfig = null;
			try {
				sysConfig = sysConfigCache.sysConfigService.getByKey(key);
			} catch (Exception e) {
				sysConfig = getSysConfigMap().get(key);
				if(sysConfig == null) {
					return defaultValue;
				}
				return sysConfig.getConfig_value();
			}

			if (sysConfig == null) {
				value = "";
			}

			value = sysConfig.getConfig_value();
			if (value == null) {
				value = "";
			}

			value = value.trim();

			if (!"".equals(value)) {

				getSysConfigMap().put(key, sysConfig);
			}
		}

		synchronized (countTable) {
			if (keyCount >= maxCount) {
				keyCount = 1;
			} else {
				keyCount++;
			}
			countTable.put(key, keyCount + "");
		}
		return getStrValue(key, defaultValue);
	}
	
	/**
	 *  从缓存中取数，当超过1000次时，从数据库重新查询获取值。
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getStrValueOfCountCache(String key, String defaultValue) {

		return getStrValueOfCountCache(key, defaultValue, 1000);

	}
	
	/**
	 * 获取定时刷新的参数
	 * @param key 配置参数key
	 * @param defaultValue 默认值
	 * @param millisecond 刷新频率
	 * @return
	 */
	public static String getStrValueOfTime(String key, String defaultValue,
			long millisecond) {

		boolean isNeedUpdate = false;

		String strTime = (String) timeTable.get(key + "_refreshTime");

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
			String strValue = getStrValue(key, defaultValue);
			synchronized (timeTable) {
				timeTable.put(key, strValue);
				timeTable.put(key + "_refreshTime", strNowTime);
			}
			return strValue;
		} else {
			return (String) timeTable.get(key);
		}
	}
	
	public static void refresh() {
		try {
			List<CspSysConfigEntity> list = sysConfigCache.sysConfigService.getAll();
			
			if (list != null && !list.isEmpty()) {
				logger.debug("----sysconfig list----\n" 
						+ JSON.toJSONString(list));
				
				Hashtable<String, CspSysConfigEntity> ht = new Hashtable<>();
				
				for (int i = 0; i < list.size(); i++) {
					CspSysConfigEntity sysConfig = list.get(i);
					ht.put(sysConfig.getConfig_key(), sysConfig);
				}
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_SYSCONFIG_CACHE_KEY, ht);
			}
		} catch(Exception e) {
			logger.error("refresh flowcontrol cache", e);
		}
	}
}
