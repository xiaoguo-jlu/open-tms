package com.opentms.dealing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.opentms.dealing.mapper")
public class DealingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealingApplication.class, args);
    }
}