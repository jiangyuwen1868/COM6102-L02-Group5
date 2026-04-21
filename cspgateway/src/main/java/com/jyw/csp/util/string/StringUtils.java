package com.jyw.csp.util.string;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.jyw.csp.util.Utils;


/**
 * 字符串工具操作类
 * @author deyang
 *
 */
public final class StringUtils {

	public static final String EMPTY_STRING = "";
	public static final char DOT = 46;
	public static final char UNDERSCORE = 95;
	public static final String COMMA_SPACE = ", ";
	public static final String COMMA = ",";
	public static final String OPEN_PAREN = "(";
	public static final String CLOSE_PAREN = ")";
	public static final char SINGLE_QUOTE = 39;
	private static final DecimalFormat df = new DecimalFormat("####0.00");

	public static boolean checkEmpty(String str) {
		return ((str == null) || ("".equals(str.trim())));
	}
	
	public static boolean hasLength(CharSequence str) {
		return ((str != null) && (str.length() > 0));
	}

	public static boolean hasLength(String str) {
		return hasLength((CharSequence)str);
	}

	public final static int getBiolInfoTableID(String custId) {
		int a = byteArrayToInt(custId.getBytes());
		return Math.abs(a) % 100 + 1;
	}
	public final static int byteArrayToInt(byte[] b) {
		int value = 0;
		if(b==null)return value;
		for (int i = 0; i < b.length; i++) {
			int temp = b[i] & 0xFF;
			value = value * 256;
			value = value + temp;
		}
		return value;
	}
	public static boolean hasText(CharSequence str) {
		if (!(hasLength(str)))
			return false;

		int strLen = str.length();
		for (int i = 0; i < strLen; ++i)
			if (!(Character.isWhitespace(str.charAt(i))))
				return true;

		return false;
	}

	public static boolean hasText(String str) {
		return hasText((CharSequence)str);
	}

	public static boolean containsWhitespace(CharSequence str)
	  {
	    if (!(hasLength(str)))
	      return false;

	    int strLen = str.length();
	    for (int i = 0; i < strLen; ++i)
	      if (Character.isWhitespace(str.charAt(i)))
	        return true;


	    return false;
	  }

	  public static boolean containsWhitespace(String str)
	  {
	    return containsWhitespace(str);
	  }

	  public static String trimWhitespace(String str)
	  {
	    if (!(hasLength(str)))
	      return str;

	    StringBuilder sb = new StringBuilder(str);
	    while ((sb.length() > 0) && (Character.isWhitespace(sb.charAt(0))))
	      sb.deleteCharAt(0);

	    while ((sb.length() > 0) && (Character.isWhitespace(sb.charAt(sb.length() - 1))))
	      sb.deleteCharAt(sb.length() - 1);

	    return sb.toString();
	  }
	  
