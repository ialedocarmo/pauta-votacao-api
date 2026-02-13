package com.ialedocarmo.pauta_votacao_api.sessao.api;

import jakarta.validation.constraints.Positive;

public record AbrirSessaoRequest(
        @Positive(message = "duracao em segundos deve ser maior que zero")
        Integer duracaoSegundos
) {
}
