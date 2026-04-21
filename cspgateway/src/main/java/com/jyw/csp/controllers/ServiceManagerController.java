package com.jyw.csp.controllers;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.aop.ManagerAuthAction;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.entity.CspResourceGroupEntity;
import com.jyw.csp.entity.CspSysConfigEntity;
import com.jyw.csp.entity.CspSysDeployEntity;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.exception.CommonException;
import com.jyw.csp.monitor.MonitorFilter;
import com.jyw.csp.monitor.MonitorServer;
import com.jyw.csp.resource.httpresource.HttpCommPoolService;
import com.jyw.csp.resource.httpresource.HttpCommService;
import com.jyw.csp.resource.httpresource.HttpResouceGroup;
import com.jyw.csp.service.CspResourceGroupService;
import com.jyw.csp.service.CspSysDeployService;
import com.jyw.csp.service.cache.CspAppInfoCache;
import com.jyw.csp.service.cache.CspAppIpWhitelistCache;
import com.jyw.csp.service.cache.CspAppSrvAuthCache;
import com.jyw.csp.service.cache.CspErrorInfoCache;
import com.jyw.csp.service.cache.CspFlowControlCache;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.service.cache.CspTxCodeCache;
import com.jyw.csp.util.Utils;
import com.jyw.csp.util.string.StringUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/serviceManager")
public class ServiceManagerController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private CspResourceGroupService resourceGroupService;
	@Autowired
	private CspSysDeployService sysDeployService;

	/**
	 * 系统配置参数查询
	 * 
	 * @param data
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/sysConfig/{param}", consumes = "application/json", produces = "application/json; charset=utf-8")
	@ResponseBody
	@ManagerAuthAction
	public JSONObject sysConfig(@RequestBody(required = false) JSONObject data, @PathVariable("param") String param) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("errorCode", ErrorInfo.SUCCESS.getCode());
		result.put("errorMsg", ErrorInfo.SUCCESS.getInfo());
		try {

			if ("get".equals(param) || "rePut".equals(param)) {
				String config_key = data.getString("config_key");
				String config_value = data.getString("config_value");
				CspSysConfigEntity entity = CspSysConfigCache.getSysConfigMap().get(config_key);
				if (entity == null) {
					result.put("errorCode", "MNG100000001");
					result.put("errorMsg", "无缓存数据");
				} else {
					if ("get".equals(param)) {
						result.put("value", entity);
					} else {
						entity.setConfig_value(config_value);
						CspSysConfigCache.getSysConfigMap().put(config_key, entity);
					}
				}
			} else if ("refresh".equals(param)) {
				CspSysConfigCache.refresh();
			}
		} catch (Exception e) {
			logger.error("sysConfig", e);
			result.put("errorCode", "MNG100009999");
			result.put("errorMsg", "操作失败:" + e.getMessage());
		}

		return (JSONObject) JSONObject.toJSON(result);
	}

	/**
	 * 系统资源监控
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sysMonitor", produces = "application/json; charset=utf-8")
	@ResponseBody
	@ManagerAuthAction
	public JSONObject sysMonitor() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("errorCode", ErrorInfo.SUCCESS.getCode());
		result.put("errorMsg", ErrorInfo.SUCCESS.getInfo());

		try {

			Hashtable<String, String> ht = MonitorFilter.getMonitor();
//			StringBuilder sb = new StringBuilder();
//			sb.append(ht.get(MonitorFilter.TOTALMEMORY));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.MAXMEMORY));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.FREEMEMORY));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.MEMORYRATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.HOSTTOTALMEMORY));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.HOSTFREEMEMORY));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.HOSTMEMORYRATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.CPURATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.DISKRATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.IORATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.NETRATE));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.TRANCTCOUNT));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.TRANERRCOUNT));
//			sb.append("|");
//			sb.append(ht.get(MonitorFilter.SYSERRCOUNT));
//			sb.append("|");
			result.put("result", ht);
		} catch (Exception e) {
			logger.error("sysMonitor", e);
			result.put("errorCode", "MNG100009999");
			result.put("errorMsg", "操作失败:" + e.getMessage());
		}

		return (JSONObject) JSONObject.toJSON(result);
	}

	/**
	 * 刷新缓存参数
	 * 
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/cacheRefresh/{param}", produces = "application/json; charset=utf-8")
	@ResponseBody
	@ManagerAuthAction
	public JSONObject cacheRefresh(@PathVariable("param") String param) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("errorCode", ErrorInfo.SUCCESS.getCode());
		result.put("errorMsg", ErrorInfo.SUCCESS.getInfo());

		try {
			switch (param) {
			case "appInfo":
				CspAppInfoCache.refresh();
				CspAppSrvAuthCache.refresh();
				CspAppIpWhitelistCache.refresh();
				break;
			case "appIpWhitelist":
				CspAppIpWhitelistCache.refresh();
				break;
			case "errorInfo":
				CspErrorInfoCache.refresh();
				break;
			case "flowControl":
				CspFlowControlCache.refresh();
				break;
			case "txCode":
				CspTxCodeCache.refresh();
			case "appSrvAuth":
				CspAppSrvAuthCache.refresh();
				break;
			case "sysConfig":
				CspSysConfigCache.refresh();
				break;
			case "httpResource":
				refreshHttpResource();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("cacheRefresh", e);
			result.put("errorCode", "MNG100009999");
			result.put("errorMsg", "操作失败:" + e.getMessage());
		}

		return (JSONObject) JSONObject.toJSON(result);
	}

	@RequestMapping(value = "/httpResource/{param}", produces = "application/json; charset=utf-8")
	@ResponseBody
	@ManagerAuthAction
	public JSONObject httpResource(@RequestBody(required = false) JSONObject data, @PathVariable("param") String param) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("errorCode", ErrorInfo.SUCCESS.getCode());
		result.put("errorMsg", ErrorInfo.SUCCESS.getInfo());

		try {
			boolean ret = false;
			switch (param) {
			case "selectAll":
				result.put("list", HttpCommPoolService.HTTPCommPool.getResourceGroup());
				ret = true;
				break;
			case "addHttpResource":
				ret = addHttpResource(data);
				break;
			case "deleteHttpResource":
				ret = deleteHttpResource(data);
				break;
			case "updateHttpResource":
				ret = updateHttpResource(data);
				break;
			case "refreshHttpResource":
				ret = refreshHttpResource();
				break;
			default:
				break;
			}
			if(!ret) {
				result.put("errorCode", "MNG100000002");
				result.put("errorMsg", "操作失败");
			}
		} catch (Exception e) {
			logger.error("httpResource", e);
			result.put("errorCode", "MNG100009999");
			result.put("errorMsg", "操作错误:" + e.getMessage());
		}

		return (JSONObject) JSONObject.toJSON(result);
	}

	private boolean addHttpResource(JSONObject data) {
		String groupid = (String) data.get("groupId");
		String serviceUrl = (String) data.get("url");
		if (StringUtils.hasText(groupid) && StringUtils.hasText(serviceUrl)) {
			Hashtable<?, ?> resourceGroups = (Hashtable<?, ?>) HttpCommPoolService.HTTPCommPool.getResourceGroup();
			HttpResouceGroup group = (HttpResouceGroup) resourceGroups.get(groupid);
			if (group == null) {
				return false;
			}
			HttpCommService service = new HttpCommService();
			service.setHttpURL(serviceUrl);
			service.isRight = true;
			group.addHttpCommService(service);
			group.count = 0;
			return true;
		} else {
			return false;
		}
	}

	private boolean deleteHttpResource(JSONObject data) {
		String groupid = (String) data.get("groupId");
		String indexStr = (String) data.get("index");
		try {
			if (StringUtils.hasText(groupid) && StringUtils.hasText(indexStr)) {
				int index = Integer.parseInt(indexStr);
				Hashtable<?, ?> resourceGroups = (Hashtable<?, ?>) HttpCommPoolService.HTTPCommPool.getResourceGroup();
				HttpResouceGroup group = (HttpResouceGroup) resourceGroups.get(groupid);
				if (group == null) {
					return false;
				}
				group.vResouce.remove(index);
				group.count = 0;
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean updateHttpResource(JSONObject data) {
		String groupId = (String) data.get("groupId");
		if (StringUtils.hasText(groupId)) {
			try {
				Hashtable<?, ?> resourceGroups = (Hashtable<?, ?>) HttpCommPoolService.HTTPCommPool.getResourceGroup();
				HttpResouceGroup mGroup = (HttpResouceGroup) resourceGroups.remove(groupId);
				mGroup.terminate();

				List<CspResourceGroupEntity> listRG = resourceGroupService.getAll();
				List<CspSysDeployEntity> listSD = sysDeployService.getAll();

				if (listRG != null && !listRG.isEmpty()) {
					if (listSD == null || listSD.isEmpty()) {
						throw new CommonException("XDBSD1009999", "查询系统部署表结果为空，请确认系统部署表配置！");
					}
					for (int i = 0; i < listRG.size(); i++) {
						CspResourceGroupEntity rgPo = listRG.get(i);

						if (!groupId.equals(rgPo.getGroupid())) {
							continue;
						}

						HttpResouceGroup group = new HttpResouceGroup();
						group.setGroupID(rgPo.getGroupid());
						group.setContentType(rgPo.getContenttype());
						group.setReqMethod(rgPo.getReqmethod());
						group.setConnectTimeout(rgPo.getConntimeout());
						group.setSoTimeout(rgPo.getSotimeout());
						group.setMAX_REQ(rgPo.getConnmaxsize());
						group.setHeartInteval(rgPo.getHeartinteval());
						group.setLb_strategy(rgPo.getLbstrategy());
						group.setBalance(Boolean.parseBoolean(rgPo.getIsbalance()));
						group.setUseProxyAuthor(rgPo.getIsuseproxyauthor());

						for (int j = 0; j < listSD.size(); j++) {
							CspSysDeployEntity sdPo = listSD.get(j);
							if (rgPo.getGroupid().equals(sdPo.getGroupid())) {
								String url = "http://" + sdPo.getSysip() + ":" + sdPo.getSysport() + sdPo.getServaddr();
								boolean isUseful = CspConstants.SYS_DEPLOY_USEFULLY_Y.equals(sdPo.getIsuseful());
								if (isUseful) {
									HttpCommService http = new HttpCommService();
									logger.info(rgPo.getGroupid() + ":setHttpURL=" + url);
									http.setHttpURL(url);
									http.isRight = isUseful;
									group.addHttpCommService(http);
								} else {
									logger.debug("[" + url + "] not available!!!");
									// logger.error("["+url+"] not available!!!");
								}
							}
						}
						group.count = 0;// 从零开始轮询
						if (group.vResouce == null || group.vResouce.size() == 0) {
							logger.debug(rgPo.getGroupid()
									+ ":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
							logger.error(rgPo.getGroupid()
									+ ":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
						}
						HttpCommPoolService.HTTPCommPool.addResoureGroup(rgPo.getGroupid(), group);
					}
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean refreshHttpResource() {
		try {
			logger.info("begin initHttpCommPool...");
			HttpCommPoolService httpCommPool = HttpCommPoolService.HTTPCommPool;
			if (httpCommPool == null) {
				logger.debug("get HTTPCommPool is null, new HttpCommPoolService!");
				httpCommPool = new HttpCommPoolService();
			}
			
			httpCommPool.clearResourceGroup();

			List<CspResourceGroupEntity> listRG = resourceGroupService.getAll();
			List<CspSysDeployEntity> listSD = sysDeployService.getAll();

			if (listRG != null && !listRG.isEmpty()) {
				if (listSD == null || listSD.isEmpty()) {
					throw new CommonException("XDBSD1009999", "查询系统部署表结果为空，请确认系统部署表配置！");
				}
				for (int i = 0; i < listRG.size(); i++) {
					CspResourceGroupEntity rgPo = listRG.get(i);

					HttpResouceGroup group = new HttpResouceGroup();
					group.setGroupID(rgPo.getGroupid());
					group.setContentType(rgPo.getContenttype());
					group.setReqMethod(rgPo.getReqmethod());
					group.setConnectTimeout(rgPo.getConntimeout());
					group.setSoTimeout(rgPo.getSotimeout());
					group.setMAX_REQ(rgPo.getConnmaxsize());
					group.setHeartInteval(rgPo.getHeartinteval());
					group.setLb_strategy(rgPo.getLbstrategy());
					group.setBalance(Boolean.parseBoolean(rgPo.getIsbalance()));
					group.setUseProxyAuthor(rgPo.getIsuseproxyauthor());

					for (int j = 0; j < listSD.size(); j++) {
						CspSysDeployEntity sdPo = listSD.get(j);
						if (rgPo.getGroupid().equals(sdPo.getGroupid())) {
							String url = "http://" + sdPo.getSysip() + ":" + sdPo.getSysport() + sdPo.getServaddr();
							boolean isUseful = CspConstants.SYS_DEPLOY_USEFULLY_Y.equals(sdPo.getIsuseful());
							if (isUseful) {
								HttpCommService http = new HttpCommService();
								logger.info(rgPo.getGroupid() + ":setHttpURL=" + url);
								http.setHttpURL(url);
								http.isRight = isUseful;
								group.addHttpCommService(http);
							} else {
								logger.debug("[" + url + "] not available!!!");
								// logger.error("["+url+"] not available!!!");
							}
						}
					}
					group.count = 0;// 从零开始轮询
					if (group.vResouce == null || group.vResouce.size() == 0) {
						logger.debug(rgPo.getGroupid()
								+ ":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
						logger.error(rgPo.getGroupid()
								+ ":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
					}
					httpCommPool.addResoureGroup(rgPo.getGroupid(), group);
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@PostMapping("/sysMonitorAll")
	@ResponseBody
	public Object sysMonitorAll(HttpServletRequest request) throws Exception
	{
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("errorCode", "000000000000");
		result.put("errorMsg", "处理成功");
		List<CspSysDeployEntity> list = sysDeployService.getAll();
		if(list.size()==0) {
            logger.error("MNG300009999","无部署资源列表");
            result.put("errorCode", "MNG300009999");
            result.put("errorMsg", "无部署资源列表");
            return (JSONObject) JSONObject.toJSON(result);
        }

		String clientIp = Utils.getClientIpAddr(request);
		String serverName = request.getServerName();
		logger.info("cspGateway.sysMonitorAll --clientIp:" + clientIp);
		logger.info("cspGateway.sysMonitorAll --serverName:" + serverName);

		boolean isPass = false;
		for(CspSysDeployEntity entity : list) {
			String deployIp = entity.getSysip();
			if(deployIp.equals(clientIp)||deployIp.equals(serverName)) {
				isPass = true;
				break;
			}
		}

		if(!isPass) {
            logger.error("MNG310009999","非法操作!");
            result.put("errorCode", "MNG310009999");
            result.put("errorMsg", "非法操作");
            return (JSONObject) JSONObject.toJSON(result);
		}

		MonitorServer server = new MonitorServer();
		server.copyTo();
		result.put("result", server);
		return (JSONObject) JSONObject.toJSON(result);
	}
}
