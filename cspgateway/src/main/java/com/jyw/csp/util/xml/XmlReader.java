package com.jyw.csp.util.xml;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * xml 读取对象 使用此对象必须最后调用 free 释放资源 
 */
public class XmlReader extends XmlConnection
{
	private static final long serialVersionUID = 1L;
	private  XmlReadView freadView      = null;
	
	/**
	 * 构造函数
	 * @throws Exception
	 */
	public XmlReader()
	{
		freadView = new XmlReadView();
	}
	
	
	/**
	 * 构造函数 传入xml文件路径
	 * @param xmlPath xml文件路径
	 * @throws Exception
	 */
	public XmlReader(String xmlPath)throws XMLException
	{
		super(xmlPath);
		
		freadView = new XmlReadView();
		  
	}
	
	/**
	 * XML节点组视图读取操作
	 * @return
	 */
	public XmlReadView readView()
	{
		return freadView;
	}
		
	/**
	 * 根据节点名称获取某节点下所有子节点
	 * @param parentNode 父节点
	 * @param nodeName 节点名称
	 * @return 某节点下所有子节点
	 */
	public synchronized XmlReadView getReadView(Element parentNode,String childName)
	{
		freadView = new XmlReadView();
		freadView.setElementList(this.getChildList(parentNode,childName));
		return freadView;
	}
	/**
	 * 根据节点路径获取根节点子节点集合
	 * @param childPath 节点路径
	 * @return 子节点集合
	 */
	public synchronized XmlReadView getReadView (String childPath)
	{
		freadView = new XmlReadView();
		freadView.setElementList(this.getChildList(childPath));
		return freadView;
	}
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param parentNode  父节点
	 * @param attrName 节点属性名称
	 * @param relative      节点字符型属性 =、 like、in 过滤 
	 * @param attrValue 节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public  synchronized XmlReadView getReadView (Element parentNode,String attrName,String relative ,String attrValue,int databg,int datand )
	{			
		freadView = new XmlReadView();
		String[] attrNameS = {attrName};
		String[] relativeS = {relative};
		String[] attrValueS = {attrValue};
		
		freadView.setElementList(super.getChildList(parentNode,attrNameS,relativeS,attrValueS,databg,datand));
		return freadView;
	}	
	
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param childPath  节点路径
	 * @param attrName 节点属性名称
	 * @param relative      节点字符型属性 =、 like、in 过滤 
	 * @param attrValue 节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public synchronized XmlReadView getReadView ( String childPath,String attrName,String relative ,String attrValue,int databg,int datand )
	{	
		freadView = new XmlReadView();
		String[] attrNameS = {attrName};
		String[] relativeS = {relative};
		String[] attrValueS = {attrValue};
		
		freadView.setElementList(super.getChildList(childPath,attrNameS,relativeS,attrValueS,databg,datand));
		return freadView;
	}
	
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param parentNode  父节点
	 * @param attrNameS   节点属性名称
	 * @param relativeS   节点字符型属性 =、 like、in 过滤 
	 * @param attrValueS   节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public  synchronized XmlReadView getReadViewS(Element parentNode,String[] attrNameS,String[] relativeS,String[] attrValueS,int databg,int datand )
	{			
		freadView = new XmlReadView();
		freadView.setElementList(super.getChildList(parentNode,attrNameS,relativeS,attrValueS,databg,datand));
		return freadView;
	}	
	
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param childPath  节点路径
	 * @param parentNode  父节点
	 * @param attrNameS   节点属性名称
	 * @param relativeS   节点字符型属性 =、 like、in 过滤 
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public synchronized XmlReadView getReadViewS ( String childPath,String[] attrNameS,String[] relativeS,String[] attrValueS,int databg,int datand )
	{	
		freadView = new XmlReadView();
		freadView.setElementList(super.getChildList(childPath,attrNameS,relativeS,attrValueS,databg,datand));
		return freadView;
	}
	/**
	 * 根据节点名称获取某节点第一个子节点值
	 * @param element 父节点
	 * @param nodeName 节点名称
	 * @return 某节点第一个子节点值
	 */ 
	public String getChildValue(Element element,String nodeName)
	{
		if (element != null)
		{
			return element.getTextTrim();
				
		}else
		{
			return "";
		}
	}	
	/**
	 * 获取某个节点的属性名称列表	
	 * @param element 节点对象
	 * @return 属性名称列表
	 */
	public String[] getChildAttrNames(Element element)
	{
		List  list = element.getAttributes();
		String names="";
		
		for (int i=0; i<list.size();i++)
		{
			if (names.equals(""))
				names =((Attribute)list.get(i)).getName();
			else
				names =names +","+((Attribute)list.get(i)).getName();
		}		
		return names.split(",",-2);
	}
	
	/**
	 * 获取某个节点某属性的值
	 * @param element 节点对象
	 * @param attrName 属性名称
	 * @return 属性值
	 */
	public String getChildAttrValue(Element element,String attrName)
	{
		if(element != null)
			return element.getAttributeValue(attrName);
		else
			return "";
	}
		
}