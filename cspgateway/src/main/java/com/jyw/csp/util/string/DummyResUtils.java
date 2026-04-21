package com.jyw.csp.util.string;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.StringTokenizer;

import com.jyw.csp.util.Utils;

public class DummyResUtils {

	/**
	 * 获取挡板实际内容，DummyResBean类的属性替换挡板内容中的表达式
	 * 
	 * @param dummyRes
	 * @param dummyResBean
	 * @return
	 */
	public static String getPlainDummyRes(String dummyRes,
			Object dummyResBean) {
		if (!StringUtils.hasLength(dummyRes)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		StringTokenizer token = new StringTokenizer(dummyRes);
		while (token.hasMoreTokens()) {
			String line = token.nextToken();
			String expr = Utils.parseExpression(line);
			for (int i = 0; i < Utils.EXPR_STARTS.length; i++) {
				String replaceExpr = Utils.EXPR_STARTS[i] + expr + "}";
				int idx = line.indexOf(replaceExpr);
				if (idx != -1) {
					String value = "";
					try {
						Field field = dummyResBean.getClass().getField(expr);
						field.setAccessible(true);
						value = (String) field.get(dummyResBean);
					} catch (Exception e) {
						char ch = expr.charAt(0);
						expr = "get" + Character.toUpperCase(ch) + expr.substring(1);
						try {
							value = (String)dummyResBean.getClass().getMethod(expr).invoke(dummyResBean);
						} catch(Exception ee){
							ee.printStackTrace();
						}
					}
					line = StringUtils.replace(line, replaceExpr, value==null?"":value);
				}
			}
			sb.append(line);
			sb.append("\r\n");
		}

		return sb.toString();
	}

	/**
	 * 获取挡板文件实际内容，DummyResBean类的属性替换挡板文件中的表达式
	 * 
	 * @param dummyResFile
	 * @param dummyResBean
	 * @return
	 */
	public static String getPlainDummyRes(File dummyResFile,
			Object dummyResBean) {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(dummyResFile);
			in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				String expr = Utils.parseExpression(line);
				for (int i = 0; i < Utils.EXPR_STARTS.length; i++) {
					String replaceExpr = Utils.EXPR_STARTS[i] + expr + "}";
					if (line.indexOf(replaceExpr) != -1) {
						String value = "";
						try {
							Field field = dummyResBean.getClass()
									.getField(expr);
							field.setAccessible(true);
							value = (String) field.get(dummyResBean);
						} catch (Exception e) {
							char ch = expr.charAt(0);
							expr = "get" + Character.toUpperCase(ch) + expr.substring(1);
							try {
								value = (String)dummyResBean.getClass().getMethod(expr).invoke(dummyResBean);
							} catch(Exception ee){
								ee.printStackTrace();
							}
						}
						line = StringUtils.replace(line, replaceExpr, value==null?"":value);
					}
				}
				sb.append(line);
				sb.append("\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}

		return sb.toString();
	}
}
