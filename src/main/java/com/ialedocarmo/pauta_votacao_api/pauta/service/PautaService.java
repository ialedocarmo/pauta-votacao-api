package com.ialedocarmo.pauta_votacao_api.pauta.service;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    @Transactional
    public Pauta criar(String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo.trim());
        return pautaRepository.save(pauta);
    }
}
