package com.ialedocarmo.pauta_votacao_api.sessao.repository;

import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    boolean existsByPautaId(Long pautaId);

    Optional<SessaoVotacao> findByPautaId(Long pautaId);
}
