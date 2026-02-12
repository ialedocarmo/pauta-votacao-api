package com.ialedocarmo.pauta_votacao_api.common.url;

import com.ialedocarmo.pauta_votacao_api.config.AppUrlProperties;
import org.springframework.stereotype.Component;

@Component
public class ApiUrlBuilder {

    private final AppUrlProperties appUrlProperties;

    public ApiUrlBuilder(AppUrlProperties appUrlProperties) {
        this.appUrlProperties = appUrlProperties;
    }

    public String absolute(String path) {
        String baseUrl = appUrlProperties.getCallbackBaseUrl().trim();
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
