package com.ialedocarmo.pauta_votacao_api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppUrlProperties.class)
public class AppConfig {
}
