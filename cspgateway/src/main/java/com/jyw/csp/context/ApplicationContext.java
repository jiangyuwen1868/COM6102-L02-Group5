package com.jyw.csp.context;

/**
 * 应用系统全局内存缓存
 */
public  class ApplicationContext extends Context{
	
	private static final long serialVersionUID = 1L;
	
	private static ApplicationContext AppContext = null;
	
	public static ApplicationContext getAppContext()
	{
		
		if (AppContext == null)
			AppContext = new ApplicationContext();
		
		return AppContext;
	}
	
	/**
	 * 判断是否已经缓存当前健值对象
	 * @param key 缓存健值
	 * @return 已缓存为true,未缓存为false
	 * @throws Exception
	 */
	public static boolean appContainsKey (String key)
	{
		return getAppContext().containsKey(key.toLowerCase());
	}
	
	/**
	 * 装载缓存对象
	 * @param key 缓存健值
	 * @param entry 缓存对象	 
	 * @throws Exception 
	 */
	public static void appPut (String key,Object entry)
	{
		getAppContext().put(key.toLowerCase(),entry);
	}
	
	/**
	 * 获取缓存对象
	 * @param key 缓存健值
	 * @return 缓存对象或null
	 */
	public static Object appGet(String key)
	{
		if (appContainsKey(key))
		{
			return getAppContext().get(key.toLowerCase());
		}else
			return null;
	}
	
	/**
	 * 根据缓存健值清除缓存对象
	 * @param key 缓存健值
	 */
	public static void  appRemove(String key)
	{
		
		if (appContainsKey(key))
		{
			getAppContext().remove(key.toLowerCase());
		}
	}
	
}