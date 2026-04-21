package com.jyw.csp.constant;

public class CspConstants {
	
	// 平台默认部署资源分组
	public final static String CSP_DEFUALT_RESOURCE_GROUPID = "csp01";
	// 本应用编号
	public final static String CSP_APPCODE = "cspGateway";
	// 平台服务地址
	public final static String CSP_SERVERPATH = "/cspGateway/mainAccess";
	// 平台服务是否可用
	public final static String CSP_DEFUALT_SERVER_ISUSEFUL = "1";
	
	/*
	 * 服务接口报文交易码节点名称
	 */
	public final static String CSP_TXCODE_NODE_NAME = "sys_txcode";
	/*
	 * 加密机指令管理交易码列表
	 */
	public final static String CSP_HSMCMD_MANAGER_TXCODES = "CSP000000,CSP000001";
	/*
	 * 时间戳交易码列表
	 */
	public final static String CSP_TS_TXCODES = "CSPTS1001,CSPTS1002,CSPTS1003,CSPTS1004,CSPTS1005,CSPTS1006,CSPTS1007";
	/*
	 *签名验签服务器交易码列表前缀 
	 */
	public final static String CSP_SVS_TXCODES_PREFIX = "CSPSVS";
	
	// 报文安全版本号01
	public final static String SEC_VERSION_01 = "01";
	public final static String SEC_VERSION_02 = "02";
	
	/*
	 * 交易环节计时器
	 */
	public final static String CSP_COSTTIME_INFO_STOPWATCH = "costtime_info";
	public final static String CSP_TX_HTTP_WATCH_NAME = "http";
	

	/*
	 * 服务交易码缓存标识
	 */
	public final static String CSP_TXCODE_CACHE_KEY = "CSP_TXCODE_CACHE";
	/*
	 * 应用信息缓存标识
	 */
	public final static String CSP_APPINFO_CACHE_KEY = "CSP_APPINFO_CACHE";
	/*
	 * 系统错误码信息缓存标识
	 */
	public final static String CSP_ERRORINFO_CACHE_KEY = "CSP_ERRORINFO_CACHE";
	/*
	 * 系统流控信息缓存标识
	 */
	public final static String CSP_FLOWCONTROL_CACHE_KEY = "CSP_FLOWCONTROL_CACHE";
	/*
	 * 系统参数配置信息缓存标识
	 */
	public final static String CSP_SYSCONFIG_CACHE_KEY = "CSP_SYSCONFIG_CACHE";
	/*
	 * 应用IP白名单信息缓存标识
	 */
	public final static String CSP_APPIPWHITELIST_CACHE_KEY = "CSP_APPIPWHITELIST_CACHE";
	/*
	 * 应用服务授权信息缓存标识
	 */
	public final static String CSP_APPSRVAUTH_CACHE_KEY = "CSP_APPSRVAUTH_CACHE";
	/*
	 * 平台初始化密钥信息缓存标识
	 */
	public final static String CSP_INITKEYS_CACHE_KEY = "CSP_INITKEYS_CACHE";
	
	// 数据库表数据签名密钥ID
	public final static String CSP_TAB_DATA_SIGN_KEYID = "tab_sign_key";
	
	//系统部署节点是否使用（使用）
	public final static String SYS_DEPLOY_USEFULLY_Y = "1";
	//系统部署节点是否使用（不使用）
	public final static String SYS_DEPLOY_USEFULLY_N = "0";
	//负载均衡策略 1-轮询 2-权重轮询 3-权重随机
	public final static String HTTP_LB_STRATEGY_ROUNDROBIN = "1";
	public final static String HTTP_LB_STRATEGY_WEIGHTED_ROUNDROBIN = "2";
	public final static String HTTP_LB_STRATEGY_WEIGHTED_RANDOM = "3";
	//分隔连接符，特殊地方使用
	public final static String SEPARATOR_CONNECTOR_A = "@";

	//错误码信息是否转译
	public final static String ERROR_CODE_ISCONV_Y = "1";
	public final static String ERROR_CODE_ISCONV_N = "0";
	
	//流控开关
	public final static String FLOWCONTROL_OPSWITCH_Y = "1";
	public final static String FLOWCONTROL_OPSWITCH_N = "0";
	
	public final static String SHA1 = "SHA1";
	//应用开通状态
	public final static String APP_STATUS_2 = "2";

	
	
	
	/*************************************************************/
	public final static String SYS_PKG_VERSION = "sys_pkg_version";
	public final static String SYS_PKG_VERSION_01 = "01";
	public final static String SYS_REQ_TIME = "sys_req_time";
	public final static String SYS_EVT_TRACE_ID = "sys_evt_trace_id";
	public final static String SYS_TX_CODE = "sys_tx_code";
	public final static String SYS_PKG_STS_TYPE = "sys_pkg_sts_type";
	public final static String SYS_PKG_STS_TYPE_00 = "00";
	public final static String TX_HEADER = "tx_header";
	public final static String ENTITY = "entity";
	public final static String ENTITY_PARAMS = "params";
	public final static String APPID = "appId";
	public final static String TENANTID = "tenantId";
	public final static String CHANNELTXCODE = "channelTxCode";
	public final static String CHANNELTXCODE_DEFUALT = "CspOpenApi";
	public final static String COM1 = "com1";
	public final static String TX_BODY = "tx_body";
	public final static String TRACEID = "traceId";
	public final static String SRVCOSTTIME = "srvCostTime";
	public final static String SUCCESS_CODE = "000000000000";
	public final static String SYS_RESP_CODE = "sys_resp_code";
	public final static String SYS_RESP_DESC = "sys_resp_desc";
	public final static String HSMRSPDATA = "hsmrspdata";
	public final static String ERRCODE = "errcode";
	public final static String SUCCESS_HSM_CODE = "00";
	
	public final static String[] CLIENTAPI_HEARTCHECK_FLAGS = {"heartCheck", "TimeTask"};
	public final static String HEARTCHECK_CODE = "XHC999999999";
	public final static String RATELIMITER_CODE = "XXX999999999";
}
