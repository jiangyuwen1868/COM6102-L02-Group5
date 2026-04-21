package com.jyw.csp.util.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.jyw.csp.util.log.LogUtil;
import com.jyw.csp.util.string.StringUtils;

/**
 * XML文件操作工具类
 * 
 * @author deyang
 * 
 */
public class XMLUtils {

	public static final String log001 = "无法生成Document对象";
	public static final String log002 = "无法输出XML流";
	public static final String XML_CDATA_START = "<![CDATA[";
	public static final String XML_CDATA_END = "]]>";
	public static Element[] ZERO_LENGTH_ELEMENT = new Element[0];
	public static final DummyErrorHandler DUMMY_ERROR_HANDLER = new DummyErrorHandler();

	public static Document getDocument(File f, boolean validate) {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();

			docBuilderFactory.setValidating(validate);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			docBuilder.setErrorHandler(DUMMY_ERROR_HANDLER);
			doc = docBuilder.parse(f);

			doc.getDocumentElement().normalize();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return doc;
	}

	public static Document getDocument(File f) {
		return getDocument(f, true);
	}

	public static Document getDocument(InputStream is) {
		return getDocument(new InputSource(is));
	}

	public static Document getDocument(Reader r) {
		return getDocument(new InputSource(r));
	}

	public static Document getDocument(String data) {
		return getDocument(new StringReader(data));
	}

	public static Document getDocument(InputSource is) {
		return getDocument(is, false);
	}

