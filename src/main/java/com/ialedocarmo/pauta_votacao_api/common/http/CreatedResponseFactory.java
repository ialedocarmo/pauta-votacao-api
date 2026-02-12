package com.ialedocarmo.pauta_votacao_api.common.http;

import com.ialedocarmo.pauta_votacao_api.common.url.ApiUrlBuilder;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreatedResponseFactory {

    private final ApiUrlBuilder apiUrlBuilder;

    public CreatedResponseFactory(ApiUrlBuilder apiUrlBuilder) {
        this.apiUrlBuilder = apiUrlBuilder;
    }

    public <T> ResponseEntity<T> created(String path, T body) {
        URI location = URI.create(apiUrlBuilder.absolute(path));
        return ResponseEntity.created(location).body(body);
    }
}
