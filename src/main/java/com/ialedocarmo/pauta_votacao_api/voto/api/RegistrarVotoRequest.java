package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarVotoRequest(
        @NotBlank(message = "obrigatorio")
        @Size(max = 100, message = "id do associado deve ter no maximo 100 caracteres")
        String associadoId,

        @NotNull(message = "obrigatorio")
        OpcaoVoto voto
) {
}
