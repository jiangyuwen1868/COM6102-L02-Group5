package com.mxbc.seckill;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringSeckillApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSeckillApplication.class, args);
	}

	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom()
				.failureRateThreshold(50)
				.slidingWindowSize(20)
				.minimumNumberOfCalls(10)
				.waitDurationInOpenState(java.time.Duration.ofMillis(10000))
				.permittedNumberOfCallsInHalfOpenState(3)
				.build();
		return CircuitBreakerRegistry.of(config);
	}

	@Bean
	public io.github.resilience4j.circuitbreaker.CircuitBreaker seckillCircuitBreaker(CircuitBreakerRegistry registry) {
		return registry.circuitBreaker("seckillCircuitBreaker");
		}

}
