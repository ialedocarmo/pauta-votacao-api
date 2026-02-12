package com.ialedocarmo.pauta_votacao_api.sessao.api;

import java.time.OffsetDateTime;

public record SessaoResponse(
        Long id,
        Long pautaId,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        int duracaoSegundos
) {}
