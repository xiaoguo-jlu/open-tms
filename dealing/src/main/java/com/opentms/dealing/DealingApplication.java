package com.opentms.dealing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.opentms.dealing.mapper")
@ComponentScan(basePackages = {"com.opentms.dealing", "com.opentms.common"})
public class DealingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealingApplication.class, args);
    }
}