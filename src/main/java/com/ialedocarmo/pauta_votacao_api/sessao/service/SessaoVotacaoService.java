package com.ialedocarmo.pauta_votacao_api.sessao.service;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
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
public class SessaoVotacaoService {

    private static final Logger log = LoggerFactory.getLogger(SessaoVotacaoService.class);
    private static final int DURACAO_PADRAO_SEGUNDOS = 60;

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final PautaRepository pautaRepository;
    private final Clock clock;

    public SessaoVotacaoService(
            SessaoVotacaoRepository sessaoVotacaoRepository,
            PautaRepository pautaRepository,
            Clock clock
    ) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.pautaRepository = pautaRepository;
        this.clock = clock;
    }

    @Transactional
    public SessaoVotacao abrirSessao(Long pautaId, Integer duracaoSegundos) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pauta nao encontrada"));

        if (sessaoVotacaoRepository.existsByPautaId(pautaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ja existe sessao cadastrada para a pauta");
        }

        int duracaoCalculada = duracaoSegundos == null ? DURACAO_PADRAO_SEGUNDOS : duracaoSegundos;
        OffsetDateTime inicio = OffsetDateTime.now(clock);

        SessaoVotacao sessaoVotacao = new SessaoVotacao();
        sessaoVotacao.setPauta(pauta);
        sessaoVotacao.setInicio(inicio);
        sessaoVotacao.setFim(inicio.plusSeconds(duracaoCalculada));

        try {
            SessaoVotacao saved = sessaoVotacaoRepository.save(sessaoVotacao);
            log.info("Sessao aberta: pautaId={} sessaoId={} duracaoSegundos={}", pautaId, saved.getId(), duracaoCalculada);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ja existe sessao cadastrada para a pauta");
        }
    }
}
