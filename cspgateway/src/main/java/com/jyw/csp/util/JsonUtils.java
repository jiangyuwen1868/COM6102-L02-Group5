package com.jyw.csp.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;

public class JsonUtils {
	protected static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
	private static final ValueFilter FILTER = new ValueFilter() {
		@Override
		public Object process(Object obj, String s, Object v) {
			if (v == null)
				return "";
			return v;
		}
	};

	public static String toJSONString(JSONObject json) {
		return JSON.toJSONString(json, FILTER);
	}

	public static String formatJson(String jsonStr){
		return formatJson(jsonStr,-1);
	}

	public static String formatJson(JSONObject json){
		return formatJson(json,-1);
	}

	public static String formatJson(Object obj){
		return formatJson(obj,-1);
	}

	public static String formatJson(String jsonStr, int maxLength) {
		return formatJson(jsonStr,"",maxLength);
	}

	public static String formatJson(JSONObject json, int maxLength) {
		return formatJson(json,"",maxLength);
	}

	public static String formatJson(Object obj, int maxLength) {
		JSONObject json = (JSONObject) JSON.toJSON(obj);
		return formatJson(json,"",maxLength);
	}

	public static String formatJson(String jsonStr,String headAdd , int maxLength) {
		if (null == jsonStr || "".equals(jsonStr))
			return "null";

		try {
			JSONObject json = JSONObject.parseObject(jsonStr);
			return formatJson(json,headAdd,maxLength);
		} catch (Exception e) {
			logger.warn(e.toString(), e);
			return jsonStr;
		}
	}

	public static String formatJson(JSONObject json,String headAdd , int maxLength) {
		if (null == json )
			return "null";

		String jsonStr = toJSONString(json);
		//String jsonStr = json.toString();

		StringBuilder sb = new StringBuilder();
		sb.append(headAdd);
		char last = '\0';
		char current = '\0';
		int indent = 0;
		boolean isInQuotationMarks = false;
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
//			System.out.print("["+i+"]-");
//			System.out.println(current);
			switch (current) {
			case '"':
				if (last != '\\') {
					isInQuotationMarks = !isInQuotationMarks;
				}
				sb.append(getStringBuilderString(tmp,maxLength));
				sb.append(current);
				break;
			case '{':
			case '[':
				sb.append(getStringBuilderString(tmp,maxLength));
				sb.append(current);
				if (!isInQuotationMarks) {
					sb.append('\n'+headAdd);
					indent++;
					addIndentBlank(sb, indent);
				}
				break;
			case '}':
			case ']':
				//System.out.println(current+"--> isInQuotationMarks:"+isInQuotationMarks);
				if (!isInQuotationMarks) {
					sb.append(getStringBuilderString(tmp,maxLength));
					sb.append('\n'+headAdd);
					indent--;
					addIndentBlank(sb, indent);
				}
				
				sb.append(current);
				break;
			case ',':
				sb.append(getStringBuilderString(tmp,maxLength));
				sb.append(current);
				if (last != '\\' && !isInQuotationMarks) {
					sb.append('\n'+headAdd);
					addIndentBlank(sb, indent);
				}
				break;
			default:
				tmp.append(current);
				//sb.append(current);
			}
			
//			System.out.println(sb.toString());
//			System.out.println(tmp.toString());
//			System.out.println("---------------------------------------------------------");
		}

		return sb.toString();
	}
	
	public static String formatJsonObject(JSONObject json,String inJsonName,int maxLength) {
		if(json == null)
			return "";
		try {
			StringBuilder sb = new StringBuilder();
			
			boolean startFlag = true;
			for(Map.Entry<String, Object> entry :json.entrySet()) {
				String key = entry.getKey();
				if(startFlag) {
					startFlag = false;
					sb.append("{\n");
				}else {
					sb.append(",\n");
				}
				if(key.equals(inJsonName)) {
					sb.append("\t\"").append(key).append("\":\n\t\t\"\n").append(formatJson((String)entry.getValue(),"\t\t",maxLength)).append("\n\t\t\"");
				}else{
					sb.append("\t\"").append(key).append("\":\"").append(entry.getValue()).append("\"");
				}
			}
			sb.append("\n}");
			return sb.toString();
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return json.toString();
	}
	
	public static String formatJsonObjectString(String jsonString,String inJsonName,int maxLength) {
		try {
			JSONObject json = JSONObject.parseObject(jsonString);
			return formatJsonObject(json,inJsonName,maxLength);
		} catch (Exception e) {
			logger.error(e.toString(),e);
			return jsonString;
		}
	}
	
	private static String getStringBuilderString(StringBuilder sb,int maxLength) {
		if(sb == null)
			return "null";
		
		String result = null;
		
		if(maxLength < 0) {
			result = sb.toString();
		}else {
			if(sb.length() > maxLength) {
				result = "|$| omitted print length "+sb.length() + " |$|";
			}else {
				result = sb.toString();
			}
		}
		
		sb.delete(0, sb.length());
		return result;
	}

	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append('\t');
		}
	}
	
}
