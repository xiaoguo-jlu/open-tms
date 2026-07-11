package com.opentms.dealing.integration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 配置（v1.0 - 2026-07-11）
 *
 * <p>为 {@link BasedataMatchClient} 提供 RestTemplate Bean。
 * 短超时（connect=2s, read=3s），避免交易创建被基于数据故障拖累。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Configuration
public class RestTemplateConfig {

    @Bean("dealingBasedataRestTemplate")
    public RestTemplate basedataRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}