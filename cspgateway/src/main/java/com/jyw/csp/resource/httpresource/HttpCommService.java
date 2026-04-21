package com.jyw.csp.resource.httpresource;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.jyw.csp.util.log.LogUtil;

public class HttpCommService {
	private String reqProxyIP = null;

	private int reqProxyPort = -1;

	private String reqMethod = "POST";
	
	private String contentType = "application/json;charset=UTF-8";
	
	private String path;

	private String useProxyAuthor = null;

	private String proxyUserName = null;

	private String proxyUserPass = null;

	public String httpURL = null;
	
	private int weight = 0; //权重

	public AtomicInteger curConn = new AtomicInteger(0);

	public boolean isRight = true;

	public HttpCommService() {
	}
	
	public int getCurConn() {
		return curConn.get();
	}
	public int getCurConnAndIncrement() {
		return curConn.getAndIncrement();
	}
	
	public int decrementCurConnAndGet() {
		return curConn.decrementAndGet();
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}

	private String getHttpHead(String requestURL, int contentLength) {
		StringBuffer reqData = new StringBuffer();
		int index = requestURL.indexOf("/");
		String path = requestURL.trim().substring(index + 2,
				requestURL.length());
		String hostName = path.substring(0, path.indexOf("/"));

		reqData.append(reqMethod + " " + requestURL + " HTTP/1.1\r\n");
		reqData.append("Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-powerpoint, application/vnd.ms-excel, application/msword, */*\r\n");
		reqData.append("Accept-Language: zh-cn\r\n");
		reqData.append("Accept-Encoding: gzip, deflate\r\n");
		reqData.append("User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n");
		reqData.append("Connection: Keep-Alive\r\n");
		reqData.append("Host: " + hostName + "\r\n");
		reqData.append("Content-Length: " + contentLength + "\r\n");
		reqData.append("content-type: application/x-www-form-urlencoded;charset=utf-8\r\n");
		if (useProxyAuthor != null
				&& useProxyAuthor.trim().equalsIgnoreCase("true")) {
			String userClonPass = proxyUserName.trim() + ":"
					+ proxyUserPass.trim();
			byte[] bUserClonPass = userClonPass.getBytes();
			// Base64 base64 = new Base64();
			String userClonPassb4 = new String(bUserClonPass);
			reqData.append("Proxy-Connection: Keep-Alive\r\n");
			reqData.append("Proxy-Authorization: Basic " + userClonPassb4
					+ "\r\n\r\n");
		} else {
			reqData.append("Proxy-Connection: Keep-Alive\r\n\r\n");
		}
		return reqData.toString();
	}

	public String inform(String commURL, String msg) throws Exception {
		if ("GET".equals(this.reqMethod)) {
			if (commURL.indexOf("?") < 0)
				commURL = commURL + "?";
			else if (msg != null)
				commURL = commURL + "&";

			if (msg != null)
				commURL = commURL + msg;

			msg = "";
		}
		String message = comm(commURL, msg);
		return message;
	}

	public String comm(String commURL, String msg) throws Exception {
		HttpURLConnection urlConnection = null;
		OutputStream out = null;
		DataInputStream in = null;
		try {
			URL aURL = new URL(commURL);

			urlConnection = (HttpURLConnection) aURL.openConnection();

			urlConnection.setRequestMethod(this.reqMethod);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("User-Agent", "MSIE");

			urlConnection.connect();
			if ((msg != null) && (msg.trim().length() != 0)) {
				out = urlConnection.getOutputStream();
				out.write(msg.getBytes());
			}

			int resCode = urlConnection.getResponseCode();
			// System.out.println("SystemDataSyn:" + resCode + "|" + commURL +
			// "|" + msg);

			if (resCode != 200) {
				throw new Exception(
						"HttpCommService.inform() Can't find page to inform"
								+ commURL);
			}

			int contentLen = urlConnection.getContentLength();

			in = new DataInputStream(
					urlConnection.getInputStream());

			if (contentLen <= 0)
				contentLen = 10240;
			else if (contentLen > 10240)
				contentLen = 10240;

			byte[] buffer = new byte[contentLen];
			int off = 0;
			int len = 0;
			do {
				len = in.read(buffer, off, contentLen - off);
				if (len == -1)
					break;
				if (len == 0)
					break;

				off += len;
			} while (off < contentLen);

			urlConnection.disconnect();
			urlConnection = null;
			return new String(buffer, 0, off);
		} catch (Throwable e) {
			if (urlConnection != null)
				;
			throw new Exception(
					"HttpCommService.inform() inform Service error."
							+ e.getMessage());
		} finally {
			if (urlConnection != null) {
				try {
					urlConnection.disconnect();
					urlConnection = null;
				} catch (Exception ee) {
				}
			}
			if(out!=null) {
				try {
					out.close();
				} catch (Exception ee) {
				}
			}
			if(in!=null) {
				try {
					in.close();
				} catch (Exception ee) {
				}
			}
		}
	}

