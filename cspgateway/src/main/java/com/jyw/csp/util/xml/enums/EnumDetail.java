package com.jyw.csp.util.xml.enums;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 枚举明细存贮
 */
public class EnumDetail implements Cloneable, Serializable{

	private static final long serialVersionUID = 1L;
	private List<EnumElement>   fenumElements;
	private List<String>        fKeys;
	private EnumElement         activeElement;
	private int          		elementIndex;
 
    public EnumDetail() 
    {
    	fenumElements   =  new CopyOnWriteArrayList<EnumElement>(); 
    	fKeys           =  new CopyOnWriteArrayList<String>(); ;

    }
    /**
     * 获取枚举数量
     * @return
     */
    public int getElementCount() {
    	return fKeys.size();
    }
    
    /**
     * 得到枚举单个元素对象
     * @return
     */
    public EnumElement getElement(int index) {
    	    	
    	if (index >-1 && index < getElementCount() )
    	{
    		if(index == elementIndex)
    			return activeElement;
    		else
    		{
    			activeElement = fenumElements.get(index);
    			elementIndex  = index;
    		}
    	}else
    	{
    		elementIndex =-1;
    		activeElement = null;
    	}
    	
    	return activeElement;
    }
    
    /**
     * 得到枚举单个元素对象
     */
    public EnumElement getElement(String key) 
    {
       	if (fKeys == null)
    		return null;
    	
    	elementIndex = fKeys.indexOf(key);	
    	
    	if (elementIndex > -1)
		{
			activeElement = fenumElements.get(elementIndex);
		}else
			activeElement = null;
		
		return activeElement;
    }
    
    /**
     * 得到枚举单个元素对象Key值
     * @return
     */
    public String getElementKey (int index) 
    {
    	getElement(index);
    	
    	if (activeElement != null)
    		return activeElement.getKey();
    	else
    		return null;   
    }
    
    /**
     * 得到枚举单个元素对象值
     * @return
     */
    public String getElementValue (int index) 
    {
    	getElement(index);
    	
    	if (activeElement != null)
    		return activeElement.getValue();
    	else
    		return null;   
    
    }
    /**
     * 得到枚举单个元素对象值
     * @return
     */
    public String getElementValue (String key) {
    	getElement(key);
    	
    	if (activeElement != null)
    		return activeElement.getValue();
    	else
    		return null;
    
    }
    /**
     * 增加一个枚举子元
     * @param key
     * @param value
     * @roseuid 3F7167C80135
     */
    public void add(String key, String value) 
    {
    	getElement(key);
       	if (activeElement != null)
    	{
    		activeElement.setValue(value);
    		fenumElements.set(elementIndex, activeElement);    		
    	}else
    	{
    		fKeys.add(key);
    		activeElement = new EnumElement();
    		activeElement.setKey(key);
    		activeElement.setValue(value);
    		fenumElements.add(activeElement);
    	}
    }
      
    /**
     * 增加一个枚举属性值
     * @param attrKey  属性键值
     * @param attrValue 属性值
     */
    public void addAttribute(String attrKey, String attrValue)
    {
       	if (activeElement != null)
    	{
    		activeElement.enumAttribute().add(attrKey, attrValue);    			
    	}
    }    
    /**
     * 增加一个枚举属性值
     * @param key      枚举键值
     * @param attrKey  属性键值
     * @param attrValue 属性值
     */
    public void addAttribute(String key, String attrKey, String attrValue)
    {
       	getElement(key);
    
    	if (activeElement != null)
    	{
    		activeElement.enumAttribute().add(attrKey, attrValue);
       	}
    }
    
    /**
     * 获取一个枚举属性值
     * @param key      枚举键值
     * @param attrKey  属性键值
     */
    public String getAttribute(String key, String attrKey) {
    	
    	getElement(key);
    	
    	if (activeElement != null)
    	{
    		return activeElement.enumAttribute().getElementValue(attrKey);
    	}else
    		return null;
    }
    /**
     * 获取一个枚举属性值
     * @param attrKey  属性键值
     */
    public String getAttribute(String attrKey) {
    	
    	if (activeElement != null)
    	{
    		return activeElement.enumAttribute().getElementValue(attrKey);
    	}else
    		return null;
    }
    /**
     * 获取Key集合
     */
    public List<String>  getKeyArray() 
    {
       	return fKeys;
    }
    /**
     * 释放资源
     */
    public void free() 
	{
    	fKeys.clear();
    	fKeys = null;
    	for (int i=0; i<fenumElements.size();i++)
    	{
    		fenumElements.get(i).free();
    	}
    	fenumElements.clear();
    	fenumElements = null;
    	activeElement = null;
					
	}
   
}