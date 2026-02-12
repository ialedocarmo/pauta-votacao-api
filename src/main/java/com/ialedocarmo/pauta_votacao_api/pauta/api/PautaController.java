package com.ialedocarmo.pauta_votacao_api.pauta.api;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.service.PautaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PautaResponse criar(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criar(request.titulo());
        return new PautaResponse(pauta.getId(), pauta.getTitulo(), pauta.getCreatedAt());
    }

    @GetMapping("/{pautaId}/resultado")
    public ResultadoPautaResponse obterResultado(@PathVariable Long pautaId) {
        return pautaService.obterResultado(pautaId);
    }
}
