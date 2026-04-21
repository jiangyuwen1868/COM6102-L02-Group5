package com.jyw.csp.util.xml.enums;
import java.io.Serializable;
/**
 * 枚举单个元素对象
 */
public class EnumElement implements Cloneable, Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


    /**
     * 枚举元素键值
     */
	private String 		fkey;
	/**
     * 枚举元素值
     */
	private String 		fvalue ="";
	/**
     * 枚举元素属性值
     */
	private EnumDetail  fenumAttribute;
   

    public EnumElement() {
    	fenumAttribute = new EnumDetail();
    }
    
    public String getKey() {
		return fkey;
	}


	public void setKey(String key) {
		this.fkey = key;
	}


	public String getValue() {
		return fvalue;
	}


	public void setValue(String value) {
		this.fvalue = value;
	}


	public EnumDetail enumAttribute() {
		return fenumAttribute;
	}
	
	public void free() 
	{
		fkey 	= null;
		fvalue	= null;
		
		if (fenumAttribute != null)
			fenumAttribute.free();
		fenumAttribute = null;
			
	}
	

}