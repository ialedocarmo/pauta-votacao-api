package com.ialedocarmo.pauta_votacao_api.voto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Test
    void deveNormalizarAssociadoIdAntesDePersistir() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-12T01:00:00Z"), ZoneOffset.UTC);
        VotoService service = new VotoService(votoRepository, pautaRepository, sessaoVotacaoRepository, clock);

        Pauta pauta = new Pauta();
        pauta.setTitulo("Pauta teste");

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setInicio(OffsetDateTime.parse("2026-02-12T00:59:00Z"));
        sessao.setFim(OffsetDateTime.parse("2026-02-12T01:10:00Z"));

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "assoc-1")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Voto voto = service.registrar(1L, "  assoc-1  ", OpcaoVoto.SIM);

        assertEquals("assoc-1", voto.getAssociadoId());
    }

    @Test
    void deveRejeitarVotoQuandoSessaoEncerrada() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-12T02:00:00Z"), ZoneOffset.UTC);
        VotoService service = new VotoService(votoRepository, pautaRepository, sessaoVotacaoRepository, clock);

        Pauta pauta = new Pauta();
        pauta.setTitulo("Pauta teste");

        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setInicio(OffsetDateTime.parse("2026-02-12T00:00:00Z"));
        sessao.setFim(OffsetDateTime.parse("2026-02-12T01:00:00Z"));

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
