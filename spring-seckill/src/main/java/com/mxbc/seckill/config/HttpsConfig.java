package com.mxbc.seckill.config;


import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HttpsConfig {

    /**
     * 配置Tomcat支持HTTP自动重定向到HTTPS
     */
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        // 创建Tomcat工厂实例
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // 配置安全约束，将HTTP请求重定向到HTTPS
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        
        // 添加HTTP连接器，监听8080端口并重定向到HTTPS
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    /**
     * 创建HTTP连接器，用于将HTTP请求重定向到HTTPS
     */
    private Connector httpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080); // HTTP端口
        connector.setSecure(false);
        connector.setRedirectPort(8443); // 重定向到HTTPS端口
        return connector;
    }
}

