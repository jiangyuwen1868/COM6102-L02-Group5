package com.jyw.csp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;

import com.jyw.csp.queue.TranceLogQueue;
import com.jyw.csp.queue.TransConsumeThread;

@SpringBootApplication
@ComponentScan(excludeFilters= {@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, classes = {TranceLogQueue.class, TransConsumeThread.class})})
public class CspGatewayApplication {
	
	public static int webServerPort = 8201;

	public static void main(String[] args) {
		SpringApplication.run(CspGatewayApplication.class, args);
	}

	@EventListener(WebServerInitializedEvent.class)
	public void webServerInitListener(WebServerInitializedEvent event) {
		int port = event.getWebServer().getPort();
		webServerPort = port;
		System.out.println("----------CspGatewayApplication getWebServer Port:" + port);
	}
}
