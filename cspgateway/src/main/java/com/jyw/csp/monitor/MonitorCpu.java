package com.jyw.csp.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import com.jyw.csp.util.log.LogUtil;



/**
 * 系统监控工具类
 * 
 */
public class MonitorCpu 
{

	private static final int CPUTIME = 30;

	private static final int FAULTLENGTH = 10;

	// 当前是否为linux平台
	private static boolean isLinux;

	static {
		String osName = System.getProperty("os.name");
		isLinux = osName.toLowerCase().startsWith("windows");
	}

	/**
	 * 得到cpu 空闲率
	 * @return
	 * @throws Exception
	 */
	public static Double getCpuRatio() 
	{
		Double cpuRatio = null;
				
		if (isLinux) {
			cpuRatio = getCpuRatioForWindows();
		} else {
			cpuRatio = getCpuRateForLinux();
		}

		return cpuRatio;
	}

	/**
	 * 
	 * 读取CPU信息.
	 * @param proc
	 * @return
	 * @author GuoHuang
	 */
	private static long[] readCpu(final Process proc) 
	{
		long[] retn = new long[2];
		try {
			proc.getOutputStream().close();
			InputStreamReader ir = new InputStreamReader(proc.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line = input.readLine();
			if (line == null || line.length() < FAULTLENGTH) {
				return null;
			}
			int capidx = line.indexOf("Caption");
			int cmdidx = line.indexOf("CommandLine");
			int rocidx = line.indexOf("ReadOperationCount");
			int umtidx = line.indexOf("UserModeTime");
			int kmtidx = line.indexOf("KernelModeTime");
			int wocidx = line.indexOf("WriteOperationCount");
			long idletime = 0;
			long kneltime = 0;
			long usertime = 0;
			while ((line = input.readLine()) != null) {
				try {
					if (line.length() < wocidx) {
						continue;
					}
					// 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
					// ThreadCount,UserModeTime,WriteOperation
					String caption = line.substring(capidx, cmdidx).trim();
					String cmd = line.substring(cmdidx, kmtidx - 1).trim();
					if (cmd.indexOf("wmic.exe") >= 0) {
						continue;
					}
					// log.info("line="+line);
					if (caption.equals("System Idle Process")
							|| caption.equals("System")) {
						idletime += Long.valueOf(
								line.substring(kmtidx, rocidx - 1).trim())
								.longValue();
						idletime += Long.valueOf(
								line.substring(umtidx, wocidx - 1).trim())
								.longValue();
						continue;
					}

					kneltime += Long.valueOf(
							line.substring(kmtidx, rocidx - 1).trim())
							.longValue();
					usertime += Long.valueOf(
							line.substring(umtidx, wocidx - 1).trim())
							.longValue();
				} catch (Exception e) {

				}
			}
			retn[0] = idletime;
			retn[1] = kneltime + usertime;
			return retn;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				proc.getInputStream().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获得CPU空闲率.
	 * 
	 * @return 返回CPU空闲率.
	 * @author GuoHuang
	 */
	private static double getCpuRatioForWindows() 
	{
		try {
			String procCmd = System.getenv("windir")
					+ "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,"
					+ "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
			// 取进程信息
			long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
			Thread.sleep(CPUTIME);
			long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
			if (c0 != null && c1 != null) {
				long idletime = c1[0] - c0[0];
				long busytime = c1[1] - c0[1];
				return  Math.round(Double.valueOf(100 * (idletime) / (busytime + idletime))
						.doubleValue()*100)/100;
			} else {
				return 0.0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0.0;
		}
	}
	
	private static double getCpuRateForLinux()
	{
		BufferedReader br = null ;
		 try{
			  File file = new File("/proc/stat");
		      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		      StringTokenizer token = new StringTokenizer(br.readLine());
		      token.nextToken();
		      
		      long user1 = Long.parseLong(token.nextToken());
		      long nice1 = Long.parseLong(token.nextToken());
		      long sys1  = Long.parseLong(token.nextToken());
		      long idle1 = Long.parseLong(token.nextToken());
		      
		      return  ((double)Math.round((double)idle1*1000 /(double)(user1 + nice1 + sys1 + idle1)))/10;
		      
		 } catch (Exception ex) {
				ex.printStackTrace();
				return 0.0;
		 } finally {
			 if(br!=null){
				 try {
					br.close() ;
				} catch (IOException e) {
					e.printStackTrace();
					return 0.0;
				}
			 }
		 }
	}
	/**
	 * 获取linux CPU空闲 率
	 * @return
	 */
	private static double getCpuRateForLinux2() 
	{
		InputStreamReader ir = null;
		LineNumberReader input = null;
		try {
			
			LogUtil.debug("*************************CPU 监控**************************");
			
			Process proc = Runtime.getRuntime().exec("top -n 1");

			BufferedReader in  = new BufferedReader(new InputStreamReader(proc.getInputStream()));   

			String line = null; 	           	           
	        while ((line = in.readLine()) != null) 
	        {
	        	line = line.toLowerCase();
				
				if (line.indexOf("cpu") == -1) 
					continue;

				if (line.indexOf("%id") != -1) 
				{
					int idx = line.indexOf("%id");
					String s = line.substring(0, idx);
					idx = s.lastIndexOf(" ");
					s = s.substring(idx + 1);
					return Double.parseDouble(s);
				}
	        
	        }			
			return -2;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				if (ir != null) {
					ir.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
			}
		}
	}
	
}