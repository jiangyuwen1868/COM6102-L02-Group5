package com.jyw.csp.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

//用来获取springboot创建好的工厂
@Component // 此类必须交给springboot管理
public class ApplicationContextUtils implements ApplicationContextAware {

	// 保留下来工厂
	private static org.springframework.context.ApplicationContext applicationContext;

	// 将创建好工厂以参数形式传递给这个类
	public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) throws BeansException {
		ApplicationContextUtils.applicationContext = applicationContext;
	}
	// 根据类在工厂的唯一id从spring容器中获取javabean
	public static Object getBeanById(String beanName) {
		return applicationContext.getBean(beanName);
	}

	// 根据class的名字从spring容器中获取javabean
	public static <T> T getBeanByName(Class<T> targetClass) {
		return applicationContext.getBean(targetClass);
	}
}

