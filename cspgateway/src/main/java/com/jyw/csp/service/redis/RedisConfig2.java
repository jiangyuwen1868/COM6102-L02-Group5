package com.jyw.csp.service.redis;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig2 {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;
    
    @Value("${spring.data.redis.database}")
    private int redisDatabase;
    
    @Value("${spring.data.redis.sentinel.enabled:false}")
    private boolean sentinelEnabled;
    
    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String sentinelMaster;
    
    @Value("${spring.data.redis.sentinel.nodes:}")
    private List<String> sentinelNodes;
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(10))
            .build();
        
        if (sentinelEnabled && sentinelNodes != null && !sentinelNodes.isEmpty()) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.setMaster(sentinelMaster);
            sentinelConfig.setPassword(redisPassword);
            sentinelConfig.setDatabase(redisDatabase);
            
            for (String node : sentinelNodes) {
                String[] parts = node.split(":");
                if (parts.length == 2) {
                    sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
                }
            }
            
            return new LettuceConnectionFactory(sentinelConfig, clientConfig);
        } else {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
            standaloneConfig.setHostName(redisHost);
            standaloneConfig.setPort(redisPort);
            standaloneConfig.setPassword(redisPassword);
            standaloneConfig.setDatabase(redisDatabase);
            
            return new LettuceConnectionFactory(standaloneConfig, clientConfig);
        }
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}

