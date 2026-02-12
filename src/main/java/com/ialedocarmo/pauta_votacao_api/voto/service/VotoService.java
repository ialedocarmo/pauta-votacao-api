package com.ialedocarmo.pauta_votacao_api.voto.service;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VotoService {

    private static final Logger log = LoggerFactory.getLogger(VotoService.class);

    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final Clock clock;

    public VotoService(
            VotoRepository votoRepository,
            PautaRepository pautaRepository,
            SessaoVotacaoRepository sessaoVotacaoRepository,
            Clock clock
    ) {
        this.votoRepository = votoRepository;
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.clock = clock;
    }

    @Transactional
    public Voto registrar(Long pautaId, String associadoId, OpcaoVoto opcaoVoto) {
        String associadoIdNormalizado = associadoId.trim();

        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pauta nao encontrada"));

        SessaoVotacao sessao = sessaoVotacaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessao nao aberta para esta pauta"));

        OffsetDateTime agora = OffsetDateTime.now(clock);
        if (agora.isBefore(sessao.getInicio()) || !agora.isBefore(sessao.getFim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessao de votacao encerrada para esta pauta");
        }

        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, associadoIdNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "associado ja votou nesta pauta");
        }

        Voto voto = new Voto();
        voto.setPauta(pauta);
        voto.setAssociadoId(associadoIdNormalizado);
        voto.setVoto(opcaoVoto);

        try {
            Voto saved = votoRepository.save(voto);
            log.info("Voto registrado: pautaId={} associadoId={} voto={}", pautaId, associadoIdNormalizado, opcaoVoto);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "associado ja votou nesta pauta");
        }
    }
}
