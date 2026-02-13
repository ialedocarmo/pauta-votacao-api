package com.ialedocarmo.pauta_votacao_api.pauta.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(
        @NotBlank(message = "obrigatorio")
        @Size(min = 3, max = 120, message = "deve ter entre 3 e 120 caracteres")
        String titulo
) {
}
