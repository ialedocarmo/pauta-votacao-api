package com.ialedocarmo.pauta_votacao_api.voto.repository;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);


    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN voto = 'SIM' THEN 1 ELSE 0 END), 0) AS totalSim,
                COALESCE(SUM(CASE WHEN voto = 'NAO' THEN 1 ELSE 0 END), 0) AS totalNao
            FROM voto
            WHERE pauta_id = :pautaId
            """, nativeQuery = true)
    VotoResumo resumirPorPautaId(@Param("pautaId") Long pautaId);
}
