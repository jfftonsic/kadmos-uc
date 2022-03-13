package com.example;

import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = TransferenceConfigurationProperties.class)
@EnableFeignClients
@Slf4j
public class Main {

    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        return filter;
    }

    @Bean
    public CommandLineRunner printConfig(TransferenceConfigurationProperties transferenceConfigurationProperties) {
        return args -> {
//            log.info("**********************************************************************************");
//            log.info(transferenceConfigurationProperties.queries().query1());
//            log.info("**********************************************************************************");
        };

    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}

