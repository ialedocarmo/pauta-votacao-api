package com.ialedocarmo.pauta_votacao_api.sessao.api;

import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.service.SessaoVotacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoVotacaoService;

    public SessaoVotacaoController(SessaoVotacaoService sessaoVotacaoService) {
        this.sessaoVotacaoService = sessaoVotacaoService;
    }

    @PostMapping("/{pautaId}/sessoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse abrir(
            @PathVariable Long pautaId,
            @Valid @RequestBody(required = false) AbrirSessaoRequest request
    ) {
        Integer duracaoSegundos = request == null ? null : request.duracaoSegundos();
        SessaoVotacao sessao = sessaoVotacaoService.abrirSessao(pautaId, duracaoSegundos);
        int duracaoCalculada = (int) (sessao.getFim().toEpochSecond() - sessao.getInicio().toEpochSecond());

        return new SessaoResponse(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getInicio(),
                sessao.getFim(),
                duracaoCalculada
        );
    }
}
