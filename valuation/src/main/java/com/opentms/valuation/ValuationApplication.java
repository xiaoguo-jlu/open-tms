package com.opentms.valuation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
public class ValuationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValuationApplication.class, args);
    }
}