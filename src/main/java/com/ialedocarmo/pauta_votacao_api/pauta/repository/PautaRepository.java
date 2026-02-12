package com.ialedocarmo.pauta_votacao_api.pauta.repository;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {}
