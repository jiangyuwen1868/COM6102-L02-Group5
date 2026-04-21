package com.jyw.csp.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TxAuthAction {

	String value() default "交易认证授权验证";
}
