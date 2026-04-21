package com.jyw.csp.configuration;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventorListener;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public ServletListenerRegistrationBean<ClassLoaderLeakPreventorListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<ClassLoaderLeakPreventorListener> servletListenerRegistrationBean = new ServletListenerRegistrationBean<ClassLoaderLeakPreventorListener>();
        servletListenerRegistrationBean.setListener(new ClassLoaderLeakPreventorListener());
        return servletListenerRegistrationBean;
    }
}
