package com.jyw.csp.context;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Context  implements IContext, Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * 缓存池
	 */
	private ConcurrentMap<String, Entry> cache;				
	/**
	 * 构造函数
	 *
	 */
	public Context()
	{
		cache = new ConcurrentHashMap<String,Entry>();
	}
	
	/**
	 * 当前健值的对象是否已缓存
	 * @param key 缓存健值
	 * @return 已缓存为true,未缓存为false
	 */
	public  boolean containsKey (String key)
	{
		return cache.containsKey(key.toLowerCase());
	}
	
	/**
	 * 装载缓存对象
	 * @param key 缓存健值
	 * @param entry 缓存对象	 
	 */
	public  boolean put (String key,Object entry)
	{
		Entry cacheEntry = new Entry(key.toLowerCase(),entry);
		
		if (cache.put(key.toLowerCase(),cacheEntry)==null)
			return false;
		else
			return true;
		
	}
	/**
	 * 替换缓存列表对象
	 * @param key 缓存列健值
	 * @param entry 缓存对象	 
	 * @return 已装载为true,未装载为false
	 */
	public boolean replace (String key,Object entry)
	{
		Entry cacheEntry = new Entry(key.toLowerCase(),entry);
		if (cache.replace(key, cacheEntry)== null)
			return false;
		else
			return true;
	}
	
	/**
	 * 获取缓存对象
	 * @param key 缓存健值
	 * @return 缓存对象
	 */
	public  Object get(String key)
	{
		if (containsKey(key))
		{
			return cache.get(key.toLowerCase()).getContent();
		}else
			return null;
		
	}
	
	/**
	 * 获取缓存对象创建时间
	 * @param key 缓存健值
	 * @return 缓存对象创建时间
	 */	
	public long getCreateTime(String key)
	{
		if (containsKey(key))
		{
			return cache.get(key.toLowerCase()).getCreateTime();
		}else
			return 0;
		
	}
	
	/**
	 * 根据缓存健值清除缓存对象
	 * @param key 缓存健值
	 */
	public  boolean remove(String key)
	{
		if (containsKey(key))
		{
			cache.remove(key.toLowerCase());
			return true;
		}else
			return false;
	}
	
	/**
	 * 清除过时缓存对象
	 * @param lastTime 清除截至时间 如果为0 或小于0 则全部清除
	 */
	public  void clear(long lastTime)
	{
		if (lastTime <= 0)
		{
			cache.clear();
		}else
		{
			Object   key;
			Entry cacheEntry;
			
			Iterator<String> optit = cache.keySet().iterator();
			while(optit.hasNext()) 
			{
				key        = optit.next();
				cacheEntry = cache.get(key);
				if (cacheEntry.getCreateTime() < lastTime)
				{
					cache.remove(key);
				}
				
			}
		}
	}


 
}