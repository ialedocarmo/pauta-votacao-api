package com.ialedocarmo.pauta_votacao_api.voto.service;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;

    public VotoService(
            VotoRepository votoRepository,
            PautaRepository pautaRepository,
            SessaoVotacaoRepository sessaoVotacaoRepository
    ) {
        this.votoRepository = votoRepository;
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
    }

    @Transactional
    public Voto registrar(Long pautaId, String associadoId, OpcaoVoto opcaoVoto) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada"));

        SessaoVotacao sessao = sessaoVotacaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão não aberta para esta pauta"));

        OffsetDateTime agora = OffsetDateTime.now();
        if (agora.isBefore(sessao.getInicio()) || !agora.isBefore(sessao.getFim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão de votação encerrada para esta pauta");
        }

        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, associadoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Associado já votou nesta pauta");
        }

        Voto voto = new Voto();
        voto.setPauta(pauta);
        voto.setAssociadoId(associadoId.trim());
        voto.setVoto(opcaoVoto);

        return votoRepository.save(voto);
    }
}
