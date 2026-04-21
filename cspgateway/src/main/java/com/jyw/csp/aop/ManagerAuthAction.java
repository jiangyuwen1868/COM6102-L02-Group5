package com.jyw.csp.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ManagerAuthAction {

	String value() default "管理操作安全认证";
}
