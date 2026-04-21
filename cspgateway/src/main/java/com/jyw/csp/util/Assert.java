package com.jyw.csp.util;

import java.util.Collection;
import java.util.Map;

import com.jyw.csp.exception.AssertException;
import com.jyw.csp.util.string.StringUtils;

public abstract class Assert {
	public static void isTrue(boolean expression, String message) {
		if (!(expression))
			throwAssertException(message);
	}

	public static void isTrue(boolean expression) {
		isTrue(expression, "该表达式必须为true");
	}

	public static void isNull(Object object, String message) {
		if (object != null)
			throwAssertException(message);
	}

	public static void isNull(Object object) {
		isNull(object, "该对象必须为空");
	}

	public static void notNull(Object object, String message) {
		if (object == null)
			throwAssertException(message);
	}

	public static void notNull(Object object) {
		notNull(object, "该对象不能为空");
	}

	public static void hasLength(String text, String message) {
		if (!(StringUtils.hasLength(text)))
			throwAssertException(message);
	}

	public static void hasLength(String text) {
		hasLength(text, "该字符串的长度必须大于0");
	}

	public static void hasText(String text, String message) {
		if (!(StringUtils.hasText(text)))
			throwAssertException(message);
	}

	public static void hasText(String text) {
		hasText(text, "该字符串必须包含内容");
	}

	public static void doesNotContain(String textToSearch, String substring,
			String message) {
		if ((StringUtils.hasLength(textToSearch))
				&& (StringUtils.hasLength(substring))
				&& (textToSearch.indexOf(substring) != -1)) {
			throwAssertException(message);
		}
	}

	public static void doesNotContain(String textToSearch, String substring) {
		doesNotContain(textToSearch, substring,
				new StringBuilder().append("该字符串不能包含 [").append(substring)
						.append("]").toString());
	}

	public static void notEmpty(Object[] array, String message) {
		if ((array == null) || (array.length == 0))
			throwAssertException(message);
	}

	public static void notEmpty(Object[] array) {
		notEmpty(array, "该数组不能为空");
	}

	public static void noNullElements(Object[] array, String message) {
		if (array != null)
			for (int i = 0; i < array.length; ++i)
				if (array[i] == null)
					throwAssertException(message);
	}

	public static void noNullElements(Object[] array) {
		noNullElements(array, "该数组不能包含空元素");
	}

	public static <E> void notEmpty(Collection<E> collection, String message) {
		if ((collection == null) || (collection.isEmpty()))
			throwAssertException(message);
	}

	public static <E> void notEmpty(Collection<E> collection) {
		notEmpty(collection, "该集合不能为空");
	}

	public static <K, V> void notEmpty(Map<K, V> map, String message) {
		if ((map == null) || (map.isEmpty()))
			throwAssertException(message);
	}

	public static <K, V> void notEmpty(Map<K, V> map) {
		notEmpty(map, "该Map不能为空");
	}

	public static <T> void isInstanceOf(Class<T> clazz, Object obj) {
		isInstanceOf(clazz, obj, "");
	}

	public static <T> void isInstanceOf(Class<T> type, Object obj,
			String message) {
		notNull(type, "该类型不能为空");
		if (!(type.isInstance(obj)))
			throwAssertException(new StringBuilder().append(message)
					.append("类型为[")
					.append((obj != null) ? obj.getClass().getName() : "null")
					.append("]的对象必须是类型[").append(type).append("]的实例")
					.toString());
	}

	public static <SUP, SUB> void isAssignable(Class<SUP> superType,
			Class<SUB> subType) {
		isAssignable(superType, subType, "");
	}

	public static <SUP, SUB> void isAssignable(Class<SUP> superType,
			Class<SUB> subType, String message) {
		notNull(superType, "该类型不能为空");
		if ((subType == null) || (!(superType.isAssignableFrom(subType))))
			throwAssertException(new StringBuilder().append(message)
					.append("[").append(subType).append("]不能指派为[")
					.append(superType).append("]").toString());
	}

	private static void throwAssertException(String message) {
		throw new AssertException("XDCF300100AK", message);
	}
}
