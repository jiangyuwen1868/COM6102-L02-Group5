package com.jyw.csp.interceptores;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jyw.csp.constant.CspConstants;
import com.jyw.csp.context.SessionContext;

@Component
public class GlobalInterceptor implements HandlerInterceptor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("==========GlobalInterceptor->preHandle");

        SessionContext context = SessionContext.getCurrentContext();
        context.setSysRecvTime(System.currentTimeMillis());
        
        StopWatch sw = new StopWatch(CspConstants.CSP_COSTTIME_INFO_STOPWATCH);
        
        context.set(CspConstants.CSP_COSTTIME_INFO_STOPWATCH, sw);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("==========GlobalInterceptor->postHandle");

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("==========GlobalInterceptor->afterCompletion");

        SessionContext.getCurrentContext().release();

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
