package com.jyw.csp.context;
/**
 * 缓存对象接口
 *
 */
public interface IContext 
{	
	/**
	 * 判断是否已经缓存当前健值对象
	 * @param key 缓存健值
	 * @return 已缓存为true,未缓存为false
	 * @throws Exception
	 */
	public boolean containsKey (String key);
	/**
	 * 装载缓存对象
	 * @param key 缓存健值
	 * @param entry 缓存对象	 
	 * @return 已装载为true,未装载为false
	 */
	public boolean  put (String key,Object entry);
	
	/**
	 * 替换缓存列表对象
	 * @param key 缓存列健值
	 * @param entry 缓存对象	 
	 * @return 已装载为true,未装载为false
	 */
	public boolean replace (String key,Object entry);
	/**
	 * 获取缓存对象
	 * @param key 缓存健值
	 * @return 缓存对象或null
	 */
	public Object  get(String key);
	
	/**
	 * 根据缓存健值清除缓存对象
	 * @param key 缓存健值
	 * @return 已删除为true,未删除为false
	 */
	public boolean  remove(String key);
	/**
	 * 清除过时缓存对象
	 * @param lastTime 清除截至时间 如果为0 或小于0 则全部清除
	 */
	public void  clear(long lastTime);
	
	public static final int ACTION       = 0;
	public static final int PAGE         = 1;
	public static final int SESSION      = 2;
	public static final int APPLICATION  =  3;
	
}