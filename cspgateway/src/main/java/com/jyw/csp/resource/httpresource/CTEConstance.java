package com.jyw.csp.resource.httpresource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import com.jyw.csp.exception.ActionFlowExceedException;
import com.jyw.csp.util.log.LogUtil;

public class CTEConstance {
	public static Hashtable limitedOpListConfig = null; // 受限交易动态变化表

	public static Hashtable opList = new Hashtable(); // 受限交易列表
	
	public String fileName = "limitedOpList.ini";
	
	public CTEConstance(){
		super();
	}
	/**
	 * CTEConstance constructor comment.
	 * @throws Exception 
	 */
	public CTEConstance(String fileName) throws Exception {
		super();
		this.fileName = fileName;
//		HttpCommPoolService ser = new HttpCommPoolService(); 
//		ser.getHeadParams("ECUAC");
//		ser.sendAndWait("ECUAC", "TEST", -1, -1, null);
	}

	/**
	 * 初始化交易限制列表文件，该交易限制列表文件的定义，用于对某个交易指定 在某一时刻可并发的最大请求数。
	 * 
	 * @exception java.lang.Exception
	 *                文件不存在或文件存在格式错误
	 */
	public void initializeLimitedOpList() throws Exception {

		if(limitedOpListConfig!=null)return;
		
		limitedOpListConfig = new java.util.Hashtable();

		String line = "";
		int index = 0;
		
		if(fileName == null || "".equals(fileName)){
			return;
		}

		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(fileName));
	
			while ((line = in.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				else if (line.trim().length() == 0)
					continue;
				line = line.trim();
				index = line.indexOf("=");
				if (index > 0 && index < line.length() - 1) {
	
					limitedOpListConfig.put(line.substring(0, index), line
							.substring(index + 1).trim());
	
				}
			}
		}catch(Exception e){
			LogUtil.error(e);
		}finally{
			if(in!=null){
				in.close();
			}
		}
	}

	/**
	 * 交易并发访问控制：当前控制数加一
	 * 
	 * @param operKey
	 *            java.lang.String
	 */
	public void opParallelLock(String operKey)
			throws ActionFlowExceedException {
		if (operKey == null)
			return;
		boolean limitEnable = false;
		try {
//			limitEnable = ((String) Configs.getSettings().getValueAt(
//					"opLimitedEnable")).equals("true");
			limitEnable = false;
		} catch (Exception e) {
		}
		if (!limitEnable)
			return;
		if (CTEConstance.limitedOpListConfig.get(operKey) != null) {
			int maxNum = -1;
			try {
				maxNum = Integer
						.parseInt((String) CTEConstance.limitedOpListConfig
								.get(operKey));
			} catch (Exception ei) {
				CTEConstance.limitedOpListConfig.remove(operKey);
			}
			if (maxNum <= 0) {
				throw new ActionFlowExceedException("XTPS10000010", "交易并发数限制：交易[" + operKey
						+ "]超过了最大并发数[0]");
			}

			if (CTEConstance.opList.get(operKey) != null) {
				int curNum = ((Integer) CTEConstance.opList.get(operKey))
						.intValue();
				if (curNum >= maxNum) {
					throw new ActionFlowExceedException("XTPS10000011", "交易并发数限制：交易[" + operKey
							+ "]超过了最大并发数[" + curNum + "]");
				} else {
					synchronized (opList) {
						CTEConstance.opList.put(operKey,
								new Integer(curNum + 1));
					}
				}

			} else {
				synchronized (opList) {
					CTEConstance.opList.put(operKey, new Integer(1));
				}
			}
		}
	}

	/**
	 * 交易并发访问控制：当前控制数减一
	 * 
	 * @param operKey
	 *            java.lang.String
	 */
	public void opParallelUnlock(String operKey) {
		if (operKey == null) {
			return;
		}
		boolean limitEnable = false;
		try {
//			limitEnable = ((String) Configs.getSettings().getValueAt(
//					"opLimitedEnable")).equals("true");
			limitEnable = false;
		} catch (Exception e) {
		}
		if (!limitEnable)
			return;

		if (CTEConstance.opList.get(operKey) == null)
			return;
		try {
			synchronized (opList) {

				int curNum = ((Integer) CTEConstance.opList.get(operKey))
						.intValue();
				CTEConstance.opList.put(operKey, new Integer(curNum - 1));
			}
		} catch (Exception e) {
			try {
				CTEConstance.limitedOpListConfig.remove(operKey);
			} catch (Exception ex) {
			}
			CTEConstance.opList.remove(operKey);
		}
	}
}