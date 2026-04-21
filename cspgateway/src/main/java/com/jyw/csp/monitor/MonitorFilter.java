package com.jyw.csp.monitor;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jyw.csp.service.cache.CspSysConfigCache;

/**
 * 监控过滤器
 */
public class MonitorFilter implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorFilter.class);
	private static final long serialVersionUID = 1L;
	private static MonitorField monitorField = null;
	/**
	 * 服务运行总内存(MB)
	 */
	public static final String TOTALMEMORY = "totalMemory";
	/**
	 * 服务运行最大内存(MB)
	 */
	public static final String MAXMEMORY = "maxMemory";
	/**
	 * 服务运行空闲内存(MB)
	 */
	public static final String FREEMEMORY = "freeMemory";
	/**
	 * 服务运行内存使用率
	 */
	public static final String MEMORYRATE = "memoryRate";
	/**
	 * 主机资源总内存(GB)
	 */
	public static final String HOSTTOTALMEMORY = "hostTotalMemory";
	/**
	 * 主机资源空闲内存(GB)
	 */
	public static final String HOSTFREEMEMORY = "hostFreeMemory";
	/**
	 * 主机资源内存使用率
	 */
	public static final String HOSTMEMORYRATE = "hostMemoryRate";
	/**
	 * 主机资源CPU使用率
	 */
	public static final String CPURATE = "cpuRate";
	/**
	 * 主机资源磁盘使用率
	 */
	public static final String DISKRATE = "diskRate";
	/**
	 * 主机资源IO使用率
	 */
	public static final String IORATE = "ioRate";
	/**
	 * 主机资源网络使用率
	 */
	public static final String NETRATE = "netRate";
	/**
	 * 交易总数
	 */
	public static final String TRANCTCOUNT = "tranctCount";
	/**
	 * 交易失败数
	 */
	public static final String TRANERRCOUNT = "tranErrCount";
	/**
	 * 系统错误数
	 */
	public static final String SYSERRCOUNT = "sysErrCount";
	/**
	 * 系统拒绝交易数
	 */
	public static final String SYSREFUSECOUNT = "sysRefuseCount";

	public static final float u_size = 1024.00f;

	/**
	 * 获取监控信息
	 * 
	 * @return
	 */
	public static Hashtable<String, String> getMonitor() {
		Hashtable<String, String> monitorTable = new Hashtable<String, String>();

		double totalMemory = Runtime.getRuntime().totalMemory() / u_size / u_size;
		double freeMemory = Runtime.getRuntime().freeMemory() / u_size / u_size;
		double maxMemory = Runtime.getRuntime().maxMemory() / u_size / u_size;
		double memoryRate = 1 - (freeMemory / totalMemory);

		monitorTable.put(TOTALMEMORY, getFloatStr(String.valueOf(totalMemory)) + "MB");
		monitorTable.put(MAXMEMORY, getFloatStr(String.valueOf(maxMemory)) + "MB");
		monitorTable.put(FREEMEMORY, getFloatStr(String.valueOf(freeMemory)) + "MB");
		monitorTable.put(MEMORYRATE, getFloatStr(String.valueOf(memoryRate * 100)) + "%");
		
		MonitorServer ms = new MonitorServer();
		ms.copyToCpuAndMem();
		
//		Long lMemInfo[] = ResourceUsage.getMenInfo();
//		Long totalMem = lMemInfo[0];
//		Long freeMem = lMemInfo[1];
//		String tmp = String.valueOf((totalMem / u_size / u_size));
//		monitorTable.put(HOSTTOTALMEMORY, getFloatStr(tmp) + "GB");
//		tmp = String.valueOf((freeMem / u_size / u_size));
//		monitorTable.put(HOSTFREEMEMORY, getFloatStr(tmp) + "GB");
		
		monitorTable.put(HOSTTOTALMEMORY, ms.getMem().getTotal() + "GB");
		monitorTable.put(HOSTFREEMEMORY, ms.getMem().getFree() + "GB");
		
		// monitorTable.put("cpuRate", MonitorCpu.getCpuRatio()+"%");
//		float _cpuUsage = ResourceUsage.getCpuUsage();
//		String cpuUsage = String.valueOf(_cpuUsage * 100);
//		cpuUsage = getFloatStr(cpuUsage) + "%";
		String cpuUsage = ms.getCpu().getSys() + "%";
		
		float _diskUsage = OSUtils.getDiskUsage();
		String diskUsage = String.valueOf(_diskUsage * 100);
		diskUsage = getFloatStr(diskUsage) + "%";

		float _ioUsage = ResourceUsage.getIoUsage();
		String ioUsage = String.valueOf(_ioUsage * 100);
		ioUsage = getFloatStr(ioUsage) + "%";

//		float _memUsage = ResourceUsage.getMemUsage();
//		String memUsage = String.valueOf(_memUsage * 100);
//		memUsage = getFloatStr(memUsage) + "%";
		String memUsage = ms.getMem().getUsage() + "%";

		float _netUsage = ResourceUsage.getNetUsage();
		String netUsage = String.valueOf(_netUsage * 100);
		netUsage = getFloatStr(netUsage) + "%";

		monitorTable.put(HOSTMEMORYRATE, memUsage);
		monitorTable.put(CPURATE, cpuUsage);
		monitorTable.put(DISKRATE, diskUsage);
		monitorTable.put(IORATE, ioUsage);
		monitorTable.put(NETRATE, netUsage);
		monitorTable.put(TRANCTCOUNT, String.valueOf(getMonitorField().getTranCount()));
		monitorTable.put(TRANERRCOUNT, String.valueOf(getMonitorField().getTranErrCount()));
		monitorTable.put(SYSERRCOUNT, String.valueOf(getMonitorField().getSysErrCount()));
		monitorTable.put(SYSREFUSECOUNT, String.valueOf(getMonitorField().getSysRefuseCount()));

		return monitorTable;
	}

	/**
	 * 获取监控交易数字段
	 * 
	 * @return
	 */
	private static MonitorField getMonitorField() {
		if (monitorField == null) {
			monitorField = new MonitorField();
			monitorField.setMonitorDay(getdayNumber());

			return monitorField;
		} else {
			if (!monitorField.getMonitorDay().equals(getdayNumber())) {
				writeFile(monitorField);

				monitorField = new MonitorField();
				monitorField.setMonitorDay(getdayNumber());
			}

			return monitorField;
		}
	}

	/**
	 * 累加交易数
	 */
	public static void addTranct() {
		getMonitorField().addTranct();
	}

	/**
	 * 累加交易错误
	 */
	public static void addTranctError() {
		getMonitorField().addTranError();
	}

	/**
	 * 累加系统错误
	 */
	public static void addSysError() {
		getMonitorField().addSysError();
	}

	/**
	 * 累加系统拒绝交易数
	 */
	public static void addSysRefuse() {
		getMonitorField().addSysRefuse();
	}

	/**
	 * 把监控信息写到监控文件中
	 * 
	 * @return
	 */
	private synchronized static void writeFile(MonitorField monitorField) {
		RandomAccessFile randomFile = null;
		try {
			String monitorDir = CspSysConfigCache.getStrValue("cspGateway.txLogFilePath", "./logs");
			File dirFile = new File(monitorDir);
			if (!dirFile.exists())
				dirFile.mkdirs();

			dirFile = new File(monitorDir);
			if (!dirFile.exists())
				dirFile.mkdirs();

			randomFile = new RandomAccessFile(monitorDir + "/monitor.txt", "rw");

			if (randomFile != null) {
				String monitorStr = "日期:" + monitorField.getMonitorDay() + ";交易数:" + monitorField.getTranCount()
						+ ";交易失败数:" + monitorField.getTranErrCount() + ";系统失败数:" + monitorField.getSysErrCount()
						+ ";系统流控拒绝数:" + monitorField.getSysRefuseCount();

				randomFile.seek(randomFile.length());

				randomFile.write((monitorStr + "\r\n").getBytes());
				randomFile.close();
			}
		} catch (Exception e) {
			LOGGER.error("MonitorFilter writeFile", e);
		} finally {
			if (randomFile != null) {
				try {
					randomFile.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 获取当前日期 ：20070102
	 */
	private static String getdayNumber() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");

		Calendar cl = new GregorianCalendar();
		try {
			return sdf.format(cl.getTime());
		} catch (Exception ex) {
			return "99999999";
		}
	}

	/**
	 * 字符串大于7位，截取前7位
	 * 
	 * @param str
	 * @return
	 */
	private static String getFloatStr(String str) {
		if (str == null)
			return null;
		if (str.length() >= 7) {
			str = str.substring(0, 7);
		}
		return str;
	}
}