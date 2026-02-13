package com.ialedocarmo.pauta_votacao_api.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "ApiError", description = "Payload padrao para respostas de erro da API")
public record ApiError(
        @Schema(example = "2026-02-12T12:10:00Z", description = "Data/hora do erro em formato ISO-8601")
        OffsetDateTime timestamp,

        @Schema(example = "400", description = "Codigo HTTP retornado")
        int status,

        @Schema(example = "sessao de votacao encerrada para esta pauta", description = "Mensagem resumida do erro")
        String message,

        @Schema(example = "/api/v1/pautas/1/votos", description = "Path da requisicao que gerou o erro")
        String path
) {
}
