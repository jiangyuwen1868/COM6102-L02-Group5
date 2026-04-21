package com.jyw.csp.flowctrl;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.entity.CspFlowControlEntity;
import com.jyw.csp.exception.ActionFlowExceedException;
import com.jyw.csp.service.cache.CspFlowControlCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.util.string.StringUtils;

public class FlowControl {
	
	private final static Logger logger = LoggerFactory.getLogger(FlowControl.class);

	// 受限交易动态变化表（数据库配置）
	private static Hashtable<String, CspFlowControlEntity> limitedOpListConfig = CspFlowControlCache.getFlowControlMap(); 
	// 受限交易列表（联机交易）
	private static Hashtable<String, FlowCtrlPrimaryKeyVo> opList = new Hashtable<String, FlowCtrlPrimaryKeyVo>();
	
	
	public static void reLoad() {
		limitedOpListConfig = CspFlowControlCache.getFlowControlMap();
	}
	/**
	 * 交易并发访问控制：当前控制数加一
	 * 
	 * @param operKey
	 *            java.lang.String
	 */
	public static void opTpsLock(String operKey)
			throws ActionFlowExceedException {
		logger.debug("----opTpsLock operKey:" + operKey);
		if (StringUtils.isEmpty(operKey))
			return;
		boolean limitEnable = false;
		try {
			String enableFlag = CspSysConfigCache.getStrValue("cspGateway.flowControlEnable", "true");
			limitEnable = Boolean.valueOf(enableFlag);
		} catch (Exception e) {
		}
		if (!limitEnable)
			return;
		if (FlowControl.limitedOpListConfig.get(operKey) != null) {
			String opswitch = FlowControl.limitedOpListConfig.get(operKey).getOpswitch();
			logger.debug("----opswitch:" + opswitch);
			if(CspConstants.FLOWCONTROL_OPSWITCH_N.equals(opswitch)) {
				return;
			}
			int maxNum = -1;
			try {
				maxNum = FlowControl.limitedOpListConfig.get(operKey).getTpscount();
			} catch (Exception ei) {
				FlowControl.limitedOpListConfig.remove(operKey);
			}
			if (maxNum <= 0) {
				throw new ActionFlowExceedException("XTPS10000010", "交易并发数限制：交易(" + operKey
						+ ")超过了最大并发数(0)");
			}

			if (FlowControl.opList.get(operKey) != null) {
				synchronized (opList) {
					Integer tNum = FlowControl.opList.get(operKey).incrementAndGet();
					logger.debug("incrementAndGet:" + tNum);
				}
				
				int curNum = FlowControl.opList.get(operKey).get();
				logger.debug("-交易["+ operKey +"] 当前并发数[" + curNum + "] 最大并发数[" + maxNum + "]");
				if (curNum > maxNum) {
					throw new ActionFlowExceedException("XTPS10000011", "交易并发数限制：交易(" + operKey
							+ ")超过了最大并发数(" + maxNum + ")");
				}

			} else {
				logger.debug("----init opList----");
				synchronized (opList) {
					FlowCtrlPrimaryKeyVo vo = new FlowCtrlPrimaryKeyVo(operKey);
					FlowControl.opList.put(operKey, vo);
				}
			}
		}
	}

	/**
	 * 交易并发访问控制：当前控制数减一
	 * 
	 * @param operKey
	 *            java.lang.String
	 */
	public static void opTpsUnlock(String operKey) {
		if (StringUtils.isEmpty(operKey)) {
			return;
		}
		boolean limitEnable = false;
		try {
			String enableFlag = CspSysConfigCache.getStrValue("cspGateway.flowControlEnable", "true");
			limitEnable = Boolean.valueOf(enableFlag);
		} catch (Exception e) {
		}
		if (!limitEnable)
			return;

		if (FlowControl.opList.get(operKey) == null)
			return;
		try {
			synchronized (opList) {

				int curNum = FlowControl.opList.get(operKey).get();
				logger.debug("--交易["+ operKey +"] 当前并发数[" + curNum + "]");
				Integer tNum = FlowControl.opList.get(operKey).decrementAndGet();
				logger.debug("decrementAndGet:" + tNum);
			}
		} catch (Exception e) {
			try {
				FlowControl.limitedOpListConfig.remove(operKey);
			} catch (Exception ex) {
			}
			FlowControl.opList.remove(operKey);
		}
	}
}
