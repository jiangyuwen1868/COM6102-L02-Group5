package com.jyw.csp.resource.http;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.system.ApplicationHome;

import com.jyw.csp.resource.httpresource.HttpResouceGroup;
import com.jyw.csp.resource.httpresource.ReceiveMsg;
import com.jyw.csp.util.file.FileUtils;
import com.jyw.csp.util.log.LogUtil;
import com.jyw.csp.util.string.StringUtils;

public class HttpCommService {
	
private String reqMethod = "POST";
	
	private String contentType = "application/json;charset=UTF-8";
	
	private String httpURL = null;
	
	private static AtomicInteger curConn = new AtomicInteger(0);
	
	public int getCurConn() {
		return curConn.get();
	}
	public int getCurConnAndIncrement() {
		return curConn.getAndIncrement();
	}
	
	public int decrementCurConnAndGet() {
		return curConn.decrementAndGet();
	}

	public String sendAndWait(String httpURL, String path, String msg,
			int connectTimeout, int soTimeout)
			throws Exception {
		String systxcode = getSystxcode(msg, path);
		
		//判断交易挡板文件是否存在，若果存在直接返回挡板文件中的内容，并替换挡板文件中的变量表达式
		String fileDir = new ApplicationHome(getClass()).getSource().getParent();
//		String fileDir = new File(HttpCommPoolService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
		String dummyResFilePath = fileDir + "/dummyRes/" + systxcode + "-out.json";
		LogUtil.debug("dummyResFilePath:" + dummyResFilePath);
		File file = new File(dummyResFilePath);
		if(file.exists()){
			return FileUtils.readFile(dummyResFilePath);
		} else {
			return sendAndWait(path, msg, connectTimeout, soTimeout);
		}
	}
	
	public String sendAndWait(String httpURL,String msg)
			throws Exception {
		return sendAndWait(httpURL,"", msg,-1,-1);
	}
	
	public String sendAndWait(String httpURL,String path,String msg)
			throws Exception {
		return sendAndWait(httpURL,path,msg,-1,-1);
	}
	
	
	private String sendAndWait(String path, String msg,
			int connectTimeout, int soTimeout)
			throws Exception {

		String url = this.httpURL + path;
		StringBuffer tmpbuf = new StringBuffer("begin:" + url);
		if (msg == null)
			msg = "";

		URLConnection reqConnection = null;

		try {
			if (this.reqMethod == null)
				this.reqMethod = "POST";

			if (reqMethod.equalsIgnoreCase("GET") && msg.trim().length() != 0) {
				if (url.indexOf("?") == -1) {
					url = url + "?" + msg;
				} else {
					url = url + "&" + msg;
				}
				msg = "";
			}
			try {
				URL reqUrl = new URL(url);
				
				// http请求
				if (reqUrl.getProtocol().toLowerCase().equals("http")) {
					reqConnection = reqUrl.openConnection();
				} else {
					// https请求
					trustAllHosts();
					reqUrl = new URL(null, url, new sun.net.www.protocol.https.Handler());
					HttpsURLConnection https = (HttpsURLConnection) reqUrl
							.openConnection();
					https.setHostnameVerifier(DO_NOT_VERIFY);// 设置验证
					reqConnection = https;
				}
				reqConnection.setRequestProperty("Content-Type",
						contentType);
				((java.net.HttpURLConnection) reqConnection)
						.setRequestMethod(reqMethod);
				reqConnection.setRequestProperty("User-Agent",
						"ECC-CTP-SERVICE");
				
				int contentLength = 0;
				if (msg != null) {
					contentLength = msg.getBytes().length;
				}
				reqConnection.setRequestProperty("Content-Length", ""
						+ contentLength);
				reqConnection.setDoInput(true);
				reqConnection.setDoOutput(true);
				reqConnection.setUseCaches(false);
				//reqConnection.setConnectTimeout(connectTimeout);
				//reqConnection.setReadTimeout(connectTimeout);

				ReceiveMsg rec = new ReceiveMsg(reqConnection, msg);
				tmpbuf.append(" #ReceiveMsg-"
						+ connectTimeout);
				rec.beginRun(connectTimeout);
				tmpbuf.append(" #ReceiveMsg_end");
				String ret = rec.getMessage();
				return ret;
			} catch (Exception ei) {
				ei.printStackTrace();
				throw ei;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			synchronized (this) {
				this.decrementCurConnAndGet();
			}
			if (tmpbuf != null) {
				System.err
						.println("in HttpCommService.sendAndWait,debug message:"
								+ new String(tmpbuf));
			}
		}
	}
	
	/**
	 * 信任所有主机-对于任何证书都不做检查
	 * 
	 * @throws Exception
	 */
	protected static void trustAllHosts() throws Exception {
		// Create a trust manager that does not validate certificate chains
		// Android 采用X509的证书信息机制
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	}
	
	/**
	 * https 请求必须实现的 安全验证类
	 * 
	 */
	protected final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
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
