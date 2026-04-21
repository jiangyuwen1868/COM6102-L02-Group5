package com.jyw.csp.util.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;


/**
 * XmlWriter 写入对象；使用此对象必须最后调用 free 释放资源 
  */
public class XmlWriter extends XmlConnection
{
	private static final long serialVersionUID = 1L;

	/**
	 * 构造函数
	 * 默认调用lily.xml文件
	 * @throws Exception
	 */
	public XmlWriter()
	{
	}
	
	/**
	 * 构造函数 传入xml文件路径
	 * @param xmlPath xml文件路径
	 * @throws Exception
	 */
	public XmlWriter(String xmlPath)throws XMLException
	{
		super(xmlPath);
	}
	
	/**
	 * 添加节点子节点
	 * @param parentNode 父节点
	 * @param element 子节点
	 */
	public void appendChild(Element parentNode,Element element)
	{
		parentNode.addContent(element).addContent("\n");
	}
	
	/**
	 * 添加节点子节点
	 * @param parentNode 父节点
	 * @param nodeName 节点名称
	 * @param nodeValue 节点值
	 * @return 返回 Element 对象
	 */
	public Element appendChild(Element parentNode,String nodeName,String nodeValue)
	{
		Element element = new Element(nodeName);
		element.setText(nodeValue);
		parentNode.addContent(element);
		
		return element;
	}
	
	/**
	 * 添加节点 xml 值 
	 * @param parentNode 父节点
	 * @param xmlStr xml值
	 */
	public void appendChild(Element parentNode,String xmlStr)
	{
		parentNode.addContent(xmlStr);
	}
	
	/**
	 * 添加节点CDATA
	 * @param parentNode 父节点
	 * @param cdata CDATA 值
	 * @return 返回 CDATA 对象
	 */
	public CDATA appendCDATA(Element parentNode,String cdata)
	{
		CDATA CData = new CDATA(cdata);		
		parentNode.addContent(CData);
		
		return CData;
	}
		
	/**
	 * 添加节点属性
	 * @param parentNode 父节点
	 * @param attrName 属性名称
	 * @param attrValue 属性值
	 */
	public void appendAttribute(Element parentNode,String attrName,String attrValue)
	{
		parentNode.setAttribute(attrName,attrValue);
	}
	
		/**
	 * @param rootElement
	 * 			根节点
	 * @param xmlPath
	 * 			xml保存路径
	 */
	public void writeXML(Element rootElement, String xmlPath) {
		
		XMLOutputter out = new XMLOutputter();
		out.setFormat(out.getFormat().setEncoding("utf-8").setIndent("\t"));
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(xmlPath));
			out.output(new Document(rootElement), fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fos!=null) {
				try {
					fos.close();
					fos = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		String xmlPath = "E:/WorkSpace/JSpace/unitTest/6217007100008609799.xml";
		
		XmlWriter xml = new XmlWriter();
		Element rootNode = new Element("dp");	
		xml.appendChild(rootNode, "appVersionType", "1.0.1");
		xml.appendChild(rootNode, "cardbrh", "440000000");
		xml.appendChild(rootNode, "taskid", "6666");
		xml.appendChild(rootNode, "cardno", "1");

		Element childNode = new Element("card1");
		xml.appendChild(rootNode, childNode);
		xml.appendChild(childNode, "reapplyFlag", "1");
		xml.appendChild(childNode, "appAidType", "A000000333010101");
		xml.appendChild(childNode, "processid", "00000000000000000000");
		xml.appendChild(childNode, "seIdType", "000000");
		xml.appendChild(childNode, "expiryDateType", "1224");
		xml.appendChild(childNode, "panType", "6217007100008609799");
		xml.appendChild(childNode, "apsApplyNo", "");
		xml.appendChild(childNode, "cardName", "");
		xml.appendChild(childNode, "cellPhone", "");
		xml.appendChild(childNode, "cvv2", "");
		xml.appendChild(childNode, "productId", "20150110000000000088");
		
		xml.writeXML(rootNode, xmlPath);
		System.out.println("****************************************************");
	}
	
}