	public static String join(String seperator, String[] strings) {
		int length = strings.length;
		if (length == 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder(length * strings[0].length())
				.append(strings[0]);

		for (int i = 1; i < length; ++i)
			builder.append(seperator).append(strings[i]);

		return builder.toString();
	}

	public static String join(String seperator, Iterator<?> objects) {
		StringBuilder builder = new StringBuilder();
		builder.append(objects.next());
		while (objects.hasNext())
			builder.append(seperator).append(objects.next());

		return builder.toString();
	}

	public static String[] add(String[] x, String sep, String[] y) {
		String[] result = new String[x.length];
		for (int i = 0; i < x.length; ++i)
			result[i] = new StringBuilder().append(x[i]).append(sep)
					.append(y[i]).toString();

		return result;
	}

	public static String repeat(String string, int times) {
		StringBuilder builder = new StringBuilder(string.length() * times);
		for (int i = 0; i < times; ++i)
			builder.append(string);
		return builder.toString();
	}

	public static String replace(String template, String placeholder,
			String replacement) {
		return replace(template, placeholder, replacement, false);
	}

	public static String replace(String template, String placeholder,
			String replacement, boolean wholeWords) {
		int loc = template.indexOf(placeholder);
		if (loc < 0)
			return template;

		boolean actuallyReplace = (!(wholeWords))
				|| (loc + placeholder.length() == template.length())
				|| (!(Character.isJavaIdentifierPart(template.charAt(loc
						+ placeholder.length()))));

		String actualReplacement = (actuallyReplace) ? replacement
				: placeholder;

		return new StringBuffer(template.substring(0, loc))
				.append(actualReplacement)
				.append(replace(template.substring(loc + placeholder.length()),
						placeholder, replacement, wholeWords)).toString();
	}

	public static String replaceOnce(String template, String placeholder,
			String replacement) {
		int loc = template.indexOf(placeholder);
		if (loc < 0)
			return template;

		return new StringBuffer(template.substring(0, loc)).append(replacement)
				.append(template.substring(loc + placeholder.length()))
				.toString();
	}

	public static String[] split(String seperators, String list) {
		return split(seperators, list, false);
	}

	public static String[] split(String seperators, String list, boolean include) {
		StringTokenizer tokens = new StringTokenizer(list, seperators, include);
		String[] result = new String[tokens.countTokens()];
		int i = 0;
		while (tokens.hasMoreTokens())
			result[(i++)] = tokens.nextToken();

		return result;
	}

	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, ".");
	}

	public static String unqualify(String qualifiedName, String seperator) {
		return qualifiedName
				.substring(qualifiedName.lastIndexOf(seperator) + 1);
	}

	public static String qualifier(String qualifiedName) {
		int loc = qualifiedName.lastIndexOf(".");
		if (loc < 0)
			return "";

		return qualifiedName.substring(0, loc);
	}

	public static String[] suffix(String[] columns, String suffix) {
		if (suffix == null)
			return columns;
		String[] qualified = new String[columns.length];
		for (int i = 0; i < columns.length; ++i)
			qualified[i] = suffix(columns[i], suffix);

		return qualified;
	}

	public static String suffix(String name, String suffix) {
		return ((suffix == null) ? name : new StringBuilder().append(name)
				.append(suffix).toString());
	}

	public static String[] prefix(String[] columns, String prefix) {
		if (prefix == null)
			return columns;
		String[] qualified = new String[columns.length];
		for (int i = 0; i < columns.length; ++i)
			qualified[i] = new StringBuilder().append(prefix)
					.append(columns[i]).toString();

		return qualified;
	}

	public static String root(String qualifiedName) {
		int loc = qualifiedName.indexOf(".");
		return ((loc < 0) ? qualifiedName : qualifiedName.substring(0, loc));
	}

	/**
	 * 字符转译为boolean类型， true,t,y,1不区分大小写均为true，其他均为false
	 * @param tfString
	 * @return
	 */
	public static boolean booleanValue(String tfString) {
		String trimmed = tfString.trim().toLowerCase();
		return ((trimmed.equals("true")) || (trimmed.equals("t")) || (trimmed.equals("y")) || (trimmed.equals("1")));
	}

	public static String toString(Object[] array) {
		int len = array.length;
		if (len == 0)
			return "";
		StringBuffer buf = new StringBuffer(len * 12);
		for (int i = 0; i < len - 1; ++i)
			buf.append(array[i]).append(", ");

		return buf.append(array[(len - 1)]).toString();
	}

	public static String[] multiply(String string, Iterator<?> placeholders,
			Iterator<?> replacements) {
		String[] result = { string };
		while (placeholders.hasNext()) {
			result = multiply(result, (String) placeholders.next(),
					(String[]) (String[]) replacements.next());
		}

		return result;
	}

	public static String[] multiply(String[] strings, String placeholder,
			String[] replacements) {
		String[] results = new String[replacements.length * strings.length];
		int n = 0;
		for (int i = 0; i < replacements.length; ++i) {
			for (int j = 0; j < strings.length; ++j)
				results[(n++)] = replaceOnce(strings[j], placeholder,
						replacements[i]);

		}

		return results;
	}

	public static int count(String string, char character) {
		int n = 0;
		for (int i = 0; i < string.length(); ++i)
			if (string.charAt(i) == character)
				++n;

		return n;
	}

	public static int countUnquoted(String string, char character) {
		if ('\'' == character) {
			throw new IllegalArgumentException(
					"Unquoted count of quotes is invalid");
		}

		int count = 0;
		int stringLength = (string == null) ? 0 : string.length();
		boolean inQuote = false;
		for (int indx = 0; indx < stringLength; ++indx)
			if (inQuote)
				if ('\'' == string.charAt(indx))
					inQuote = false;

				else if ('\'' == string.charAt(indx))
					inQuote = true;
				else if (string.charAt(indx) == character)
					++count;

		return count;
	}

	public static boolean isNotEmpty(String string) {
		return ((string != null) && (string.length() > 0));
	}

	public static String qualify(String prefix, String name) {
		return new StringBuilder(prefix.length() + name.length() + 1)
				.append(prefix).append('.').append(name).toString();
	}

	public static String[] qualify(String prefix, String[] names) {
		if (prefix == null)
			return names;
		int len = names.length;
		String[] qualified = new String[len];
		for (int i = 0; i < len; ++i)
			qualified[i] = qualify(prefix, names[i]);

		return qualified;
	}

	public static int firstIndexOfChar(String sqlString, String string,
			int startindex) {
		int matchAt = -1;
		for (int i = 0; i < string.length(); ++i) {
			int curMatch = sqlString.indexOf(string.charAt(i), startindex);
			if (curMatch >= 0)
				if (matchAt == -1)
					matchAt = curMatch;
				else
					matchAt = Math.min(matchAt, curMatch);

		}

		return matchAt;
	}

	public static String truncate(String string, int length) {
		if (string.length() <= length)
			return string;

		return string.substring(0, length);
	}

	public static String toIntStr(String floatStr) {
		if ((floatStr == null) || (floatStr.length() < 1))
			return "0";

		int index = floatStr.indexOf(".");
		if (index == -1)
			return floatStr;
		if (index == 0)
			return "0";
		if ((index == 1) && (floatStr.substring(0, 1).equals("-")))
			return "0";

		String intStr = floatStr.substring(0, index);
		if (intStr.substring(0, 1).equals("-")) {
			long tmp = Long.parseLong(intStr) - 1L;
			intStr = new StringBuilder().append(tmp).append("").toString();
		}
		return intStr;
	}

	public static String toFmtDoubleStr(String doubleStr) {
		return toFmtDoubleStr(Double.parseDouble(doubleStr));
	}

	public static String toFmtDoubleStr(double d) {
		return df.format(d);
	}
	
	/*
     * 中文转unicode编码
     */
    public static String unicodeEncoding(final String gbString) {
    	try {
	        char[] utfBytes = gbString.toCharArray();
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < utfBytes.length; i++) {
	            String hexB = Integer.toHexString(utfBytes[i]);
	            
	            sb.append("\\u" + Utils.fillCharsToStringLeft(hexB, (char)48, 4));
	        }
	        return sb.toString();
    	} catch(Exception e) {
    		e.printStackTrace();
    		return gbString;
    	}
    }
    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
    	try {
	        int start = 0;
	        int end = 0;
	        final StringBuffer buffer = new StringBuffer();
	        while (start > -1) {
	            end = dataStr.indexOf("\\u", start + 2);
	            String charStr = "";
	            if (end == -1) {
	                charStr = dataStr.substring(start + 2, dataStr.length());
	            } else {
	                charStr = dataStr.substring(start + 2, end);
	            }
	            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
	            buffer.append(new Character(letter).toString());
	            start = end;
	        }
	        return buffer.toString();
    	} catch(Exception e) {
    		e.printStackTrace();
    		return dataStr;
    	}
    }
    
    public static String join(Object[] array, String separator) {
		if (array == null)
			return null;

		if (separator == null)
			separator = "";

		int arraySize = array.length;
		int bufSize = (arraySize == 0) ? 0 : arraySize * (((array[0] == null) ? 16 : array[0].toString().length()) + ((separator != null) ? separator.length() : 0));
		StringBuffer buf = new StringBuffer(bufSize);
		for (int i = 0; i < arraySize; ++i) {
			if ((separator != null) && (i > 0))
				buf.append(separator);

			if (array[i] != null)
				buf.append(array[i]);
		}

		return buf.toString();
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否是空的
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否是空的
     */
    public static boolean isEmpty(char[] str) {
        return str == null || str.length == 0;
    }


    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不是空的
     */
    public static boolean isNotEmpty(char[] str) {
        return !isEmpty(str);
    }

    /**
     * 合并byte[]
     *
     * @param bts 字节数组
     * @return 合并后的字节
     */
    public static char[] merger(char[]... bts) {
        int lenght = 0;
        for (char[] b : bts) {
            lenght += b.length;
        }

        char[] bt = new char[lenght];
        int lastLength = 0;
        for (char[] b : bts) {
            System.arraycopy(b, 0, bt, lastLength, b.length);
            lastLength += b.length;
        }
        return bt;
    }

    /**
     * 字节转char数组
     *
     * @param bytes 字节数组
     * @return chars
     */
    public static char[] toChars(byte[] bytes) {
        byte[] bytes0 = new byte[bytes.length];
        System.arraycopy(bytes, 0, bytes0, 0, bytes.length);
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    /**
     * 字符数组转成字节数组
     *
     * @param chars 字符数组
     * @return 字节数组
     */
    public static byte[] toBytes(char[] chars) {
        char[] chars0 = new char[chars.length];
        System.arraycopy(chars, 0, chars0, 0, chars.length);
        CharBuffer charBuffer = CharBuffer.wrap(chars0);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    /**
     * char数组比较
     *
     * @param char1 char1
     * @param char2 char2
     * @return 是否相等
     */
    public static boolean equal(char[] char1, char[] char2) {
        if (char1.length != char2.length) {
            return false;
        }

        for (int i = 0; i < char1.length; i++) {
            if (char1[i] != char2[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * 字符串是否包含数组中的任1元素
     *
     * @param array 数组
     * @param str   包含的字串
     * @return 是否
     */
    public static boolean containsArray(String str, String[] array) {
        for (String e : array) {
            if (str.contains(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换成字符串
     *
     * @param chars 字符数组
     * @return 字符串
     */
    public static String toCharArrayCode(char[] chars) {
        List<Integer> list = new ArrayList<>();
        for (char c : chars) {
            list.add((int) c);
        }
        return list.toString().replace("[", "{").replace("]", "}");
    }

    /**
     * 在字符串的某个位置插入字符串
     *
     * @param arrayStr  字符串数组
     * @param insertStr 要插入的字串
     * @param pos       位置开始标识
     * @return 插入后的字串
     */
    public static String insertStringArray(String[] arrayStr, String insertStr, String pos) {
        StringBuffer newStr = new StringBuffer();
        boolean isInsert = false;
        for (int i = 0; i < arrayStr.length; i++) {
            newStr.append(arrayStr[i]).append("\r\n");
            if (arrayStr[i].startsWith(pos)) {
                newStr.append(insertStr).append("\r\n");
                isInsert = true;
            }
        }
        if (!isInsert) {
            newStr.append(insertStr).append("\r\n");
        }
        return newStr.toString();
    }

    /**
     * 通配符匹配
     *
     * @param match      匹配字符串
     * @param testString 待匹配字符窜
     * @return 是否匹配
     */
    public static boolean isMatch(String match, String testString) {
        String regex = match.replaceAll("\\?", "(.?)")
                .replaceAll("\\*+", "(.*?)");
        return Pattern.matches(regex, testString);
    }

    /**
     * 判断是否是匹配
     *
     * @param matches    匹配的
     * @param testString 要判断
     * @return 是否属于
     */
    public static boolean isMatchs(List<String> matches, String testString) {
        return isMatchs(matches, testString, false);
    }

    public static boolean isMatchs(List<String> matches, String testString, boolean dv) {
        if (matches == null || matches.size() == 0) {
            return dv;
        }

        for (String m : matches) {
            if (StringUtils.isMatch(m, testString) || testString.startsWith(m) || testString.endsWith(m)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * * 判断一个对象是否非空
	 *
	 * @param object Object
	 * @return true：非空 false：空
	 */
	public static boolean isNotNull(Object object)
	{
		return !isNull(object);
	}

	/**
	 * * 判断一个对象是否为空
	 *
	 * @param object Object
	 * @return true：为空 false：非空
	 */
	public static boolean isNull(Object object)
	{
		return object == null;
	}
}
