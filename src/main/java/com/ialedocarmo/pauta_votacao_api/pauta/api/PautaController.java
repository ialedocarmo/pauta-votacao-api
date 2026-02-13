package com.ialedocarmo.pauta_votacao_api.pauta.api;

import com.ialedocarmo.pauta_votacao_api.common.api.ApiError;
import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Pautas", description = "Operacoes de cadastro e apuracao de pautas")
public class PautaController {

    private final PautaService pautaService;
    private final CreatedResponseFactory createdResponseFactory;

    public PautaController(PautaService pautaService, CreatedResponseFactory createdResponseFactory) {
        this.pautaService = pautaService;
        this.createdResponseFactory = createdResponseFactory;
    }

    @PostMapping
    @Operation(summary = "Criar pauta", description = "Cadastra uma nova pauta para votacao")
    @ApiResponse(responseCode = "201", description = "Pauta criada", content = @Content(schema = @Schema(implementation = PautaResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Dados invalidos",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "titulo-invalido",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":400,\"message\":\"titulo da pauta e obrigatorio\",\"path\":\"/api/v1/pautas\"}"
                    )
            )
    )
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criar(request.titulo());
        PautaResponse response = new PautaResponse(pauta.getId(), pauta.getTitulo(), pauta.getCreatedAt());
        return createdResponseFactory.created("/api/v1/pautas/" + pauta.getId(), response);
    }

    @GetMapping("/{pautaId}/resultado")
    @Operation(summary = "Consultar resultado", description = "Retorna a apuracao consolidada da pauta")
    @ApiResponse(responseCode = "200", description = "Resultado encontrado", content = @Content(schema = @Schema(implementation = ResultadoPautaResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Pauta nao encontrada",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "pauta-nao-encontrada",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":404,\"message\":\"pauta nao encontrada: 999\",\"path\":\"/api/v1/pautas/999/resultado\"}"
                    )
            )
    )
    public ResultadoPautaResponse obterResultado(@PathVariable Long pautaId) {
        return pautaService.obterResultado(pautaId);
    }
}
