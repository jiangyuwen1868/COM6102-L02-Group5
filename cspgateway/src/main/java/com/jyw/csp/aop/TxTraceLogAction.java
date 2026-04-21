package com.jyw.csp.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TxTraceLogAction {

	String value() default "记录交易报文文件日志及数据库日志";
}
