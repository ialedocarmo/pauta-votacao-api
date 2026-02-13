package com.ialedocarmo.pauta_votacao_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final AppUrlProperties appUrlProperties;

    public OpenApiConfig(AppUrlProperties appUrlProperties) {
        this.appUrlProperties = appUrlProperties;
    }

    @Bean
    public OpenAPI pautaVotacaoOpenApi() {
        Server server = new Server()
                .url(appUrlProperties.getCallbackBaseUrl())
                .description("Servidor configurado para execucao atual");

        Info info = new Info()
                .title("Pauta Votacao API")
                .version("v1")
                .description("API REST para gestao de pautas, sessoes de votacao, votos e apuracao de resultados.")
                .contact(new Contact().name("API Support"))
                .license(new License().name("MIT"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
