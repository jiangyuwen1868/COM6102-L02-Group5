package com.jyw.csp;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.ApplicationContext;
import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.entity.CspAppIpWhitelistEntity;
import com.jyw.csp.entity.CspAppSrvAuthEntity;
import com.jyw.csp.entity.CspErrorInfoEntity;
import com.jyw.csp.entity.CspFlowControlEntity;
import com.jyw.csp.entity.CspHsmGroupEntity;
import com.jyw.csp.entity.CspHsmInfoEntity;
import com.jyw.csp.entity.CspInitKeysEntity;
import com.jyw.csp.entity.CspResourceGroupEntity;
import com.jyw.csp.entity.CspSysConfigEntity;
import com.jyw.csp.entity.CspSysDeployEntity;
import com.jyw.csp.entity.CspTxCodeEntity;
import com.jyw.csp.enums.HsmCategory;
import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.resource.httpresource.HttpCommPoolService;
import com.jyw.csp.resource.httpresource.HttpCommService;
import com.jyw.csp.resource.httpresource.HttpResouceGroup;
import com.jyw.csp.service.CspAppInfoService;
import com.jyw.csp.service.CspAppIpWhitelistService;
import com.jyw.csp.service.CspAppSrvAuthService;
import com.jyw.csp.service.CspErrorInfoService;
import com.jyw.csp.service.CspFlowControlService;
import com.jyw.csp.service.CspHsmGroupService;
import com.jyw.csp.service.CspHsmInfoService;
import com.jyw.csp.service.CspInitKeysService;
import com.jyw.csp.service.CspResourceGroupService;
import com.jyw.csp.service.CspSysConfigService;
import com.jyw.csp.service.CspSysDeployService;
import com.jyw.csp.service.CspTxCodeService;
import com.jyw.csp.service.cache.CspSysConfigCache;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.string.StringUtils;
import com.jyw.csp.util.xml.XmlReadView;
import com.jyw.csp.util.xml.XmlReader;

@Component
public class CspGatewayApplicationRunner implements ApplicationRunner {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CspTxCodeService txCodeService;
	@Autowired
	private CspAppInfoService appInfoService;
	@Autowired
	private CspErrorInfoService errorInfoService;
	@Autowired
	private CspFlowControlService flowControlService;
	@Autowired
	private CspSysConfigService sysConfigService;
	@Autowired
	private CspAppIpWhitelistService appIpWhitelistService;
	@Autowired
	private CspAppSrvAuthService appSrvAuthService;
	@Autowired
	private CspResourceGroupService resourceGroupService;
	@Autowired
	private CspSysDeployService sysDeployService;
	@Autowired
	private CspInitKeysService initKeysService;
	
	@Resource
    private CspHsmGroupService hsmGroupService;
    @Resource
    private CspHsmInfoService hsmInfoService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String[] sourceArgs = args.getSourceArgs();
		for (String arg : sourceArgs) {
            System.out.print("getSourceArgs:" + arg + " ");
        }
		
		
		logger.debug("***************CspGatewayApplicationRunner***************");
		autoInsertDeployInfo();
		loadInitKeys();
		loadSysConfig();
		loadAppInfo();
		loadTxCode();
		loadErrorInfo();
		loadFlowControl();
		loadAppIpWhitelist();
		loadAppSrvAuth();
		initHttpCommPool();
		
