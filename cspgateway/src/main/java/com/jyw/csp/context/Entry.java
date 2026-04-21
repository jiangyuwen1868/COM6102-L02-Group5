 package com.jyw.csp.context;

import java.io.Serializable;
/**
 * 环境变量实体对象
 *
 */
public class Entry implements Cloneable, Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 实体对象
	 */
	private Object fContent = null;

	/**
	 * 实体对象键值
	 */
	private String fKey;	
	
	
	/**
	 * 创建时间
	 */
	private long created = -1;
	
	/**
	 * 实体对象构造函数
	 * @param key 实体键值
	 * @param content 实体主体
	 */
	public Entry(String key, Object content)
	{
		fKey     = key;
		fContent = content;
		created = System.currentTimeMillis();
	}
	
	/**
	 * 获取键值
	 * @return 返回键值
	 */
	public String getKey()
	{
		return fKey;
	}
	
	/**
	 * 获取主体
	 * @return 返回主体
	 */
	public Object getContent()
	{
		return fContent;		
	}
	
	/**
	 * 返回创建时间
	 * @return
	 */
	public long getCreateTime()
	{
		return created;
	}
	
}