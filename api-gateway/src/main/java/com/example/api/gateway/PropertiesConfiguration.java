package com.example.api.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Getter
@Setter
public class PropertiesConfiguration {

    // for some testing/troubleshooting purposes
    private String httpbin = "http://httpbin.org:80";

    // the adequate thing here would be to use service discovery tools
    private String savingsA = "http://localhost:8081";
    private String savingsB = "http://localhost:8082";

    private String savingsAGatewayPath = "/savings/a/**";
    private String savingsBGatewayPath = "/savings/b/**";

    private int savingsAGatewayStripPrefix = 2;
    private int savingsBGatewayStripPrefix = 2;

    private int savingsTimeoutSeconds = 5;
}