		String test = CspSysConfigCache.getStrValue("sys.index.skinName", "");
		logger.debug("********test********"+test);
	}
	
	
	
	/**
	 * 服务器启动时自动插入系统部署信息，资源分组，默认:csp01
	 */
	protected void autoInsertDeployInfo() {
		try {
			CspSysDeployEntity sysDeploy = new CspSysDeployEntity();
			sysDeploy.setGroupid(CspConstants.CSP_DEFUALT_RESOURCE_GROUPID);
			sysDeploy.setDeployid(LocalMessage.getHostName());
			sysDeploy.setHostname(LocalMessage.getHostName());
			sysDeploy.setUsername(CspConstants.CSP_APPCODE);
			sysDeploy.setDeploycode(CspConstants.CSP_APPCODE);
			sysDeploy.setAppcode(CspConstants.CSP_APPCODE);
			sysDeploy.setSysip(LocalMessage.getLocalIP().get(0));
			sysDeploy.setSysport(CspGatewayApplication.webServerPort);
			sysDeploy.setServaddr(CspConstants.CSP_SERVERPATH);
			sysDeploy.setIsuseful(CspConstants.CSP_DEFUALT_SERVER_ISUSEFUL);
			sysDeploy.setWeight(0);
			sysDeploy.setRemark("密码服务平台接入网关");
			
			sysDeployService.initMyDeploy(sysDeploy);
		} catch(Exception e) {
			logger.warn("autoInsertDeployInfo", e);
		}
	}
	
	/**
	 * 加载平台初始化密钥
	 */
	public void loadInitKeys() {
		List<CspInitKeysEntity> list = initKeysService.getAll();
		
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
	}
	
	/**
	 * 加载系统交易码
	 */
	public void loadTxCode() {
		List<CspTxCodeEntity> list = txCodeService.getAll();
	
		Hashtable<String, CspTxCodeEntity> ht = new Hashtable<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----txcode list----\n" 
					+ JSON.toJSONString(list));
			
			for (int i = 0; i < list.size(); i++) {
				CspTxCodeEntity txcode = list.get(i);
				ht.put(txcode.getTxcode(), txcode);
			}
		}
		ApplicationContext.getAppContext().put(CspConstants.CSP_TXCODE_CACHE_KEY, ht);
	}
	
	/**
	 * 加载应用信息
	 */
	public void loadAppInfo() {
		List<CspAppInfoEntity> list = appInfoService.getAll();
		Hashtable<String, CspAppInfoEntity> ht = new Hashtable<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----appinfo list----\n" 
					+ JSON.toJSONString(list));
			
			for (int i = 0; i < list.size(); i++) {
				CspAppInfoEntity appInfo = list.get(i);
				ht.put(appInfo.getAppid(), appInfo);
			}
		}
		ApplicationContext.getAppContext().put(CspConstants.CSP_APPINFO_CACHE_KEY, ht);
	}
	
	/**
	 * 加载系统错误码信息
	 */
	public void loadErrorInfo() {
		List<CspErrorInfoEntity> list = errorInfoService.getAll();
		Hashtable<String, CspErrorInfoEntity> ht = new Hashtable<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----errorinfo list----\n" 
					+ JSON.toJSONString(list));
			for (int i = 0; i < list.size(); i++) {
				CspErrorInfoEntity errorInfo = list.get(i);
				ht.put(errorInfo.getErrorcode(), errorInfo);
			}
		}
		ApplicationContext.getAppContext().put(CspConstants.CSP_ERRORINFO_CACHE_KEY, ht);
	}
	
	/**
	 * 加载流控信息
	 */
	public void loadFlowControl() {
		List<CspFlowControlEntity> list = flowControlService.getAll();
		Hashtable<String, CspFlowControlEntity> ht = new Hashtable<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----flowcontrol list----\n" 
					+ JSON.toJSONString(list));
			
			for (int i = 0; i < list.size(); i++) {
				CspFlowControlEntity flowControl = list.get(i);
				ht.put(flowControl.getAppid() + 
						CspConstants.SEPARATOR_CONNECTOR_A + 
						flowControl.getTxcode(), flowControl);
			}
		}
		ApplicationContext.getAppContext().put(CspConstants.CSP_FLOWCONTROL_CACHE_KEY, ht);
	}
	
	/**
	 * 加载系统参数配置信息
	 */
	public void loadSysConfig() {
		List<CspSysConfigEntity> list = sysConfigService.getAll();
		Hashtable<String, CspSysConfigEntity> ht = new Hashtable<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----sysconfig list----\n" 
					+ JSON.toJSONString(list));
			
			for (int i = 0; i < list.size(); i++) {
				CspSysConfigEntity sysConfig = list.get(i);
				ht.put(sysConfig.getConfig_key(), sysConfig);
			}
		}
		ApplicationContext.getAppContext().put(CspConstants.CSP_SYSCONFIG_CACHE_KEY, ht);
	}
	
	/**
	 * 加载应用IP白名单信息
	 */
	public void loadAppIpWhitelist() {
		List<CspAppIpWhitelistEntity> list = appIpWhitelistService.getAll();
		Map<String, List<CspAppIpWhitelistEntity>> map = new HashMap<>();
		if (list != null && !list.isEmpty()) {
			logger.debug("----appipwhitelist list----\n" 
					+ JSON.toJSONString(list));
			//根据appid分组
			map = list.stream().collect(Collectors.groupingBy(CspAppIpWhitelistEntity::getAppid));
		}
		
		ApplicationContext.getAppContext().put(CspConstants.CSP_APPIPWHITELIST_CACHE_KEY, map);
	}
	
	/**
	 * 加载应用服务授权信息
	 */
	public void loadAppSrvAuth() {
		List<CspAppSrvAuthEntity> list = appSrvAuthService.getAll();
		Map<String, List<CspAppSrvAuthEntity>> map = new HashMap<>();
		if(list!=null && !list.isEmpty()) {
			logger.debug("----appsrvauth list----\n" 
					+ JSON.toJSONString(list));
			//根据appid分组
			map = list.stream().collect(Collectors.groupingBy(CspAppSrvAuthEntity::getAppid));
		}
		
		ApplicationContext.getAppContext().put(CspConstants.CSP_APPSRVAUTH_CACHE_KEY, map);
	}

	/**
	 * 初始化HTTP通讯连接池
	 */
	public void initHttpCommPool() {
		logger.debug("-------------CspGatewayApplicationRunner.initHttpCommPool--------------------");
		
		//fastjson在将bean转换为json时，fastjson会先判断propertyName长度大于1、且头两个字符都是大写时，不做转换
		//TypeUtils.compatibleWithJavaBean = true;
		
		logger.info("begin initHttpCommPool...");
		HttpCommPoolService httpCommPool = HttpCommPoolService.HTTPCommPool;
		if (httpCommPool == null) {
			logger.debug("get HTTPCommPool is null, new HttpCommPoolService!");
			httpCommPool = new HttpCommPoolService();
		}
		
		List<CspResourceGroupEntity> listRG = resourceGroupService.getAll();
		List<CspSysDeployEntity> listSD = sysDeployService.getAll();
		
		if(listRG!=null&&!listRG.isEmpty()){
			if(listSD==null ||listSD.isEmpty()){
				throw new CommonRuntimeException("XDBSD1009999", "查询系统部署表结果为空，请确认系统部署表配置！");
			}
			for(int i=0;i<listRG.size();i++){
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
				
				for(int j=0;j<listSD.size();j++){
					CspSysDeployEntity sdPo = listSD.get(j);
					if(rgPo.getGroupid().equals(sdPo.getGroupid())){
						String url = "http://"+sdPo.getSysip()+":"+sdPo.getSysport()+sdPo.getServaddr();
						boolean isUseful = CspConstants.SYS_DEPLOY_USEFULLY_Y.equals(sdPo.getIsuseful());
						if(isUseful){
							HttpCommService http = new HttpCommService();
							logger.info(rgPo.getGroupid()+":setHttpURL="+url);
							http.setHttpURL(url);
							http.isRight = isUseful;
							group.addHttpCommService(http);
						}else{
							logger.debug("["+url+"] not available!!!");
							//logger.error("["+url+"] not available!!!");
						}
					}
				}
				group.count = 0;//从零开始轮询
				if(group.vResouce==null || group.vResouce.size()==0){
					logger.debug(rgPo.getGroupid()+":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
					logger.error(rgPo.getGroupid()+":未匹配系统部署配置*******************请检查分组资源与系统部署配置关系************************");
				}
				httpCommPool.addResoureGroup(rgPo.getGroupid(), group);
			}
			
			
			// 初始化时间戳服务器http资源
			try {
	            logger.debug("==========Init hsm http resources");
	            if (hsmGroupService == null || hsmInfoService == null) {
	                logger.error("hsmGroupService and hsmInfoService can't be null");
	                return;
	            }

	            List<CspHsmGroupEntity> hsmGroupEntityList = hsmGroupService.getListByCategory(HsmCategory.CATEGORY04.getCode());
	            if (hsmGroupEntityList != null && !hsmGroupEntityList.isEmpty()) {
	                int poolSize = 100;
	                for (int i = 0, length = hsmGroupEntityList.size(); i < length; i++) {
	                    CspHsmGroupEntity hsmGroupEntity = hsmGroupEntityList.get(i);
	                    if (hsmGroupEntity.getConnectPoolSize() > poolSize) {
	                        poolSize = hsmGroupEntity.getConnectPoolSize();
	                    }
	                    
	                    HttpResouceGroup group = new HttpResouceGroup();
	    				group.setGroupID(String.valueOf(hsmGroupEntity.getHsmGroupId()));
	    				group.setConnectTimeout(hsmGroupEntity.getConnectTimeout());
	    				group.setSoTimeout(hsmGroupEntity.getConnectTimeout());
	    				group.setMAX_REQ(poolSize);
	    				group.setHeartInteval(hsmGroupEntity.getConnectTimeout());
	    				group.setLb_strategy(CspConstants.HTTP_LB_STRATEGY_ROUNDROBIN);
	    				group.setBalance(true);
	    				group.setUseProxyAuthor("FALSE");
	                    
	                    List<CspHsmInfoEntity> hsmInfoEntityList = hsmInfoService.getByGroupId(hsmGroupEntity.getHsmGroupId());
	                    if (hsmInfoEntityList != null && !hsmInfoEntityList.isEmpty()) {
	                        for (int j = 0, size = hsmInfoEntityList.size(); j < size; j++) {
	                            CspHsmInfoEntity hsmInfoEntity = hsmInfoEntityList.get(j);
	                            if (!hsmGroupEntity.getHsmGroupId().equals(hsmInfoEntity.getHsmGroupId())) {
	                                continue;
	                            }
	                            if (!hsmInfoEntity.isEnable()) {
	                                logger.error("Hsm ip[{}] not enabled!!!", hsmInfoEntity.getHsmIp());
	                                continue;
	                            }

	                            Long hsmId = hsmInfoEntity.getHsmId();
	                            String hsmType = hsmInfoEntity.getHsmType().getHsmType();
	                            String hsmIp = hsmInfoEntity.getHsmIp();
	                            int hsmPort = hsmInfoEntity.getHsmPort().intValue();
	                            int connectTimeout = hsmGroupEntity.getConnectTimeout();
	                            int serviceTimeout = hsmGroupEntity.getConnectTimeout();
	                            String hsmContextPath = StringUtils.isEmpty(hsmInfoEntity.getHsmContextPath())?"":hsmInfoEntity.getHsmContextPath();
	                            if(hsmContextPath.length()>0 && !hsmContextPath.startsWith("/")) {
	                            	hsmContextPath = "/" + hsmContextPath;
	                            }
	                            
	                            String url = "http://"+hsmIp+":"+hsmPort+hsmContextPath;
	                            HttpCommService http = new HttpCommService();
								logger.info(hsmGroupEntity.getHsmGroupId()+":setHttpURL="+url);
								http.setHttpURL(url);
								http.isRight = true;
								group.addHttpCommService(http);

	                            logger.debug("==========groups[{}]==========", hsmGroupEntity.getHsmGroupId());
	                            logger.debug("Hsm Id: {}", hsmId);
	                            logger.debug("Hsm Type: {}", hsmType);
	                            logger.debug("Hsm Ip: {}", hsmIp);
	                            logger.debug("Hsm Port: {}", hsmPort);
	                            logger.debug("Hsm ContextPath: {}", hsmContextPath);
	                            logger.debug("Hsm connectTimeout: {}", connectTimeout);
	                            logger.debug("Hsm serviceTimeout: {}", serviceTimeout);
	                            logger.debug("==========groups[{}]==========", hsmGroupEntity.getHsmGroupId());
	                        }
	                        
	                        group.count = 0;//从零开始轮询
	                        httpCommPool.addResoureGroup(String.valueOf(hsmGroupEntity.getHsmGroupId()), group);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            logger.error("", e);
	        } finally {
	            logger.debug("==========Init hsm http finish");
	        }
			
			
			
			// 初始化签名验签服务器http资源
			try {
	            logger.debug("==========Init hsm http resources");
	            if (hsmGroupService == null || hsmInfoService == null) {
	                logger.error("hsmGroupService and hsmInfoService can't be null");
	                return;
	            }

	            List<CspHsmGroupEntity> hsmGroupEntityList = hsmGroupService.getListByCategory(HsmCategory.CATEGORY03.getCode());
	            if (hsmGroupEntityList != null && !hsmGroupEntityList.isEmpty()) {
	                int poolSize = 100;
	                for (int i = 0, length = hsmGroupEntityList.size(); i < length; i++) {
	                    CspHsmGroupEntity hsmGroupEntity = hsmGroupEntityList.get(i);
	                    if (hsmGroupEntity.getConnectPoolSize() > poolSize) {
	                        poolSize = hsmGroupEntity.getConnectPoolSize();
	                    }
	                    
	                    HttpResouceGroup group = new HttpResouceGroup();
	    				group.setGroupID(String.valueOf(hsmGroupEntity.getHsmGroupId()));
	    				group.setContentType("application/x-www-form-urlencoded");
	    				group.setConnectTimeout(hsmGroupEntity.getConnectTimeout());
	    				group.setSoTimeout(hsmGroupEntity.getConnectTimeout());
	    				group.setMAX_REQ(poolSize);
	    				group.setHeartInteval(hsmGroupEntity.getConnectTimeout());
	    				group.setLb_strategy(CspConstants.HTTP_LB_STRATEGY_ROUNDROBIN);
	    				group.setBalance(true);
	    				group.setUseProxyAuthor("FALSE");
	                    
	                    List<CspHsmInfoEntity> hsmInfoEntityList = hsmInfoService.getByGroupId(hsmGroupEntity.getHsmGroupId());
	                    if (hsmInfoEntityList != null && !hsmInfoEntityList.isEmpty()) {
	                        for (int j = 0, size = hsmInfoEntityList.size(); j < size; j++) {
	                            CspHsmInfoEntity hsmInfoEntity = hsmInfoEntityList.get(j);
	                            if (!hsmGroupEntity.getHsmGroupId().equals(hsmInfoEntity.getHsmGroupId())) {
	                                continue;
	                            }
	                            if (!hsmInfoEntity.isEnable()) {
	                                logger.error("Hsm ip[{}] not enabled!!!", hsmInfoEntity.getHsmIp());
	                                continue;
	                            }

	                            Long hsmId = hsmInfoEntity.getHsmId();
	                            String hsmType = hsmInfoEntity.getHsmType().getHsmType();
	                            String hsmIp = hsmInfoEntity.getHsmIp();
	                            int hsmPort = hsmInfoEntity.getHsmPort().intValue();
	                            int connectTimeout = hsmGroupEntity.getConnectTimeout();
	                            int serviceTimeout = hsmGroupEntity.getConnectTimeout();
	                            String hsmContextPath = StringUtils.isEmpty(hsmInfoEntity.getHsmContextPath())?"":hsmInfoEntity.getHsmContextPath();
	                            if(hsmContextPath.length()>0 && !hsmContextPath.startsWith("/")) {
	                            	hsmContextPath = "/" + hsmContextPath;
	                            }
	                            
	                            String url = "http://"+hsmIp+":"+hsmPort+hsmContextPath;
	                            HttpCommService http = new HttpCommService();
								logger.info(hsmGroupEntity.getHsmGroupId()+":setHttpURL="+url);
								http.setHttpURL(url);
								http.isRight = true;
								group.addHttpCommService(http);

	                            logger.debug("==========groups[{}]==========", hsmGroupEntity.getHsmGroupId());
	                            logger.debug("Hsm Id: {}", hsmId);
	                            logger.debug("Hsm Type: {}", hsmType);
	                            logger.debug("Hsm Ip: {}", hsmIp);
	                            logger.debug("Hsm Port: {}", hsmPort);
	                            logger.debug("Hsm ContextPath: {}", hsmContextPath);
	                            logger.debug("Hsm connectTimeout: {}", connectTimeout);
	                            logger.debug("Hsm serviceTimeout: {}", serviceTimeout);
	                            logger.debug("==========groups[{}]==========", hsmGroupEntity.getHsmGroupId());
	                        }
	                        
	                        group.count = 0;//从零开始轮询
	                        httpCommPool.addResoureGroup(String.valueOf(hsmGroupEntity.getHsmGroupId()), group);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            logger.error("", e);
	        } finally {
	            logger.debug("==========Init hsm http finish");
	        }
						
		} else {
			XmlReader xmlReader = null;
			try {
				logger.debug("*******************http资源分组设置*******************");
				logger.debug("***********系统自动从配置文件serviceapp.xml获取***********");
				// 配置文件获取
				String fileDir = new ApplicationHome(getClass()).getSource().getParent();
				xmlReader = new XmlReader(fileDir + "/serviceapp.xml");//Local Junit Test:src/main/resources/serviceapp.xml
				XmlReadView httpCommPoolNode = xmlReader.getReadView("HttpCommPool");
				while (httpCommPoolNode.next()) {
					XmlReadView httpResouceGroupNode = httpCommPoolNode.getReadView("HttpResouceGroup");
					while (httpResouceGroupNode.next()) {
						String id = httpResouceGroupNode.getAttrValue("id");
						String reqMethod = httpResouceGroupNode.getAttrValue("reqMethod");
						String connectTimeout = httpResouceGroupNode.getAttrValue("connectTimeout");
						String soTimeout = httpResouceGroupNode.getAttrValue("soTimeout");
						String size = httpResouceGroupNode.getAttrValue("size");
						String heartInteval = httpResouceGroupNode.getAttrValue("heartInteval");
						String balance = httpResouceGroupNode.getAttrValue("balance");
						String useProxyAuthor = httpResouceGroupNode.getAttrValue("useProxyAuthor");
						String reqProxyIP = httpResouceGroupNode.getAttrValue("reqProxyIP");
						String reqProxyPort = httpResouceGroupNode.getAttrValue("reqProxyPort");
						String proxyUserName = httpResouceGroupNode.getAttrValue("proxyUserName");
						String proxyUserPass = httpResouceGroupNode.getAttrValue("proxyUserPass");
						HttpResouceGroup group = new HttpResouceGroup();
						group.setGroupID(id);
						group.setReqMethod(reqMethod);
						group.setConnectTimeout(Integer.parseInt(connectTimeout));
						group.setSoTimeout(Integer.parseInt(soTimeout));
						group.setMAX_REQ(Integer.parseInt(size));
						group.setHeartInteval(Integer.parseInt(heartInteval));
						group.setBalance(Boolean.parseBoolean(balance));
						group.setUseProxyAuthor(useProxyAuthor);
						group.setReqProxyIP(reqProxyIP);
						if(StringUtils.isNotEmpty(reqProxyPort)) {
							try {
								group.setReqProxyPort(Integer.parseInt(reqProxyPort));
							} catch(NumberFormatException e) {
								e.printStackTrace();
							}
						}
						group.setProxyUserName(proxyUserName);
						group.setProxyUserPass(proxyUserPass);
						
						String sys_code = httpResouceGroupNode.getAttrValue("sys_code");
						String app_name = httpResouceGroupNode.getAttrValue("app_name");
						String mp_code = httpResouceGroupNode.getAttrValue("mp_code");
						String sec_version = httpResouceGroupNode.getAttrValue("sec_version");
						String deskey = httpResouceGroupNode.getAttrValue("deskey");
						String deskeyiv = httpResouceGroupNode.getAttrValue("deskeyiv");
						String aeskey = httpResouceGroupNode.getAttrValue("aeskey");
						group.setSys_code(sys_code);
						group.setApp_name(app_name);
						group.setMp_code(mp_code);
						group.setSec_version(sec_version);
						group.setDeskey(deskey);
						group.setDeskeyiv(deskeyiv);
						group.setAeskey(aeskey);
						String contentType = httpResouceGroupNode.getAttrValue("contentType");
						if(StringUtils.hasText(contentType)) {
							group.setContentType(contentType);
						}
	
						XmlReadView httpResourceNode = httpResouceGroupNode.getReadView("HttpResource");
						if (httpResourceNode.getElementCount() == 0)
							throw new Exception("errcode=SE05;serviceapp.xml 文件中:" + id + " 通讯标示下没定义通讯信息！");
						while (httpResourceNode.next()) {
							HttpCommService http = new HttpCommService();
							http.setHttpURL(httpResourceNode.getAttrValue("dest"));
							group.addHttpCommService(http);
							logger.info(id + ":setHttpURL=" + http.getHttpURL());
						}
						httpCommPool.addResoureGroup(id, group);
					}
				}
				logger.info("end initHttpCommPool...");
			} catch(Exception e) {
				System.out.println("*******************初始化Http连接池失败************************");
				System.out.println("*														 *");
				System.out.println(e.getMessage());
				System.out.println("*														 *");
				System.out.println("*******************初始化Http连接池失败************************");
				e.printStackTrace();
			}
		}
	}
}
