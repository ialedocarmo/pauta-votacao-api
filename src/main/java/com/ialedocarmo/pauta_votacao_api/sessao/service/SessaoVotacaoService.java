package com.ialedocarmo.pauta_votacao_api.sessao.service;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessaoVotacaoService {

    private static final int DURACAO_PADRAO_SEGUNDOS = 60;

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final PautaRepository pautaRepository;

    public SessaoVotacaoService(SessaoVotacaoRepository sessaoVotacaoRepository, PautaRepository pautaRepository) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.pautaRepository = pautaRepository;
    }

    @Transactional
    public SessaoVotacao abrirSessao(Long pautaId, Integer duracaoSegundos) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada"));

        if (sessaoVotacaoRepository.existsByPautaId(pautaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe sessão cadastrada para a pauta");
        }

        int duracaoCalculada = duracaoSegundos == null ? DURACAO_PADRAO_SEGUNDOS : duracaoSegundos;
        OffsetDateTime inicio = OffsetDateTime.now();

        SessaoVotacao sessaoVotacao = new SessaoVotacao();
        sessaoVotacao.setPauta(pauta);
        sessaoVotacao.setInicio(inicio);
        sessaoVotacao.setFim(inicio.plusSeconds(duracaoCalculada));

        return sessaoVotacaoRepository.save(sessaoVotacao);
    }
}
