package com.jyw.csp.monitor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSUtils {
	private final static Logger logger = LoggerFactory.getLogger(OSUtils.class);
	/**
	 * 功能：可用磁盘
	 * 
	 */
	public static int disk() {
		try {
			long total = FileSystemUtils.freeSpaceKb("/home");
			double disk = (double) total / 1024 / 1024;
			return (int) disk;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @Purpose:采集磁盘IO使用率
	 * @param args
	 * @return float,磁盘IO使用率,小于1
	 * @throws Exception
	 */
	public static float getDiskUsage() {
		double totalhd = 0;
		double usedhd = 0;
		BufferedReader in = null;
		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("df -hl /home");// df -hl 查看硬盘空间
			
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			String[] strArray = null;
			while ((str = in.readLine()) != null) {
				int m = 0;
				strArray = str.split(" ");
				for (String tmp : strArray) {
					if (tmp.trim().length() == 0) {
						continue;
					}
					++m;
					if (tmp.indexOf("G") != -1) {
						if (m == 2) {
							if (!tmp.equals("") && !tmp.equals("0"))
								totalhd += Double.parseDouble(tmp.substring(0, tmp.length() - 1)) * 1024;
						}
						if (m == 3) {
							if (!tmp.equals("none") && !tmp.equals("0"))
								usedhd += Double.parseDouble(tmp.substring(0, tmp.length() - 1)) * 1024;
						}
					}
					if (tmp.indexOf("M") != -1) {
						if (m == 2) {
							if (!tmp.equals("") && !tmp.equals("0"))
								totalhd += Double.parseDouble(tmp.substring(0, tmp.length() - 1));
						}
						if (m == 3) {
							if (!tmp.equals("none") && !tmp.equals("0"))
								usedhd += Double.parseDouble(tmp.substring(0, tmp.length() - 1));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in!=null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logger.debug("----totalhd----" + totalhd);
		logger.debug("----usedhd----" + usedhd);
		return (float) ((usedhd / totalhd));
	}

	/**
	 * 功能：获取Linux系统cpu使用率
	 */

	public static int cpuUsage() {
		try {
			Map<String, String> map1 = OSUtils.cpuinfo();
			Thread.sleep(5 * 1000);
			Map<String, String> map2 = OSUtils.cpuinfo();
			long user1 = Long.parseLong(map1.get("user").toString());
			long nice1 = Long.parseLong(map1.get("nice").toString());
			long system1 = Long.parseLong(map1.get("system").toString());
			long idle1 = Long.parseLong(map1.get("idle").toString());
			long user2 = Long.parseLong(map2.get("user").toString());
			long nice2 = Long.parseLong(map2.get("nice").toString());
			long system2 = Long.parseLong(map2.get("system").toString());
			long idle2 = Long.parseLong(map2.get("idle").toString());
			long total1 = user1 + system1 + nice1;
			long total2 = user2 + system2 + nice2;
			float total = total2 - total1;
			long totalIdle1 = user1 + nice1 + system1 + idle1;
			long totalIdle2 = user2 + nice2 + system2 + idle2;
			float totalidle = totalIdle2 - totalIdle1;
			float cpusage = (total / totalidle) * 100;
			return (int) cpusage;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 功能：CPU使用信息
	 */

	public static Map<String, String> cpuinfo() {
		InputStreamReader inputs = null;
		BufferedReader buffer = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
			buffer = new BufferedReader(inputs);
			String line = "";
			while (true) {
				line = buffer.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("cpu")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					List<String> temp = new ArrayList<String>();
					while (tokenizer.hasMoreElements()) {
						String value = tokenizer.nextToken();
						temp.add(value);
					}
					map.put("user", temp.get(1));
					map.put("nice", temp.get(2));
					map.put("system", temp.get(3));
					map.put("idle", temp.get(4));
					map.put("iowait", temp.get(5));
					map.put("irq", temp.get(6));
					map.put("softirq", temp.get(7));
					map.put("stealstolen", temp.get(8));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(buffer!=null)
					buffer.close();
				if(inputs!=null)
					inputs.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * 功能：内存使用率
	 */
	public static int memoryUsage() {

		Map<String, String> map = new HashMap<String, String>();
		InputStreamReader inputs = null;
		BufferedReader buffer = null;
		try {
			inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));
			buffer = new BufferedReader(inputs);
			String line = "";
			while (true) {
				line = buffer.readLine();
				if (line == null)
					break;
				int beginIndex = 0;
				int endIndex = line.indexOf(":");
				if (endIndex != -1) {
					String key = line.substring(beginIndex, endIndex);
					beginIndex = endIndex + 1;
					endIndex = line.length();
					String memory = line.substring(beginIndex, endIndex);
					String value = memory.replace("kB", "").trim();
					map.put(key, value);
				}
			}

			long memTotal = Long.parseLong(map.get("MemTotal").toString());
			long memFree = Long.parseLong(map.get("MemFree").toString());
			long memused = memTotal - memFree;
			long buffers = Long.parseLong(map.get("Buffers").toString());
			long cached = Long.parseLong(map.get("Cached").toString());
			double usage = (double) (memused - buffers - cached) / memTotal * 100;
			return (int) usage;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(buffer!=null)
					buffer.close();
				if(inputs!=null)
					inputs.close();

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return 0;
	}
}
