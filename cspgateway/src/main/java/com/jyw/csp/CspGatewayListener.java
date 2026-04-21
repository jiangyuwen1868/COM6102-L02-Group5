package com.jyw.csp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(-100)
public class CspGatewayListener implements ApplicationListener<ServletWebServerInitializedEvent>{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void onApplicationEvent(ServletWebServerInitializedEvent event) {
		int port = event.getWebServer().getPort();
		CspGatewayApplication.webServerPort = port;
		logger.debug("----------CspGatewayListener getWebServer Port:" + port);
	}
	
}
