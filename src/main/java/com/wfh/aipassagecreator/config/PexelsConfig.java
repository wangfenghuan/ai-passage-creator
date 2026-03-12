package com.wfh.aipassagecreator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Title: PexelsConfig
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.config
 * @Date 2026/3/12 19:52
 * @description:
 */
@Configuration
@ConfigurationProperties("pexels")
@Data
public class PexelsConfig {

    private String apiKey;
}
