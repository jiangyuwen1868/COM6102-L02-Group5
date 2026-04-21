package com.jyw.csp.monitor;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 监控字段
 */
public class MonitorField  implements Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * 监控日期
	 */
	private String 	monitorDay="";
	
	/**
	 * 当天交易数
	 */
	private AtomicLong  tranctCount;
	
	/**
	 * 交易错误数
	 */
	private AtomicLong tranctErrorCount;	
	/**
	 * 系统错误数
	 */
	private AtomicLong sysErrorCount;	

	/**
	 * 系统拒绝交易数
	 */
	private AtomicLong sysRefuseCount;

	/**
	 * 构造函数
	 *
	 */
	public MonitorField()
	{
		tranctCount       = new AtomicLong(0);
		tranctErrorCount  = new AtomicLong(0);
		sysErrorCount     = new AtomicLong(0);
		sysRefuseCount    = new AtomicLong(0);
	}

	public String getMonitorDay() {
		return monitorDay;
	}

	public void setMonitorDay(String monitorDay) {
		this.monitorDay = monitorDay;
	}
	/**
	 * 累加交易数
	 */
	public  void addTranct()
	{
		tranctCount.getAndIncrement(); 

	}
	/**
	 * 返回交易累计数
	 * @return
	 */
	public long getTranCount() {
		return tranctCount.get();
	}
	
	/**
	 * 累加交易错误
	 */
	public  void addTranError()
	{
		tranctErrorCount.getAndIncrement(); 
	}
	/**
	 * 返回交易错误累计数
	 * @return
	 */
	public long getTranErrCount() {
		return tranctErrorCount.get();
	}
	
	/**
	 * 累加系统错误
	 */
	public  void addSysError()
	{
		sysErrorCount.getAndIncrement(); 
	}
	/**
	 * 返回系统错误累计数
	 * @return
	 */
	public long getSysErrCount() {
		return sysErrorCount.get();
	}
	
	/**
	 * 累加系统拒绝交易数
	 */
	public void addSysRefuse() {
		sysRefuseCount.getAndIncrement();
	}
	
	/**
	 * 返回系统拒绝交易数
	 * @return
	 */
	public long getSysRefuseCount() {
		return sysRefuseCount.get();
	}
	
}
