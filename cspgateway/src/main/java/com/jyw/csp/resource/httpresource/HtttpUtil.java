package com.jyw.csp.resource.httpresource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jyw.csp.util.Base64;
import com.jyw.csp.util.log.LogUtil;


public class HtttpUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtttpUtil.class);
	public static String httpPostFrom(String urlStr, Map<String, String> textMap, Map<String, String> fileMap) {
		String res = "";
		HttpURLConnection conn = null;
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			OutputStream out = new DataOutputStream(conn.getOutputStream());
			// text
			if (textMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator iter = textMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			// file
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					String contentType = "image/jpeg";//默认jpg
					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + inputName + "\"\r\n");
					strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
					out.write(strBuf.toString().getBytes());
					
		     		byte[] bytes = Base64.decode(inputValue);
//					DataInputStream in = new DataInputStream(new FileInputStream(file));
//					int bytes = 0;
//					byte[] bufferOut = new byte[1024];
//					while ((bytes = in.read(bufferOut)) != -1) {
//						out.write(bufferOut, 0, bytes);
//					}
//					in.close();
		     		out.write(bytes);
				}
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();
			// 读取返回数据
			LOGGER.debug("======"+conn.getResponseCode());
			LOGGER.debug("======"+conn.getResponseMessage());
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = null;
			if(conn.getResponseCode()!=200){
				reader=new BufferedReader(new InputStreamReader(conn.getErrorStream()));	
			}else{
				reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;
		} catch (Exception e) {
			//LogUtil.error("发送POST请求出错。" + urlStr);
			LOGGER.error("发送POST请求出错。" + urlStr);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}
	public static String httpGetFrom(String urlStr, Map<String, String> textMap, Map<String, String> fileMap) {
		String res = "";
		HttpURLConnection conn = null;
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			OutputStream out = new DataOutputStream(conn.getOutputStream());
			// text
			if (textMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator iter = textMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			// file
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					String contentType = "image/jpeg";//默认jpg
					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + inputName + "\"\r\n");
					strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
					out.write(strBuf.toString().getBytes());
		     		byte[] bytes = Base64.decode(inputValue);
//					DataInputStream in = new DataInputStream(new FileInputStream(file));
//					int bytes = 0;
//					byte[] bufferOut = new byte[1024];
//					while ((bytes = in.read(bufferOut)) != -1) {
//						out.write(bufferOut, 0, bytes);
//					}
//					in.close();
		     		out.write(bytes);
				}
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();
			// 读取返回数据
			LOGGER.debug("======"+conn.getResponseCode());
			LOGGER.debug("======"+conn.getResponseMessage());
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = null;
			if(conn.getResponseCode()!=200){
				reader=new BufferedReader(new InputStreamReader(conn.getErrorStream()));	
			}else{
				reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;
		} catch (Exception e) {
			//LogUtil.error("发送POST请求出错。" + urlStr);
			LOGGER.error("发送POST请求出错。" + urlStr);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return res;
	}
	public static String httpPostFrom(String urlStr, String msg,int connectTimeout1, int soTimeout1, Object dataColl,String path) {
		Map<String, String> textMap=null;
		Map<String, String> fileMap=null;
		if(dataColl != null && dataColl instanceof List){
			List objList = (ArrayList)dataColl;
			textMap = (Map<String, String>) objList.get(0);
			fileMap =  (Map<String, String>)objList.get(1);
		}
		
		if(path!=null){
			urlStr=urlStr+path;
		}
		LogUtil.debug("sendurl:"+urlStr);
		String res = "";
		HttpURLConnection conn = null;
		String BOUNDARY = "---------------------------123821742118716"; // boundary就是request头和上传文件内容的分隔符
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout1);
			conn.setReadTimeout(soTimeout1);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			OutputStream out = new DataOutputStream(conn.getOutputStream());
			// text
			if (textMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator iter = textMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			// file
			if (fileMap != null) {
				Iterator iter = fileMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String inputName = (String) entry.getKey();
					String inputValue = (String) entry.getValue();
					if (inputValue == null) {
						continue;
					}
					String contentType = "image/jpeg";//默认jpg
					StringBuffer strBuf = new StringBuffer();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + inputName + "\"\r\n");
					strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
					out.write(strBuf.toString().getBytes());
		     		byte[] bytes = Base64.decode(inputValue);
		     		out.write(bytes);
				}
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();
			//读取返回数据
			StringBuffer strBuf = new StringBuffer();
			BufferedReader reader = null;
			if(conn.getResponseCode()==200){
				reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}else if(conn.getResponseCode()==400){
				reader=new BufferedReader(new InputStreamReader(conn.getErrorStream()));	
			}
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;	
		} catch (Exception e) {
			//LogUtil.error("发送POST请求出错。" + urlStr);
			LOGGER.error("发送POST请求出错。" + urlStr);
			//e.printStackTrace();
//			try {
//				//isRight = false;//记录当前连接不可用
//				LogUtil.info("HttpResouceGroup :" + urlStr+" set isRight:"+false);
//				if (reourceCfg.balance) {
//					reourceCfg.start();
//				}
//			} catch (Exception ee) {
//			}
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
//			synchronized (this) {
//				this.curConn--;
//			}
		}
		return res;
	}
	
	public static String postJson(String urlStr, String msg) {
		String result="";
        try {
        	URL url = new URL(urlStr);
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 使用 URL 连接进行输出，则将 DoOutput标志设置为 true  
			conn.setDoOutput(true);  
			conn.setRequestMethod("POST");  
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			// 向服务端发送key = value对
			out.write(msg);
			out.flush();
			out.close();
			// 如果请求响应码是200，则表示成功  
			LOGGER.debug("conn=="+conn);
			if (conn.getResponseCode() == 200) {  
			    // HTTP服务端返回的编码是UTF-8,故必须设置为UTF-8,保持编码统一,否则会出现中文乱码  
			    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    result = in.readLine();  
			    in.close();  
			}  
			conn.disconnect();// 断开连接  
		} catch (Exception e) {
			//LogUtil.error("发送POST请求出错。" + urlStr);
			LOGGER.error("发送POST请求出错。" + urlStr);
			e.printStackTrace();
//			try {
//				isRight = false;// 记录当前连接不可用
//				LogUtil.info("HttpResouceGroup :" + urlStr + " set isRight:" + false);
//				if (reourceCfg.balance) {
//					reourceCfg.start();
//				}
//			} catch (Exception ee) {
//			}
		}
        return result;
    }
}
