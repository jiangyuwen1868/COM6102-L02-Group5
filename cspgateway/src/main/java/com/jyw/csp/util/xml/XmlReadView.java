package com.jyw.csp.util.xml;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
/**
 * XMLDOM 操作控制对象
 */
public class XmlReadView  implements Serializable
{
	private static final long serialVersionUID = 1L;
	private  List<Element>    fElementList = null;
	private  Element          factiveElement = null;
	private  int              fCurrentNo = -1;
	private  int              felementCount  = 0;
	private  int              fendCount  = 0;
	/**
	 * 构造函数
	 * @throws Exception
	 */
	public XmlReadView()
	{
		fElementList = Collections.synchronizedList(new ArrayList<Element>());
	}
	/**
	 * 赋值XML节点集合
	 * @param activeChildList
	 */
	public void setElementList( List<Element>  elementList) 
	{
		if (elementList == null)
			return;
		
		fElementList        = elementList;
		factiveElement      = null;
		fCurrentNo          = -1;
		felementCount       = elementList.size();
	}
	
	/**
	 * 赋值XML节点集合
	 * @param activeChild
	 */
	public void addElemen(Element  elemen) {
		if (elemen == null)
			return;
		
		fElementList.add(elemen);
		factiveElement      = elemen;
		fCurrentNo          = -1;
		felementCount       = fElementList.size();
	}
	
	public int getIndex()
	{
		return fCurrentNo;
	}
	
	public void setEndCount(int endCount)
	{
		this.fendCount = endCount;
	}
	/**
	 * 判读是否第一条记录
	 * @return
	 */
	public boolean isFirst()
	{
		return fCurrentNo == 0;
	}
	/**
	 * 移位到第一条记录
	 */
	public void first() 
	{
		fCurrentNo = 0;
	}
	/**
	 * 移位到第0条记录
	 */
	public void beforeFirst()
	{
	     fCurrentNo = -1;
	}
	/**
	 * 是否最后一条记录
	 * @return
	 */
	public boolean isLast()
	{
	     return fCurrentNo == felementCount - 1;
	}
	/**
	 * 移位到最后一条记录
	 */
	public void last()
	{
		fCurrentNo = felementCount-1;
	}
	/**
	 * 判读是否最后一条记录
	 * @return
	 */
	public boolean isAfterLast()
	{
	      return fCurrentNo == felementCount;
	}
	/**
	 * 移位到下一条数据
	 * @return 移到最后一条返回false,否则返回true 
	 */
	public  boolean next()
	{
		int overCount = felementCount -1;
		if (fendCount>0 && overCount>fendCount)
			overCount = fendCount;
			
		if ( fCurrentNo < overCount) 
		{
	         fCurrentNo ++;
	         return true;
	      } else 
	      {
	    	  fCurrentNo ++; 
	    	  return false;
	      }
	 }
	/**
	 * 移位到下一条数据
	 * @return 移到第一一条返回false,否则返回true
	 */
	 public  boolean previous()
	 {
		 if ( fCurrentNo > 0 )
		 {
	            fCurrentNo --;
	            return true;
	     }else
	        return false;
	 }
	 /**
	  * 移位到记录集某条数据
	  * @param idx
	  */
	 public  void absolute(int idx)throws XMLException
	 {
		 if ( idx >= -1 && idx < felementCount )
		 {
			 fCurrentNo = idx;
		 }
	 }

	
	/**
	 * 根据节点列表获取当前位置节点
	 * @return 根节点第一个子节点
	 */
	 public  Element getNode()throws XMLException
	{
		 factiveElement = null;
		if (fCurrentNo>= 0 && fCurrentNo  < felementCount)
			factiveElement = fElementList.get(fCurrentNo);		
		
		return factiveElement;
	}
	
	 /**
	 * 根据节点列表获取当前位置节点路径下的子节点集合
	 * @return 位置节点路径下的子节点集合
	 */
	public  XmlReadView getReadView(String childPath)throws XMLException
	{
		XmlReadView fchildView = null;
		
		if (getNode() != null)
		{
			try {
				fchildView = new XmlReadView();
				List<Element> ElementList = XPath.selectNodes(factiveElement, childPath);
				fchildView.setElementList(ElementList);
			
			} catch (JDOMException e) {
				throw new XMLException("ecuac.com.xml.ReadView.getChildView:"+e.getMessage(),e);
			}
		}				
		return fchildView;
	}
	/**
	 * 根据节点列表获取当前位置节点所有子节点集合
	 * @return 所有子节点集合
	 */
	public  List<Element> getChilds()throws XMLException
	{
		if (getNode() != null)
		{
			return factiveElement.getChildren();
		}else
		{
			return null;
		}
	}
	 /**
	 * 根据节点列表获取当前位置节点所有子节点集合
	 * @return 所有子节点集合
	 */
	public  XmlReadView getReadView()throws XMLException
	{
		XmlReadView fchildView = new XmlReadView();
		fchildView.setElementList(getChilds());
		return fchildView;
	}
	/**
	 * 获取某个节点的属性名称列表	
	 * @return 属性名称列表
	 * @throws XMLException 
	 */
	public String[] getAttrNames() throws XMLException
	{
		if (getNode() == null)
			return new String[0];
		
		List  list   = factiveElement.getAttributes();
		String names = "";		
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
	 * 当前位置节点的属性值
	 * @param AttrName 节点属性名称
	 * @return 节点属性值
	 */
	public  String getAttrValue(String AttrName)throws XMLException
	{
		if (getNode() != null)
		{
			return factiveElement.getAttributeValue(AttrName);
		}else
		{
			return "";
		}
	}
	/**
	 * 当前位置节点的值
	 * @return 节点值
	 */
	public  String getNodeValue()throws XMLException
	{
		if (getNode() != null)
		{
			return factiveElement.getText();
		}else
		{
			return "";
		}
	}
	/**
	 * 当前位置节点的XML值
	 * @return XML值
	 */
	public  String getNodeXML()throws XMLException
	{
		if (getNode() != null)
		{
			XMLOutputter out = new XMLOutputter();
			return out.outputString(factiveElement);
		}else
		{
			return "";
		}		
	}
	/**
	 * 获取对象总数
	 * @return
	 */
	public int getElementCount(){
		
		return felementCount;
	}
}