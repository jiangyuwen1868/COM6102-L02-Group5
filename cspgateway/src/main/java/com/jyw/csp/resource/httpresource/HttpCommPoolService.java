package com.jyw.csp.resource.httpresource;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.springframework.boot.system.ApplicationHome;

import com.jyw.csp.util.file.FileUtils;
import com.jyw.csp.util.log.LogUtil;
import com.jyw.csp.util.string.StringUtils;

public class HttpCommPoolService {
	private CTEConstance constance = null;
	private String name = "";
	private Hashtable<String,HttpResouceGroup> resourceGroup = new Hashtable<>(1);
	
	public static HttpCommPoolService HTTPCommPool = null;

	public HttpCommPoolService() {
		super();
		constance = new CTEConstance();
		HTTPCommPool = this;
	}

	public void addResoureGroup(String sid, HttpResouceGroup group) {
		if (sid != null && group != null) {
			resourceGroup.put(sid, group);
		}
	}

	public Hashtable<String,HttpResouceGroup> getResourceGroup() {
		return resourceGroup;
	}
	
	public HttpResouceGroup getHttpResouceGroup(String groupId) {
		return (HttpResouceGroup) resourceGroup.get(groupId);
	}
	
	public void clearResourceGroup() {
		if(resourceGroup!=null) {
			resourceGroup.clear();
		}
	}
	
	public HttpCommPoolService(String name) throws Exception {
		constance = new CTEConstance(name);
		HTTPCommPool = this;
	}

	public Vector<?> getHeadParams(String httpResource) throws Exception {
		HttpResouceGroup srv = (HttpResouceGroup) resourceGroup
				.get(httpResource);
		if (srv == null) {
			throw new Exception("in http comm pool " + this.name
					+ ",Http Service " + httpResource + " no defined!");
		}
		return srv.getHeadParams();
	}

	public String sendAndWait(String httpResource, String path, String msg,
			int connectTimeout, int soTimeout, Object dataColl)
			throws Exception {
		if (resourceGroup.size() == 0) {
			throw new Exception("not resourceGroup,all poolService not available");
		}
		String systxcode = getSystxcode(msg, path);
		constance.opParallelLock(systxcode);
		HttpResouceGroup srv = (HttpResouceGroup) resourceGroup
				.get(httpResource);
		if (srv == null) {
			constance.opParallelUnlock(systxcode);
			throw new Exception("in http comm pool " + this.name
					+ ",Http Service " + httpResource + " no defined!");
		}
		constance.opParallelUnlock(systxcode);
		
		//判断交易挡板文件是否存在，若果存在直接返回挡板文件中的内容，并替换挡板文件中的变量表达式
		String fileDir = new ApplicationHome(getClass()).getSource().getParent();
//		String fileDir = new File(HttpCommPoolService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
		String dummyResFilePath = fileDir + "/dummyRes/" + systxcode + "-out.json";
		LogUtil.debug("dummyResFilePath:" + dummyResFilePath);
		File file = new File(dummyResFilePath);
		if(file.exists()){
			return FileUtils.readFile(dummyResFilePath);
		}else{
			return srv.sendAndWait(path, msg, connectTimeout, soTimeout, dataColl);
		}
	}
	
	public String sendAndWait(String groupId,String msg)
			throws Exception {
		return sendAndWait(groupId,"", msg,-1,-1,null);
	}
	
	public String sendAndWait(String groupId,String path,String msg)
			throws Exception {
		return sendAndWait(groupId,path,msg,-1,-1,null);
	}
	/**
	 * http通讯，默认资源分组为01
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public String sendAndWait(String msg)
			throws Exception {
		return sendAndWait("01","",msg,-1,-1,null);
	}
	public void terminate() throws Exception {
		Enumeration e;
		if (this.resourceGroup != null) {
			e = this.resourceGroup.keys();
			if (e.hasMoreElements()) {
				try {
					HttpResouceGroup group = (HttpResouceGroup) this.resourceGroup
							.get(e.nextElement());
					group.terminate();
				} catch (Exception ee) {
				}
			}
		}

		HTTPCommPool = null;
	}
	
	public String getSystxcode(String indata,String path) throws Exception {
		String SYS_TX_CODE = "SYS_TX_CODE";
		String bngStr = "<" + SYS_TX_CODE + ">";
		String endStr = "</" + SYS_TX_CODE + ">";

		int bngIdx = indata.indexOf(bngStr);
		int endIdx = indata.indexOf(endStr);
		
		String sys_tx_code = "Http";
		if(!StringUtils.checkEmpty(path)) {
			sys_tx_code = path;
		}
		if (bngIdx == -1 || endIdx == -1) {
			//SYS_TX_CODE=EMV070001&para1=value1
			
			StringTokenizer st = new StringTokenizer(indata,"&");
			while(st.hasMoreTokens()){
				String tmp = st.nextToken();
				bngIdx = tmp.indexOf(SYS_TX_CODE+"=");
				if(bngIdx !=-1){
					return tmp.substring(bngIdx+12,tmp.length());
				}
			}
			return sys_tx_code;
		}

		
		bngIdx = bngIdx + bngStr.length();
		if (endIdx >= bngIdx) {
			sys_tx_code = indata.substring(bngIdx, endIdx);
		}
		return sys_tx_code;
	}
}