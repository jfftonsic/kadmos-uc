package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transference")
public record TransferenceConfigurationProperties(
        String idemActor,
        FeignProperties feign,
        QueriesConfigurationProperties queries
) {

    public record BasicAuthProperties(String user, String pass) {}

    public record FeignConfigProperties(FeignBalanceAProperties balanceAService) {
        public record FeignBalanceAProperties(BasicAuthProperties auth) {}
    }

    public record FeignProperties(FeignConfigProperties config) {
    }

    public record QueriesConfigurationProperties(String transferenceCreation) {
    }
}
