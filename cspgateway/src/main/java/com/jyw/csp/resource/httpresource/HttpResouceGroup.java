package com.jyw.csp.resource.httpresource;

import java.util.Vector;

import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.mq.msg.AlarmLevel;
import com.jyw.csp.mq.msg.AlarmMsgData;
import com.jyw.csp.mq.msg.AlarmType;
import com.jyw.csp.util.LocalMessage;
import com.jyw.csp.util.date.DateUtils;
import com.jyw.csp.util.log.LogUtil;

public class HttpResouceGroup {
	
	private String sys_code;
	private String app_name;
	private String mp_code;
	private String sec_version;
	private String deskey;
	private String deskeyiv;
	private String aeskey;
	//*************************//
	private String groupID = null;
	
	public Vector vResouce = new Vector(1);

	public String reqProxyIP = null;

	public int reqProxyPort = -1;

	public String reqMethod = "POST";
	
	public String contentType = "application/json;charset=UTF-8";
	
	public String path = "";

	public String useProxyAuthor = null;

	public String proxyUserName = null;

	public String proxyUserPass = null;

	//private boolean usingProxy = true;

	public int connectTimeout = 4000;

	public int soTimeout = 3000;
	
	public String lb_strategy = "1";

	public boolean soNodelay = true;

	Vector headParam = null;

	//轮询链接资源
	public volatile int count = 0;

	public int heartInteval = 300000;

	Thread heartThread = null;

	int MAX_REQ = 20;
	
	public boolean balance = true;

	public class HeartTest implements Runnable {
		int checkInteval = 300000;

		HttpResouceGroup group = null;

		/**
		 *  
		 */
		public HeartTest(HttpResouceGroup group1, int inteval) {
			super();
			group = group1;
			checkInteval = inteval;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			while (true) {
				try {
					Thread.sleep(checkInteval);
					if (group.heartCheck()) {
						break;
					}
				} catch (Exception ee) {
				}
			}
			try{
				heartThread.interrupt();
				heartThread = null;
			}catch(Exception ee){}
			
		}

	}

	/**
	 *  
	 */
	public HttpResouceGroup() {
		super();
	}
	
	public HttpResouceGroup(String groupID){
		this.groupID = groupID;
	}
	
	public Vector<HttpCommService> getAllResource() {
		return this.vResouce;
	}
	
	public void addHttpCommService(HttpCommService httpCommService){
		if(httpCommService!=null){
			this.vResouce.addElement(httpCommService);
		}
	}

	/**
	 * 获取http连接资源
	 * @return
	 * @throws Exception
	 */
	public synchronized HttpCommService getResouce() throws Exception {
		//负载均衡策略 1-轮询 2-权重轮询 3-权重随机
		if(CspConstants.HTTP_LB_STRATEGY_WEIGHTED_ROUNDROBIN.equals(lb_strategy)) {
			WeightedRoundRobinScheduling wrrs = new WeightedRoundRobinScheduling(vResouce);
			return wrrs.getResouce(this.MAX_REQ);
		} else if (CspConstants.HTTP_LB_STRATEGY_WEIGHTED_RANDOM.equals(lb_strategy)) {
			WeightedRandomRobinScheduling wrrs = new WeightedRandomRobinScheduling(vResouce);
			return wrrs.getResouce(this.MAX_REQ);
		}
		int cnt = 0;
		int rLen = vResouce.size();
		//System.out.println("vResouce.size()==="+rLen);
		while (true) {
			//System.out.println("count===1"+count);
			if(count >= rLen){//重新轮询
				count = 0;
			}
			HttpCommService srv = (HttpCommService) vResouce.elementAt(count++);
			cnt++;
			//System.out.println("count===2"+count);
			if (srv.isRight) {
				int curConn = srv.getCurConn();
				if (curConn < this.MAX_REQ) {
					srv.getCurConnAndIncrement();
					LogUtil.debug("Service Url:" + count+"==="+srv.httpURL);
					return srv;
				}
			}
			if (rLen == cnt) {//轮询了所有资源
				break;
			}

		}
		throw new HttpResouceException("Http comm service in group all not available!,please check network!");
	}

	public String sendAndWait(String path, String msg, int connectTimeout1, int soTimeout1,
			Object dataColl) throws Exception {
		if (connectTimeout1 == -1) {
			connectTimeout1 = this.connectTimeout;
		}
		if (soTimeout1 == -1) {
			soTimeout1 = this.soTimeout;
		}
		String outdata = null;
		
		try{
			outdata = getResouce().sendAndWait(this, path, msg, connectTimeout1, soTimeout1,dataColl);
		}catch(Exception e){
			if(e instanceof HttpResouceException){
				return outdata;
			}
		}
				
		int cnt = 0;
		//System.out.println("outdata:"+outdata + " cnt:"+cnt);
		//判断当前连接是否正常返回,否继续轮询
		while(outdata==null || outdata.length()<4){
			//System.out.println("count:"+count + " cnt:"+cnt);
			try{
				cnt++;
				if(cnt >= vResouce.size()){
					break;
				}
				outdata = getResouce().sendAndWait(this, path, msg, connectTimeout1, soTimeout1,
					dataColl);
			}catch(Exception e){
				LogUtil.error("sendAndWait:", e);
				if (cnt >= vResouce.size()) {
					throw e;
				}
			}
			
			if (cnt >= vResouce.size()) {
				break;
			}
		}
		return outdata;
	}

	public Vector getHeadParams() {
		return headParam;
	}


