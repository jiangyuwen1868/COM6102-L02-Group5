package com.jyw.csp.util;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMessage {
	
	private final static Logger logger = LoggerFactory.getLogger(LocalMessage.class);
	
	 private static String localAppPath = null;
	/**
	 * 获取系统工程路径
	 * @return 
	 */
	protected String getProAppPath()throws Exception
	{
		
		if (this.getClass().getClassLoader().getResource("/") != null)
			localAppPath = this.getClass().getClassLoader().getResource("/").getPath();
		else if (this.getClass().getResource("/") != null)
			localAppPath = this.getClass().getResource("/").getPath();
		
		if (localAppPath == null)
			throw new Exception("获取系统路径为NULL");
		
		try {
			localAppPath = java.net.URLDecoder.decode(localAppPath,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		logger.debug("get Resource localAppPath:" + localAppPath);
		
		if (localAppPath.indexOf(":/")>-1)
			localAppPath = localAppPath.substring(1);
		
		localAppPath = localAppPath.replace("WEB-INF/classes/", "");
		
		localAppPath = localAppPath.replace("target/classes/", "");
		
		return localAppPath;
	}
	/**
	 * 获取当前系统绝对路径
	 * @return
	 * @throws Exception
	 */
	public static String getAppPath()throws Exception
	{
		if (localAppPath == null)
		{
			LocalMessage localMessage = new  LocalMessage();
			return localMessage.getProAppPath();
		}else
			return localAppPath;
	}
	/**
	 * 获取当前系统IP地址集合
	 * @return
	 * @throws Exception
	 */
	public static List<String> getLocalIP()
	{
		List<String>  ipList = Collections.synchronizedList(new ArrayList<String>());
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();  
		    while (netInterfaces.hasMoreElements())
		    {  
		         NetworkInterface ni = netInterfaces.nextElement();  
		         Enumeration<InetAddress> ips = ni.getInetAddresses();  
		         while (ips.hasMoreElements())
		         {  
		             String ip = ips.nextElement().getHostAddress();
		             if (isIP(ip))
		             {
		            	 ipList.add(ip);
		             }
		        }  
		    }
		} catch(Exception e) {
			e.printStackTrace();
			ipList.add(getHostName());
		}
	    return ipList;		
	}
	
	/**
	 * 获取本地机器名称或ip地址
	 * @return
	 */
	public static String getHostName(){
    	try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try{
				List<String> ipList = LocalMessage.getLocalIP();
				for(int i=0;i<ipList.size();i++){
					if (!ipList.get(i).equals("127.0.0.1")){
						return ipList.get(i);
					}
				}
				return "localhost";
			}catch(Exception ex){
				return "localhost";
			}
		}
    }
    
	
	/**
	 * 校验ＩＰ是否正确
	 * @param ip
	 * @return
	 */
	public static boolean isIP(String ip)
	  {
		  Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		  Matcher matcher = pattern.matcher(ip); 	  
		  return matcher.matches();
		 
	  }
}