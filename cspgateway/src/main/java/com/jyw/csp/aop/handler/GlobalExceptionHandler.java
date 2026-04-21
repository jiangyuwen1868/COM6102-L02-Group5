package com.jyw.csp.aop.handler;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jyw.csp.context.SessionContext;
import com.jyw.csp.datatransform.message.gw.GwResponseMsg;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.exception.CommonException;
import com.jyw.csp.exception.CommonRuntimeException;
import com.jyw.csp.monitor.MonitorFilter;
import com.jyw.csp.util.Utils;

@RestControllerAdvice
public class GlobalExceptionHandler {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(CommonException.class)
    @ResponseBody
    public GwResponseMsg messageExceptionHandler(CommonException e) {
    	logger.debug("-------------------CommonException---------------------");
        SessionContext context = SessionContext.getCurrentContext();
        //GwRequestMsg requestMsg = context.getGwRequestMsg();

        GwResponseMsg GwResponseMsg = new GwResponseMsg();
        GwResponseMsg.setReturn_code(e.getCode());
        GwResponseMsg.setReturn_message(e.getMessage());
        GwResponseMsg.setSys_evt_trace_id(Utils.GUID());
        GwResponseMsg.setSrv_costtime(String.valueOf(context.getSysRespTime() - context.getSysRecvTime()));

        return GwResponseMsg;
    }

    @ExceptionHandler(CommonRuntimeException.class)
    @ResponseBody
    public GwResponseMsg messageParseExceptionHandler(CommonRuntimeException e) {
    	logger.debug("-------------------CommonRuntimeException---------------------");
    	SessionContext context = SessionContext.getCurrentContext();
        GwResponseMsg GwResponseMsg = new GwResponseMsg();
        GwResponseMsg.setReturn_code(e.getCode());
        GwResponseMsg.setReturn_message(e.getMessage());
        GwResponseMsg.setSys_evt_trace_id(Utils.GUID());
        GwResponseMsg.setSrv_costtime(String.valueOf(context.getSysRespTime() - context.getSysRecvTime()));

        return GwResponseMsg;
    }
    
    @ExceptionHandler(BeanCreationException.class)
    public void messageParseExceptionHandler(BeanCreationException e) {
    	logger.debug("-------------------BeanCreationException---------------------");
    }
    
    @ExceptionHandler({ SQLException.class })
    @ResponseBody
    public GwResponseMsg databaseError(Exception exception) {
    	logger.debug("-------------------CommonRuntimeException---------------------");
    	MonitorFilter.addSysError();
    	SessionContext context = SessionContext.getCurrentContext();
        GwResponseMsg GwResponseMsg = new GwResponseMsg();
        GwResponseMsg.setReturn_code(ErrorInfo.DB_ERROR.getCode());
        GwResponseMsg.setReturn_message(ErrorInfo.DB_ERROR.getInfo());
        GwResponseMsg.setSys_evt_trace_id(Utils.GUID());
        GwResponseMsg.setSrv_costtime(String.valueOf(context.getSysRespTime() - context.getSysRecvTime()));

        return GwResponseMsg;
    }
}
