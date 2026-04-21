package com.jyw.csp.util.log.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import com.jyw.csp.util.log.base.LogConfig;


/**
 * 文件序号生成器
 * 
 * <p>Title: 文件序号生成器</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: anydef.com.cn</p>
 * @author pengdy
 * @version 1.0
 */
public class FileNoGenerator {
	private static final HashMap<String, Integer> dateFileNoMap = new HashMap<String, Integer>();
	private static DecimalFormat fileNoFormat = new DecimalFormat("0000");

	/**
	 * 清除过期的数据
	 */
	public static void clearExpiredData() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(LogConfig.getLogRollDirectoryTime());
		String date1 = sdf.format(calendar.getTime());
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		String date2 = sdf.format(calendar.getTime());

		synchronized (dateFileNoMap) {
			Iterator<String> iter = dateFileNoMap.keySet().iterator();
			while (iter.hasNext()) {
				String date = iter.next();
				if (date.equals(date1) || date.equals(date2)) {
					dateFileNoMap.remove(date);
				}
			}
		}
	}

	/**
	 * 得到文件序号
	 * 
	 * @param pathFile
	 * @param fileExtName
	 * @param date
	 * @param isCreate
	 * @return
	 */
	public static String getFileNo(File pathFile, String fileExtName,
			String date, boolean isCreate) {
		synchronized (dateFileNoMap) {
			Integer fileNo = dateFileNoMap.get(date);
			if (fileNo != null) {
				fileNo++;
			} else {
				fileNo = getFileNoFromDisk(pathFile, fileExtName, isCreate);
			}
			dateFileNoMap.put(date, fileNo);
			return fileNoFormat.format(fileNo);
		}
	}

	/**
	 * 从日志文件目录中获取序列号
	 * 
	 * @param pathFile 日志文件路径
	 * @param fileExtName 文件扩展名，过滤文件
	 * @param isCreate 是否新建文件
	 * @return
	 */
	protected static int getFileNoFromDisk(File pathFile,
			final String fileExtName, boolean isCreate) {
		String[] fileNames = pathFile.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(fileExtName);
			}

		});

		int maxFileNo = 0;
		for (String name : fileNames) {
			try {
				String s = name.substring(0, name.length() - 4);
				int no = Integer.parseInt(s);
				maxFileNo = Math.max(maxFileNo, no);
			} catch (Exception e) {
			}
		}

		int fileNo;

		if (isCreate) {
			fileNo = maxFileNo + 1;
		} else {
			if (fileNames.length == 0) {
				maxFileNo = 1;
			}
			fileNo = maxFileNo;
		}

		return fileNo;
	}
	
	public static void main(String[] args){
		File file = new File("E:/log/SERVICEAUTH/service/2014-08-28-15");
		System.out.println(getFileNoFromDisk(file,".log",true));
	}
}