package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import java.time.OffsetDateTime;

public record VotoResponse(
        Long id,
        Long pautaId,
        String associadoId,
        OpcaoVoto voto,
        OffsetDateTime createdAt
) {
}
