package com.jyw.csp.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.fastjson.JSONObject;
import com.jyw.csp.entity.CspSysDeployEntity;
import com.jyw.csp.enums.ErrorInfo;
import com.jyw.csp.service.CspSysDeployService;
import com.jyw.csp.util.Utils;

@Component
@Aspect // 声明一个切面
public class ManagerAuthAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CspSysDeployService sysDeployService;
	
	@Pointcut("@annotation(com.jyw.csp.aop.ManagerAuthAction)") // 声明一个切点
	public void ManagerAuthPointCut() {
	};
	
	@Around("ManagerAuthPointCut()")
	public Object doAroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
		logger.debug("==========ManagerAuthAspect->doAround begin");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("errorCode", ErrorInfo.SUCCESS.getCode());
		result.put("errorMsg", ErrorInfo.SUCCESS.getInfo());
		try {
			List<CspSysDeployEntity> list = sysDeployService.getAll();
			if(list==null) {
				result.put("errorCode", "MNG300009999");
				result.put("errorMsg", "无部署资源列表");
				return (JSONObject) JSONObject.toJSON(result); 
			}
			
			//获取RequestAttributes
	        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
	        //从获取RequestAttributes中获取HttpServletRequest的信息
	        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
	        
	        //HttpSession session = (HttpSession) requestAttributes.resolveReference(RequestAttributes.REFERENCE_SESSION);
			
			String clientIp = Utils.getClientIpAddr(request);
			String serverName = request.getServerName();
			
			logger.debug("request clientIp:" + clientIp);
			
			boolean isPass = false;
			for(CspSysDeployEntity entity : list) {
				String deployIp = entity.getSysip();
				if(deployIp.equals(clientIp)||deployIp.equals(serverName)) {
					isPass = true;
					break;
				}
			}
			
			if(isPass) {
				return pjp.proceed();
			} else {
				result.put("errorCode", "MNG310009999");
				result.put("errorMsg", "非法操作");
			}
			
		} catch (Throwable e) {
			logger.error("ManagerAuthAction", e);
			result.put("errorCode", "MNG200009999");
			result.put("errorMsg", "非法操作:" + e.getMessage());
		}
		
		return (JSONObject) JSONObject.toJSON(result); 
	}
}
