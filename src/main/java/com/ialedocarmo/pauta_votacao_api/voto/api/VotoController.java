package com.ialedocarmo.pauta_votacao_api.voto.api;

import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class VotoController {

    private final VotoService votoService;
    private final CreatedResponseFactory createdResponseFactory;

    public VotoController(VotoService votoService, CreatedResponseFactory createdResponseFactory) {
        this.votoService = votoService;
        this.createdResponseFactory = createdResponseFactory;
    }

    @PostMapping("/{pautaId}/votos")
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
