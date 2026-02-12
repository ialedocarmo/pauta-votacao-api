package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarVotoRequest(
        @NotBlank(message = "id do associado obrigatorio")
        String associadoId,

        @NotNull(message = "voto obrigatorio")
        OpcaoVoto voto
) {
}
