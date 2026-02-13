package com.ialedocarmo.pauta_votacao_api.sessao.api;

import com.ialedocarmo.pauta_votacao_api.common.api.ApiError;
import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.service.SessaoVotacaoService;
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
@Tag(name = "Sessoes", description = "Operacoes de abertura de sessao de votacao")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoVotacaoService;
    private final CreatedResponseFactory createdResponseFactory;

    public SessaoVotacaoController(SessaoVotacaoService sessaoVotacaoService, CreatedResponseFactory createdResponseFactory) {
        this.sessaoVotacaoService = sessaoVotacaoService;
        this.createdResponseFactory = createdResponseFactory;
    }

    @PostMapping("/{pautaId}/sessoes")
    @Operation(summary = "Abrir sessao", description = "Abre uma sessao para a pauta com duracao informada ou 60s por padrao")
    @ApiResponse(responseCode = "201", description = "Sessao aberta", content = @Content(schema = @Schema(implementation = SessaoResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Pauta nao encontrada",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "pauta-nao-encontrada",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":404,\"message\":\"pauta nao encontrada: 999\",\"path\":\"/api/v1/pautas/999/sessoes\"}"
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Pauta ja possui sessao",
            content = @Content(
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "sessao-ja-existe",
                            value = "{\"timestamp\":\"2026-02-12T12:10:00Z\",\"status\":409,\"message\":\"ja existe sessao para esta pauta\",\"path\":\"/api/v1/pautas/1/sessoes\"}"
                    )
            )
    )
    public ResponseEntity<SessaoResponse> abrir(
            @PathVariable Long pautaId,
            @Valid @RequestBody(required = false) AbrirSessaoRequest request
    ) {
        Integer duracaoSegundos = request == null ? null : request.duracaoSegundos();
        SessaoVotacao sessao = sessaoVotacaoService.abrirSessao(pautaId, duracaoSegundos);
        int duracaoCalculada = (int) (sessao.getFim().toEpochSecond() - sessao.getInicio().toEpochSecond());

        SessaoResponse response = new SessaoResponse(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getInicio(),
                sessao.getFim(),
                duracaoCalculada
        );

        return createdResponseFactory.created("/api/v1/pautas/" + pautaId + "/sessoes/" + sessao.getId(), response);
    }
}
