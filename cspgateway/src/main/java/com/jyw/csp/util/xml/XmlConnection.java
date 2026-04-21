package com.jyw.csp.util.xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
/**
 * xml 读取对象 使用此对象必须最后调用 free 释放资源 
 */
public class XmlConnection implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private   List     activeChildList = null;
	private   Element  activeChild     = null;	
	private   String   fxmlPath        = "";
	protected Element  rootNode        = null;
	protected Document document        = null;	
	private   int      dataCount       = 0;
	
	/**
	 * 构造函数
	 * @throws Exception
	 */
	public XmlConnection()
	{
		
	}
		
	/**
	 * 构造函数 传入xml文件路径
	 * @param xmlPath xml文件路径
	 * @throws Exception
	 */
	public XmlConnection(String xmlPath)throws XMLException
	{
		setXmlPath(xmlPath);	  
	}	
	/**
	 * 赋Xml文件路径 ，并初始XML 对象
	 * @param xmlPath
	 * @throws Exception
	 */
	public void setXmlPath(String xmlPath)throws XMLException
	{
		fxmlPath = xmlPath;
		read(false);
	}
	
	/**
	 * 赋Xml文件路径 ，并初始XML 对象
	 * @param xmlPath
	 * @throws Exception
	 */
	public void setXmlPath(String xmlPath,boolean islock)throws XMLException
	{
		fxmlPath = xmlPath;
		read(islock);
	}
	
	
	/**
	 * 赋xmlStr 数值 ，并初始XML 对象
	 * @param xmlStr xmlStr 数值
	 * @throws Exception
	 */
	public void setXmlStr(String xmlStr)throws XMLException
	{
		Reader reader = null;
		
		if (xmlStr == null || xmlStr.trim().equals(""))
		{
			throw new XMLException("xmlStr 不能为空！");
		}
		
		try
		{
			reader = new StringReader(xmlStr);
	        SAXBuilder saxbd = new SAXBuilder();
	        document     = saxbd.build(reader);
	        rootNode     = document.getRootElement();
	           	            
		}catch(Exception exception)
		{
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			reader = null;
			throw new XMLException(exception.getMessage(),exception);
		}
		finally
		{
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			reader = null;
		}
	}
	/**
	 * 获取截取数据的数据总数
	 * @return
	 */
	public int getDataCount()
	{
		return dataCount;
	}
	/**
	 * 获取 document XML 字符串
	 * @return XML 字符串
	 */
	public String getXml()
	{
		if (document != null)
		{
			XMLOutputter out = new XMLOutputter();
			return out.outputString(document);
		}else
			return "";
	}
	
	/**
	 * 获取某元素对象的 XML 字符串
	 * @param element 某元素对象
	 * @return XML 字符串
	 */ 
	public String getXml(Element element)
	{
		if (element != null)
		{
			XMLOutputter out = new XMLOutputter();
			return out.outputString(element);
		}else
			return "";
	}
	
	/**
	 * 获取根节点
	 * @return 根节点
	 */
	public Element getRootNode()
	{
		return rootNode;
	}
	/**
	 * 获取当前取得的节点
	 * @return 活动节点
	 */
	public Element getActiveNode()
	{
		return activeChild;
	}
	/**
	 * 根据节点名称获取某节点第一个子节点
	 * @param parentNode 父节点
	 * @param nodeName 节点名称
	 * @return 某节点第一个子节点
	 */
	public Element getChild(Element parentNode,String childName)
	{
		if (parentNode != null)
		{
			activeChild = parentNode.getChild(childName);				
		}
		return activeChild;
	}	
	/**
	 * 获取当前取得的节点集合
	 * @return 活动节点集合
	 */
	public List getActiveChildList()
	{
		return activeChildList;
	}	
	/**
	 * 根据节点路径获取根节点子节点集合
	 * @param childPath 节点路径
	 * @return 子节点集合
	 */
	public List getChildList(String childPath)
	{
		dataCount = 0;
		List<Element> ElementList = null;		
		try {
			ElementList = XPath.selectNodes(rootNode, childPath);
			
			if (ElementList != null)
				dataCount = ElementList.size();
			
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return ElementList;
	}	
	/**
	 * 根据节点名称获取某节点下所有子节点
	 * @param parentNode 父节点
	 * @param nodeName 节点名称
	 * @return 某节点下所有子节点
	 */
	public List getChildList(Element parentNode,String childName)
	{
		dataCount = 0;
		activeChildList = null;
		if (parentNode != null)
		{
			activeChildList = parentNode.getChildren(childName);			
		}
		if (activeChildList != null)
			dataCount = activeChildList.size();
		return activeChildList;
	}
	
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param parentNode  父节点
	 * @param attrNameS 节点属性名称
	 * @param relativeS      节点字符型属性 =、 like、in 过滤 
	 * @param attrValueS 节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public  List<Element>  getChildList(Element parentNode,String[] attrNameS,String[] relativeS,String[] attrValueS,int databg,int datand )
	{			
		Element element = null;
		List<Element>  selectChildList = new ArrayList<Element>();
		List ElementList = parentNode.getChildren();
		
		if (ElementList != null)
		{
			dataCount = 0;
			
			for (int j=0 ;j< ElementList.size(); j++)
			{
				element = (Element)ElementList.get(j);	
				if (fitswith(element,attrNameS,relativeS,attrValueS))
				{
					if (datand >0)
					{
						if(dataCount >= databg && dataCount< datand)
						{
							selectChildList.add(element);
						}
						
						dataCount ++;
					}else
					{
						selectChildList.add(element);
					}
				}			
			}				
		}			
		return selectChildList;		
	}	
	
	/**
	 * 根据节点路径、节点属性名称和属性值获取某节点
	 * @param childPath  节点路径
	 * @param attrNameS 节点属性名称
	 * @param relativeS      节点字符型属性 =、 like、in 过滤 
	 * @param attrValueS 节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @param databg 获取数据起始位置
	 * @param datand 获取数据结束位置
	 * @return 符合条件节点集
	 * @throws Exception
	 */
	public  List<Element>  getChildList( String childPath,String[] attrNameS,String[] relativeS,String[] attrValueS,int databg,int datand )
	{	
		Element element = null;		
		List<Element>  selectChildList = new ArrayList<Element>();
		
		List<Element> ElementList = getChildList(childPath);	
		if (ElementList != null)
		{
			dataCount = 0;
			
			for (int j=0 ;j< ElementList.size(); j++)
			{
				element = (Element)ElementList.get(j);	
				if (fitswith(element,attrNameS,relativeS,attrValueS))
				{
					if (datand >0)
					{
												
						if(dataCount >= databg && dataCount< datand)
						{
							selectChildList.add(element);
						}
						dataCount ++;
					}else
					{
						selectChildList.add(element);
					}
				}
			}				
		}				
		return selectChildList;		
	}
	/**
	 * 多重条件联合判断过滤数据
	 * @param element     节点元素 
	 * @param attrNames   节点属性名称数组
	 * @param relatives   节点字符型属性 =、 like、in 过滤 数组
	 * @param attrValues  节点属性值以 in 过滤 则属性值，以","分离 如value1,value2  数组
	 * @return 符合true,否则false
	 */
	private  boolean  fitswith(Element element,String[] attrNameS,String[] relativeS,String[] attrValueS)
	{
		boolean isfit = true;
		
		for (int i=0;i<attrNameS.length;i++)
		{
			if (i< relativeS.length && i< attrValueS.length)
			{
				if (!fitwith(element,attrNameS[i],relativeS[i],attrValueS[i]))
				{
					isfit = false;
					break;
				}
			}else
			{
				isfit = false;
				break;
			}
		}
		return isfit;		
	}
	
	
	/**
	 * 判断该节点是否符合输入属性条件
	 * @param element   节点元素 
	 * @param attrName  节点属性名称
	 * @param relative  节点字符型属性 =、 like、in 过滤 
	 * @param attrValue 节点属性值以 in 过滤 则属性值，以","分离 如value1,value2
	 * @return 符合true,否则false
	 */
	private  boolean  fitwith( Element element,String attrName,String relative ,String attrValue )
	{			
		boolean isfit = false;
		
		
		if (relative == null || relative.trim().equals(""))
			relative = "=";
		
		if(relative.trim().toLowerCase().equals("in"))
			attrValue = "," + attrValue + ",";
		
		if (element != null)
		{
			if (element.getAttribute(attrName) != null)
			{
				if(relative.trim().toLowerCase().equals("="))
				{
					if ( element.getAttributeValue(attrName).equals(attrValue))
						isfit =true;					
						
				}else if(relative.trim().toLowerCase().equals("like"))
				{
					if ( element.getAttributeValue(attrName).contains(attrValue))
						isfit =true;	
											
				}else 	if(relative.trim().toLowerCase().equals("in"))
				{
					 String attributeValue  = "," + element.getAttributeValue(attrName) +",";
					 if ( attrValue.contains(attributeValue))
						 isfit =true;							
				}else 	if(relative.trim().toLowerCase().equals("not in"))
				{
					 String attributeValue  = "," + element.getAttributeValue(attrName) +",";
					 if ( !attrValue.contains(attributeValue))
						 isfit =true;							
				}						
			}
		}
		return isfit;
	}
	
	/**
	 * 初始XML 对象
	 * @throws Exception
	 */
	private synchronized void read(boolean islock)throws XMLException
	{
		SAXBuilder saxbd = new SAXBuilder();		
		InputStream inputstream = null;
			
		try
		{
			inputstream = getClass().getResourceAsStream(fxmlPath);			
		    if (inputstream == null)
			{
		    	inputstream = new FileInputStream(new File(fxmlPath));
				document = saxbd.build(inputstream);
			}else
			{
				document = saxbd.build(inputstream);
			}
			
			rootNode     = document.getRootElement();
						
		}catch(JDOMParseException jdom)
		{
			throw new XMLException(jdom.getMessage(),jdom);
		}
		catch(Exception exception)
		{
			if (inputstream != null)
			{
				try {
					inputstream.close();
				} catch (IOException e) 
				{					
					throw new XMLException(e.getMessage(),e);
				}
			}
			
			inputstream = null;
						
			throw new XMLException(exception.getMessage(),exception);
		}
		
	}
	
	/**
	 * 把xml对象保存文件中
	 * @throws IOException 
	 * @throws XMLException
	 */
	public  void save() throws XMLException
	{
		Save(document,fxmlPath);
	}
	public  void saveBack(String backPath) throws XMLException
	{
		//Save(document,backPath);
	}	
	
	private synchronized void Save(Document saveDoc,String savePath) throws XMLException
	{
		XMLOutputter xmlOutputter = new XMLOutputter();
		OutputStream outputStream = null;		
		FileReader fr = null;
				
		try
		{
			 fr = new FileReader(new File(fxmlPath));
			
			if (fr.read() !=-1)
			{
				outputStream = new FileOutputStream(savePath);
				xmlOutputter.output(saveDoc,outputStream);
			}
			
		}catch(Exception exception)
		{
			try {
				if (outputStream != null)
				{
					outputStream.flush();
					outputStream.close();
				}
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				
				throw new XMLException(e.getMessage(),e);
			}
			
			fr = null;
			outputStream = null;
			xmlOutputter = null;
			
			throw new XMLException(exception.getMessage(),exception);
			
		}
	}
	
	/**
	 * 释放资源
	 */
	public void free()
	{
		rootNode     = null;
		document     = null;
	}
}