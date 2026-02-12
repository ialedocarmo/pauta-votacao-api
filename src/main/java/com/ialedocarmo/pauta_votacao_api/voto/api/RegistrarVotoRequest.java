package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarVotoRequest(
        @NotBlank(message = "ID do associado é obrigatório!")
        String associadoId,

        @NotNull(message = "Voto é obrigatorio!")
        OpcaoVoto voto
) {
}
