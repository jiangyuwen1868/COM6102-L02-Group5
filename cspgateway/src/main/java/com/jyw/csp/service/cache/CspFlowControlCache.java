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
import com.jyw.csp.entity.CspFlowControlEntity;
import com.jyw.csp.flowctrl.FlowControl;
import com.jyw.csp.service.CspFlowControlService;

@Component
public class CspFlowControlCache {
	
	private final static Logger logger = LoggerFactory.getLogger(CspFlowControlCache.class);
	
	public static CspFlowControlCache flowControlCache;
	
	@Autowired
	private CspFlowControlService flowControlService;
	
	@PostConstruct
	public void onInit() {
		flowControlCache = this;
		flowControlCache.flowControlService = this.flowControlService;
	}

	@SuppressWarnings("unchecked")
	public static Hashtable<String, CspFlowControlEntity> getFlowControlMap() {
		return (Hashtable<String, CspFlowControlEntity>)ApplicationContext.getAppContext().get(CspConstants.CSP_FLOWCONTROL_CACHE_KEY);
	}
	
	public static Integer getTPS(String appid, String txcode) {
		try {
			String key = appid + CspConstants.SEPARATOR_CONNECTOR_A + txcode;
			
			CspFlowControlEntity flowControl = getFlowControlMap().get(key);
			if(flowControl!=null) {
				return flowControl.getTpscount();
			}
			
			return 0;
		} catch(Exception e) {
			return 0;
		}
	}
	
	public static void refresh() {
		try {
			List<CspFlowControlEntity> list = flowControlCache.flowControlService.getAll();
			logger.debug("----flowcontrol list----\n" 
					+ JSON.toJSONString(list));
			if (list != null && !list.isEmpty()) {
				Hashtable<String, CspFlowControlEntity> ht = new Hashtable<>();
				
				for (int i = 0; i < list.size(); i++) {
					CspFlowControlEntity flowControl = list.get(i);
					ht.put(flowControl.getAppid() + 
							CspConstants.SEPARATOR_CONNECTOR_A + 
							flowControl.getTxcode(), flowControl);
				}
				
				ApplicationContext.getAppContext().put(CspConstants.CSP_FLOWCONTROL_CACHE_KEY, ht);
				
				FlowControl.reLoad();
			}
		} catch(Exception e) {
			logger.error("refresh flowcontrol cache", e);
		}
	}
}
