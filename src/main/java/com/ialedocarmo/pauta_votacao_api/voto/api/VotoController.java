package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.common.api.ApiError;
import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Votos", description = "Operacoes de registro de voto")
public class VotoController {

    private final VotoService votoService;
    private final CreatedResponseFactory createdResponseFactory;

    public VotoController(VotoService votoService, CreatedResponseFactory createdResponseFactory) {
        this.votoService = votoService;
        this.createdResponseFactory = createdResponseFactory;
    }

    @PostMapping("/{pautaId}/votos")
    @Operation(summary = "Registrar voto", description = "Registra voto SIM/NAO para a pauta durante sessao aberta")
    @ApiResponse(responseCode = "201", description = "Voto registrado", content = @Content(schema = @Schema(implementation = VotoResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Sessao nao aberta ou encerrada",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "sessao-encerrada",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":400,\"message\":\"sessao de votacao encerrada para esta pauta\",\"path\":\"/api/v1/pautas/1/votos\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pauta nao encontrada",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "pauta-nao-encontrada",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":404,\"message\":\"pauta nao encontrada: 999\",\"path\":\"/api/v1/pautas/999/votos\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Associado ja votou nesta pauta",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "voto-duplicado",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":409,\"message\":\"associado ja votou nesta pauta\",\"path\":\"/api/v1/pautas/1/votos\"}"
                    )
            )
    )
    public ResponseEntity<VotoResponse> registrar(
            @PathVariable Long pautaId,
            @Valid @RequestBody RegistrarVotoRequest request
    ) {
        Voto voto = votoService.registrar(pautaId, request.associadoId(), request.voto());
        VotoResponse response = new VotoResponse(
                voto.getId(),
                voto.getPauta().getId(),
                voto.getAssociadoId(),
                voto.getVoto(),
                voto.getCreatedAt()
        );

        return createdResponseFactory.created("/api/v1/pautas/" + pautaId + "/votos/" + voto.getId(), response);
    }
}
