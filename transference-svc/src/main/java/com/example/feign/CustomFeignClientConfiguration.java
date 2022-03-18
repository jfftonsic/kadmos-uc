package com.example.feign;

import com.example.TransferenceConfigurationProperties;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomFeignClientConfiguration {

    @Bean
    public BasicAuthRequestInterceptor balanceABasicAuthRequestInterceptor(
            TransferenceConfigurationProperties properties) {
        final var auth = properties.feign().config().balanceAService().auth();
        return new BasicAuthRequestInterceptor(auth.user(), auth.pass());
    }
}
