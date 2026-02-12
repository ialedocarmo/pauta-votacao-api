package com.ialedocarmo.pauta_votacao_api.pauta.service;

import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoPautaResponse;
import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoVotacao;
import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;

    public PautaService(PautaRepository pautaRepository, VotoRepository votoRepository) {
        this.pautaRepository = pautaRepository;
        this.votoRepository = votoRepository;
    }

    @Transactional
    public Pauta criar(String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo.trim());
        return pautaRepository.save(pauta);
    }

    @Transactional(readOnly = true)
    public ResultadoPautaResponse obterResultado(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta não encontrada"));

        long totalSim = votoRepository.countByPautaIdAndVoto(pautaId, OpcaoVoto.SIM);
        long totalNao = votoRepository.countByPautaIdAndVoto(pautaId, OpcaoVoto.NAO);
        long totalVotos = totalSim + totalNao;

        ResultadoVotacao resultado = ResultadoVotacao.EMPATE;
        if (totalSim > totalNao) {
            resultado = ResultadoVotacao.APROVADA;
        } else if (totalNao > totalSim) {
            resultado = ResultadoVotacao.REPROVADA;
        }

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
