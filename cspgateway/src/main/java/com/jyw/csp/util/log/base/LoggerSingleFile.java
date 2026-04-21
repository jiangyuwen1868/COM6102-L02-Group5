package com.jyw.csp.util.log.base;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jyw.csp.util.log.LogUtil;

/**
 * 根据日志滚动Mode记录日志文件
 *  #month：每月一个文件目录
	#week：每周一个文件目录
	#day：每天一个文件目录
	#midday：每12个小时一个文件目录
	#hour：每小时一个文件目录
	#minute：每分钟一个文件目录
 * 
 * <p>Title: LoggerSingleFile</p>
 * <p>Description: 日志工具类，use iostream</p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class LoggerSingleFile {

	private String lastRolling;
	private SimpleDateFormat rollingModel;
	private SimpleDateFormat logDateModel;
	private SimpleDateFormat fileDateModel;
	private String appCode;
	private String subAppCode;

	public LoggerSingleFile() {
		this.rollingModel = new SimpleDateFormat(getRollingModel());
		this.logDateModel = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		this.fileDateModel = new SimpleDateFormat("yyyyMMddHHmmss_");
		this.lastRolling = "";
		this.appCode = LogConfig.APP_CODE;
		this.subAppCode = LogConfig.SERVER_INSTANCE_CODE;
	}

	private String getRollingModel() {
		return LogConfig.getLogRollDirectoryTime();

	}

	private OutputStream createLogFile(String transID, Date now) {
		String r = this.rollingModel.format(now);

		String header = this.appCode + File.separator + this.subAppCode + File.separator;
		String path = LogConfig.logFilePath;
		path = path
				+ ((path.endsWith("/")) ? header + r : new StringBuffer("/")
						.append(header).append(r).toString());
		if (!(r.equals(this.lastRolling))) {
			File dir = new File(path);
			if (!(dir.exists()))
				try {
					dir.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

			this.lastRolling = r;
		}
		path = path + "/" + /* this.fileDateModel.format(now) + */transID
				+ ".log";
		try {
//			return new FileOutputStream(path, true);
			return new BufferedOutputStream(new FileOutputStream(path, true));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public boolean log(String info, String transID) {
		Date now = new Date();
		OutputStream out = createLogFile(transID, now);
		if (out != null)
			try {
				out.write(("[" + this.appCode + "][" + this.subAppCode + "] ["
						+ this.logDateModel.format(now) + "] [msg=")
						.getBytes());

				out.write((info + "]\r\n").getBytes());
//				out.flush();

				return true;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				LogUtil.error("", e);
				return false;
			} finally {
				try {
					out.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
					LogUtil.error("", e);
				}
			}

		return false;
	}

	/**
	 * 生成随机数
	 * @return
	 */
	public static String genRandom() {
		SecureRandom random = new SecureRandom();
		byte[] bs = new byte[12];
		random.nextBytes(bs);
		return new BigInteger(bs).abs().toString(10);
	}
}