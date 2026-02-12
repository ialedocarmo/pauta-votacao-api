package com.ialedocarmo.pauta_votacao_api.pauta.api;

import jakarta.validation.constraints.NotBlank;

public record CriarPautaRequest(
        @NotBlank(message = "titulo e obrigatorio")
        String titulo
) {
}
