package com.jyw.csp;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        logger.debug("==========ServletInitializer->configure");

        return application.sources(CspGatewayApplication.class);
    }
}
