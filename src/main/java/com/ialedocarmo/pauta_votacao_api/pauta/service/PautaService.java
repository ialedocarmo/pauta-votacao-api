package com.ialedocarmo.pauta_votacao_api.pauta.service;

import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoPautaResponse;
import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoVotacao;
import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoResumo;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;

    public PautaService(PautaRepository pautaRepository, VotoRepository votoRepository, Clock clock) {
        this.pautaRepository = pautaRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
    }

    @Transactional
    public Pauta criar(String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo.trim());
        pauta.setCreatedAt(OffsetDateTime.now(clock));

        Pauta saved = pautaRepository.save(pauta);
        log.info("Pauta criada: id={} titulo={}", saved.getId(), saved.getTitulo());
        return saved;
    }

    @Transactional(readOnly = true)
    public ResultadoPautaResponse obterResultado(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pauta nao encontrada"));

        VotoResumo resumo = votoRepository.resumirPorPautaId(pautaId);
        long totalSim = resumo == null ? 0 : resumo.getTotalSim();
        long totalNao = resumo == null ? 0 : resumo.getTotalNao();
        long totalVotos = totalSim + totalNao;

        ResultadoVotacao resultado = ResultadoVotacao.EMPATE;
        if (totalSim > totalNao) {
            resultado = ResultadoVotacao.APROVADA;
        } else if (totalNao > totalSim) {
            resultado = ResultadoVotacao.REPROVADA;
        }

        log.info("Resultado pauta id={} sim={} nao={} total={} resultado={}", pautaId, totalSim, totalNao, totalVotos, resultado);

        return new ResultadoPautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                totalSim,
                totalNao,
                totalVotos,
                resultado
        );
    }
}
