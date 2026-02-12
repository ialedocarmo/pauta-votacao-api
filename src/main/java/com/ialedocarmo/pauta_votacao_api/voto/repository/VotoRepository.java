package com.ialedocarmo.pauta_votacao_api.voto.repository;

import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);
}
