package com.example.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.math.BigDecimal;

@FeignClient(
        value = "${transference.feign.config.balanceAService.name}",
        url = "${transference.feign.config.balanceAService.url}",
        configuration = CustomFeignClientConfiguration.class
)
public interface BalanceAServiceClient {

    record GetBalanceResponse(BigDecimal amount){}

    @RequestMapping(value = "/balance", method = GET)
    GetBalanceResponse getBalance();
}
