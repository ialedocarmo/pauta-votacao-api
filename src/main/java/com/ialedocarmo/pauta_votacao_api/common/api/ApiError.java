package com.ialedocarmo.pauta_votacao_api.common.api;

import java.time.OffsetDateTime;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String message,
        String path
) {
}
