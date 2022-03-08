package com.example.api.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@SpringBootApplication
@EnableConfigurationProperties(PropertiesConfiguration.class)
@RestController
@Slf4j
public class Main {

    public static final String CB_SAVINGS = "cb-savings";

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public RouteLocator myRoutes(
            RouteLocatorBuilder builder,
            PropertiesConfiguration propertiesConfiguration
    ) {
        return builder.routes()
                .route(p -> p
                        .path(propertiesConfiguration.getSavingsAGatewayPath())
                        .filters(gatewayFilterSpec ->
                                gatewayFilterSpec
                                        .stripPrefix(propertiesConfiguration.getSavingsAGatewayStripPrefix())
                                        .circuitBreaker(config -> config
                                                        .setName(CB_SAVINGS)
                                        )
                        )
                        .uri(propertiesConfiguration.getSavingsA())
                )
                .route(p -> p
                        .path(propertiesConfiguration.getSavingsBGatewayPath())
                        .filters(gatewayFilterSpec ->
                                gatewayFilterSpec
                                        .stripPrefix(propertiesConfiguration.getSavingsBGatewayStripPrefix())
                                        .circuitBreaker(config -> config
                                                .setName(CB_SAVINGS)
                                        )
                        )
                        .uri(propertiesConfiguration.getSavingsB())
                )
                .route(p -> p
                        .host("*.circuitbreaker.com")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                                .setName(CB_SAVINGS)
                                )
                        )
                        .uri(propertiesConfiguration.getHttpbin()))
                .build();
    }


    @Bean
    public ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory(
            CircuitBreakerRegistry circuitBreakerRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            PropertiesConfiguration propertiesConfiguration
    ) {
        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory(
                circuitBreakerRegistry,
                timeLimiterRegistry);
        factory.configure(
                builder ->
                    builder
                            .timeLimiterConfig(
                                    TimeLimiterConfig.custom()
                                            .timeoutDuration(
                                                    Duration.ofSeconds(
                                                            propertiesConfiguration.getSavingsTimeoutSeconds()
                                                    )
                                            ).build()
                            )
                ,
                CB_SAVINGS

        );

        return factory;
    }
}

