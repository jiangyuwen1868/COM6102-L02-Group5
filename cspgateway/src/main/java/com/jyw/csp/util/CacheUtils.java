package com.jyw.csp.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.util.string.StringUtils;

public class CacheUtils {

	private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap(
			1000);

	private static final Map<String, Charset> CHARSET_CACHE = new HashMap();
	private static final Map<String, Locale> LOCALE_CACHE = new HashMap();

	public static Class<?> getClassForName(String className) {
		Class targetClass = (Class) CLASS_CACHE.get(className);
		if (targetClass == null)
			try {
				targetClass = forName(className, Thread.currentThread()
						.getContextClassLoader());

				CLASS_CACHE.put(className, targetClass);
			} catch (Throwable t) {
				throw new CommonRuntimeException("XATF300100AA", "类名："
						+ className, t);
			}

		return targetClass;
	}

	public static Charset getCharset(String charsetName) {
		if (!(StringUtils.hasText(charsetName))) {
			return null;
		}

		charsetName = charsetName.toUpperCase();
		Charset targetCharset = (Charset) CHARSET_CACHE.get(charsetName);
		if (targetCharset == null)
			try {
				targetCharset = Charset.forName(charsetName);
				CHARSET_CACHE.put(charsetName, targetCharset);
			} catch (Exception e) {
				e.printStackTrace();
			}

		return targetCharset;
	}

	public static Locale getLocale(String language, String country) {
		if (!(StringUtils.hasText(language)))
			return null;

		language = language.toLowerCase(Locale.ENGLISH);

		if (!(StringUtils.hasText(country)))
			country = "";
		else {
			country = country.toUpperCase(Locale.ENGLISH);
		}

		String localeKey = language + '_' + country + '_';

		Locale targetLocale = (Locale) LOCALE_CACHE.get(localeKey);
		if (targetLocale == null) {
			targetLocale = new Locale(language, country);
			LOCALE_CACHE.put(localeKey, targetLocale);
		}

		return targetLocale;
	}

	public static Locale getLocale(String language) {
		return getLocale(language, null);
	}

	private static Class<?> forName(String name, ClassLoader classLoader)
			throws ClassNotFoundException, LinkageError {
		if (classLoader == null) {
			classLoader = getDefaultClassLoader();
		}
		return classLoader.loadClass(name);
	}

	private static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
		}
		if (cl == null) {
			cl = CacheUtils.class.getClassLoader();
		}
		return cl;
	}
}
