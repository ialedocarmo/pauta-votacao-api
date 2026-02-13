package com.ialedocarmo.pauta_votacao_api.pauta.api;

public record ResultadoPautaResponse(
        Long pautaId,
        String titulo,
        long totalSim,
        long totalNao,
        long totalVotos,
        ResultadoVotacao resultado
) {
}