	public String sendAndWait(HttpResouceGroup reourceCfg, String path, String msg,
			int connectTimeout1, int soTimeout1, Object dataColl)
			throws Exception {

		String url = this.httpURL + path;
		StringBuffer tmpbuf = new StringBuffer("begin:" + url);
		if (msg == null)
			msg = "";

		URLConnection reqConnection = null;

		try {
			if(reourceCfg.useProxyAuthor!=null && 
					reourceCfg.useProxyAuthor.trim().equalsIgnoreCase("true")) {
				if (reourceCfg.reqProxyIP != null
						&& reourceCfg.reqProxyIP.trim().length() != 0) {
					LogUtil.debug(String.format("In HttpCommService----sendAndWait get reqProxyIP[%s] reqProxyPort[%s]", reourceCfg.reqProxyIP, reourceCfg.reqProxyPort));
					System.getProperties().put("proxySet", "true");
					System.getProperties().put("proxyHost", reourceCfg.reqProxyIP);
					System.getProperties().put("proxyPort",
							"" + reourceCfg.reqProxyPort);
					
					System.getProperties().put("http.proxySet", "true");
					System.getProperties().put("http.proxyHost", reourceCfg.reqProxyIP);
					System.getProperties().put("http.proxyPort",
							"" + reourceCfg.reqProxyPort);
				}
			}
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
						reourceCfg.contentType);
				((java.net.HttpURLConnection) reqConnection)
						.setRequestMethod(reqMethod);
				reqConnection.setRequestProperty("User-Agent",
						"ECC-CTP-SERVICE");
				if (reourceCfg.proxyUserName != null
						&& reourceCfg.proxyUserName.trim().length() != 0) {
					reqConnection
							.setRequestProperty(
									"Proxy-Authorization",
									new String(
											(reourceCfg.proxyUserName + ":" + reourceCfg.proxyUserPass)
													.getBytes()));
					reqConnection.setRequestProperty("Proxy-Connection",
							"Keep-Alive");
				}
				int contentLength = 0;
				if (msg != null) {
					contentLength = msg.getBytes().length;
				}
				reqConnection.setRequestProperty("Content-Length", ""
						+ contentLength);
				reqConnection.setDoInput(true);
				reqConnection.setDoOutput(true);
				reqConnection.setUseCaches(false);
//				reqConnection.setConnectTimeout((connectTimeout1 == -1 ? reourceCfg.connectTimeout
//								: connectTimeout1));
//				reqConnection.setReadTimeout((soTimeout1 == -1 ? reourceCfg.soTimeout
//								: soTimeout1));

				ReceiveMsg rec = new ReceiveMsg(reqConnection, msg);
				tmpbuf.append(" #ReceiveMsg-"
						+ (soTimeout1 == -1 ? reourceCfg.soTimeout
								: soTimeout1));
				
				rec.beginRun((connectTimeout1 == -1 ? reourceCfg.connectTimeout
						: connectTimeout1));
				tmpbuf.append(" #ReceiveMsg_end");
				String ret = rec.getMessage();
				tmpbuf = null;
				return ret;
			} catch (Exception ei) {
				ei.printStackTrace();
				try {
					if (reourceCfg.balance) {
						reourceCfg.start();
						isRight = false;//记录当前连接不可用
					}
				} catch (Exception ee) {
				}
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

	public String informByProxy(String commURL, String msg) throws Exception {
		String message = null;

		if ("GET".equals(this.reqMethod)) {
			if (commURL.indexOf("?") < 0)
				commURL = commURL + "?";

			if (msg != null)
				commURL = commURL + msg;

			msg = "";
		}
		message = commProxy(commURL, msg);

		return message;
	}

	public String commProxy(String commURL, String msg) throws Exception {
		Socket socket;
		try {
			socket = new Socket(this.reqProxyIP, this.reqProxyPort);

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			String reqData = getHttpHead(commURL, msg.getBytes().length) + msg
					+ "\r\n";

			out.write(reqData.getBytes());
			ReceiveMsg rm = new ReceiveMsg(in);
			String recMsg = rm.receive();
			socket.close();
			return recMsg;
		} catch (Exception e) {
			throw new Exception(
					"HttpCommService.informByProxy() informByProxy Service error.");
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

	public String getHttpURL() {
		return httpURL;
	}

	public void setHttpURL(String httpURL) {
		this.httpURL = httpURL;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}