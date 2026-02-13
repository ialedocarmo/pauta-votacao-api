package com.ialedocarmo.pauta_votacao_api.pauta.api;

import java.time.OffsetDateTime;

public record PautaResponse(
        Long id,
        String titulo,
        OffsetDateTime createdAt
) {
}