	public static Document getDocument(InputSource is, boolean validate) {
		Document doc = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();

			docBuilderFactory.setValidating(validate);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			docBuilder.setErrorHandler(DUMMY_ERROR_HANDLER);
			doc = docBuilder.parse(is);

			doc.getDocumentElement().normalize();
		} catch (Exception err) {
			LogUtil.error(log001, err);
		}
		return doc;
	}

	public static OutputStream writeDocument(Document doc, OutputStream os) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			LogUtil.error(log002, e);
		} catch (TransformerException e) {
			LogUtil.error(log002, e);
		}
		return os;
	}

	public static Element getNextChildElement(Node node) {
		Node n = node.getFirstChild();
		while ((n != null) && (n.getNodeType() != 1))
			n = n.getNextSibling();

		return ((Element) n);
	}

	public static Element getNextSiblingElement(Node node) {
		Node n = node.getNextSibling();
		while ((n != null) && (n.getNodeType() != 1))
			n = n.getNextSibling();

		return ((Element) n);
	}

	public static String getNodeAttributeValue(Node element,
			String attributeName) {
		if (element == null) {
			return null;
		}

		Node tmpNode = element.getAttributes().getNamedItem(attributeName);
		String tmp = null;
		if (tmpNode != null)
			tmp = tmpNode.getNodeValue();
		return tmp;
	}

	public static String getTextData(Node node) {
		if (!(node.hasChildNodes()))
			return null;

		Node child = node.getFirstChild();

		while ((child != null) && (child.getNodeType() != 3)
				&& (child.getNodeType() != 4)) {
			child = child.getNextSibling();
		}

		if (child == null) {
			return null;
		}

		return ((Text) child).getData();
	}

	public static String getCDATATextData(Node node) {
		if (!(node.hasChildNodes()))
			return null;

		Node child = node.getFirstChild();
		while ((child != null) && (child.getNodeType() != 4)) {
			child = child.getNextSibling();
		}

		if (child == null) {
			return null;
		}

		return ((CDATASection) child).getData();
	}

	public static List<Element> childNodeList(Node node, String childNodeName) {
		if ((node == null) || (!(StringUtils.hasText(childNodeName)))) {
			return null;
		}

		List children = new LinkedList();
		Node childNode = node.getFirstChild();
		if (childNode != null) {
			do
				if ((childNode.getNodeType() == 1)
						&& (childNodeName.equals(childNode.getNodeName()))) {
					children.add((Element) childNode);
				}
			while ((childNode = childNode.getNextSibling()) != null);
		}

		return children;
	}

	public static List<Element> childNodeList(Node node) {
		if (node == null)
			return null;

		List children = new LinkedList();
		Node childNode = node.getFirstChild();
		if (childNode != null) {
			do
				if (childNode.getNodeType() == 1)
					children.add((Element) childNode);

			while ((childNode = childNode.getNextSibling()) != null);
		}

		return children;
	}

	public static Element childNodeByTag(Node node, String childNodeName) {
		if ((node == null) || (!(StringUtils.hasText(childNodeName)))) {
			return null;
		}

		Node childNode = node.getFirstChild();
		if (childNode != null) {
			do
				if ((childNode.getNodeType() == 1)
						&& (childNodeName.equals(childNode.getNodeName()))) {
					return ((Element) childNode);
				}
			while ((childNode = childNode.getNextSibling()) != null);
		}

		return null;
	}

	public static String getElementText(String xml, String elementName) {
		if ((!(StringUtils.hasText(xml)))
				|| (!(StringUtils.hasText(elementName)))) {
			return null;
		}

		elementName = StringUtils.trimWhitespace(elementName);

		String elemStartTag = new StringBuilder("<").append(elementName)
				.append('>').toString();

		String elemEndTag = new StringBuilder("</").append(elementName)
				.append('>').toString();

		int pos = xml.indexOf(elemStartTag);
		if (pos == -1)
			return null;

		pos += elemStartTag.length();

		int pos2 = xml.indexOf(elemEndTag);
		if ((pos2 == -1) || (pos2 <= pos)) {
			return null;
		}

		String elemValue = StringUtils.trimWhitespace(xml.substring(pos, pos2));

		if ((elemValue.startsWith("<![CDATA[")) && (elemValue.endsWith("]]>"))) {
			elemValue = elemValue.substring("<![CDATA[".length(),
					elemValue.length() - "]]>".length());
		}

		return elemValue;
	}

	public static String setElementText(String xml, String elementName,
			String value, boolean isCDATA) {
		if ((!(StringUtils.hasText(xml)))
				|| (!(StringUtils.hasText(elementName)))) {
			return xml;
		}

		elementName = StringUtils.trimWhitespace(elementName);

		String elemStart = new StringBuilder().append("<").append(elementName)
				.toString();
		int elemStartLength = elemStart.length();
		String elemEndTag = new StringBuilder("</").append(elementName)
				.append('>').toString();

		int elemEndTagLength = elemEndTag.length();

		int pos = xml.indexOf(elemStart);

		int pos2 = -1;

		int pos3 = -1;
		while (pos != -1) {
			int idx = pos + elemStartLength;
			if ((xml.charAt(idx) != '>') && (xml.charAt(idx) != ' ')
					&& (!(xml.substring(idx, idx + 2).equals("/>")))) {
				pos = xml.indexOf(elemStart, idx);
			} else {
				int idx2 = xml.indexOf(">", idx);
				if (idx2 == -1) {
					pos = -1;
					break;
				}
				if (xml.charAt(idx2 - 1) == '/') {
					pos2 = idx2 - 1;

					pos3 = idx2 + 1;
					break;
				}

				int idx3 = xml.indexOf(elemEndTag, idx2 + 1);
				if (idx3 == -1) {
					pos = -1;
				} else {
					pos2 = idx2;
					pos3 = idx3 + elemEndTagLength;
				}

				break;
			}
		}

		if (pos == -1) {
			return xml;
		}

		StringBuilder sbXml = new StringBuilder(StringUtils.trimWhitespace(xml
				.substring(0, pos2)));

		if (value != null) {
			sbXml.append('>');
			if (isCDATA) {
				sbXml.append("<![CDATA[").append(value).append("]]>");
			} else {
				sbXml.append(value);
			}

			sbXml.append(elemEndTag);
		} else {
			sbXml.append(" />");
		}

		return sbXml.append(xml.substring(pos3)).toString();
	}

    public static String getNodeValue(Element element, String nodeName) {
        String nodeValue = "";
        NodeList nodeList = element.getChildNodes();
        for (int i = 0, length = nodeList.getLength(); i < length; i++) {
            Node node = nodeList.item(i);
            if (nodeName.equals(node.getNodeName())) {
                node = node.getFirstChild();
                if (node != null) {
                    nodeValue = node.getNodeValue();
                }
                break;
            }
        }
        return nodeValue;
    }

	public static class DummyErrorHandler implements ErrorHandler {
		public void fatalError(SAXParseException err) throws SAXException {
			throw err;
		}

		public void error(SAXParseException err) throws SAXException {
			throw err;
		}

		public void warning(SAXParseException err) throws SAXException {
			throw err;
		}
	}
}