	public boolean heartCheck() {
		boolean isGoog = true;
		if (vResouce != null) {
			int cnt = 0;
			for (int i = 0; i < vResouce.size(); i++) {
				HttpCommService srv = (HttpCommService) vResouce.elementAt(i);
				if (!srv.isRight) {
					cnt++;
					try {
						srv.getCurConnAndIncrement();
						String checkdata = srv.sendAndWait(this, "", "{\"heartCheckMsg\":\"########heartCheck########\"}", -1, -1, null);
						if(checkdata==null){
							srv.isRight = false;
							
							AlarmMsgData alarmMsgData = new AlarmMsgData();
							alarmMsgData.setAlarmContent("网络不可达：" + srv.getHttpURL());
							alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
							alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
							alarmMsgData.setAlarmType(AlarmType.NET.getType());
							alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
							alarmMsgData.setAlarmHostDesc("cpsGateway");
							alarmMsgData.setAlarmDesc("请求响应数据为空");
//							MqProvider.sendAlarmMessage(alarmMsgData);
						}else{
							srv.isRight = true;
						}
					} catch (Exception ee) {
						isGoog = false;
						srv.isRight = false;
						//ee.printStackTrace();
						
						AlarmMsgData alarmMsgData = new AlarmMsgData();
						alarmMsgData.setAlarmContent("网络不可达：" + srv.getHttpURL());
						alarmMsgData.setAlarmTime(DateUtils.getNumberDate());
						alarmMsgData.setAlarmLevel(AlarmLevel.SERIOUS.getLevel());
						alarmMsgData.setAlarmType(AlarmType.NET.getType());
						alarmMsgData.setAlarmHostIp(LocalMessage.getLocalIP().get(0));
						alarmMsgData.setAlarmHostDesc("cpsGateway");
						alarmMsgData.setAlarmDesc(ee.getMessage());
//						MqProvider.sendAlarmMessage(alarmMsgData);
					}
				}
			}
			if(cnt>0){//还有坏的资源，继续探针
				isGoog = false;
			}
		}
		return isGoog;
	}

	public void start() {
		if (heartThread == null) {
			heartThread = new Thread(new HeartTest(this, heartInteval));
			heartThread.setName("Heart check thread for HttpResouceGroup ");
			heartThread.start();
		}
	}
	
	public void terminate() throws Exception {
	    if (this.heartThread != null)
	      try {
	        this.heartThread.interrupt();
	        this.heartThread = null;
	      }
	      catch (Exception ee)
	      {
	      }
	  }

	public String getReqProxyIP() {
		return reqProxyIP;
	}

	public void setReqProxyIP(String reqProxyIP) {
		this.reqProxyIP = reqProxyIP;
	}

	public int getReqProxyPort() {
		return reqProxyPort;
	}

	public void setReqProxyPort(int reqProxyPort) {
		this.reqProxyPort = reqProxyPort;
	}

	public String getReqMethod() {
		return reqMethod;
	}

	public void setReqMethod(String reqMethod) {
		this.reqMethod = reqMethod;
	}

	public String getUseProxyAuthor() {
		return useProxyAuthor;
	}

	public void setUseProxyAuthor(String useProxyAuthor) {
		this.useProxyAuthor = useProxyAuthor;
	}

	public String getProxyUserName() {
		return proxyUserName;
	}

	public void setProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}

	public String getProxyUserPass() {
		return proxyUserPass;
	}

	public void setProxyUserPass(String proxyUserPass) {
		this.proxyUserPass = proxyUserPass;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public boolean isSoNodelay() {
		return soNodelay;
	}

	public void setSoNodelay(boolean soNodelay) {
		this.soNodelay = soNodelay;
	}

	public Vector getHeadParam() {
		return headParam;
	}

	public void setHeadParam(Vector headParam) {
		this.headParam = headParam;
	}

	public int getHeartInteval() {
		return heartInteval;
	}

	public void setHeartInteval(int heartInteval) {
		this.heartInteval = heartInteval;
	}

	public Thread getHeartThread() {
		return heartThread;
	}

	public void setHeartThread(Thread heartThread) {
		this.heartThread = heartThread;
	}

	public int getMAX_REQ() {
		return MAX_REQ;
	}

	public void setMAX_REQ(int mAX_REQ) {
		MAX_REQ = mAX_REQ;
	}

	public boolean isBalance() {
		return balance;
	}

	public void setBalance(boolean balance) {
		this.balance = balance;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getLb_strategy() {
		return lb_strategy;
	}

	public void setLb_strategy(String lb_strategy) {
		this.lb_strategy = lb_strategy;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSys_code() {
		return sys_code;
	}

	public void setSys_code(String sys_code) {
		this.sys_code = sys_code;
	}

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getMp_code() {
		return mp_code;
	}

	public void setMp_code(String mp_code) {
		this.mp_code = mp_code;
	}

	public String getSec_version() {
		return sec_version;
	}

	public void setSec_version(String sec_version) {
		this.sec_version = sec_version;
	}

	public String getDeskey() {
		return deskey;
	}

	public void setDeskey(String deskey) {
		this.deskey = deskey;
	}

	public String getDeskeyiv() {
		return deskeyiv;
	}

	public void setDeskeyiv(String deskeyiv) {
		this.deskeyiv = deskeyiv;
	}

	public String getAeskey() {
		return aeskey;
	}

	public void setAeskey(String aeskey) {
		this.aeskey = aeskey;
	}

}