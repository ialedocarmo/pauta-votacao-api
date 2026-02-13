package com.ialedocarmo.pauta_votacao_api.voto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.dao.DataIntegrityViolationException;
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
    void deveRetornar404QuandoPautaNaoExistir() {
        VotoService service = serviceAt("2026-02-12T01:00:00Z");
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("pauta nao encontrada", ex.getReason());
    }

    @Test
    void deveRetornar400QuandoSessaoNaoEstiverAberta() {
        VotoService service = serviceAt("2026-02-12T01:00:00Z");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("sessao nao aberta para esta pauta", ex.getReason());
    }

    @Test
    void deveRetornar409QuandoAssociadoJaTiverVotado() {
        VotoService service = serviceAt("2026-02-12T01:00:00Z");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoAberta()));
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "assoc-1")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("associado ja votou nesta pauta", ex.getReason());
    }

    @Test
    void deveRetornar409QuandoSaveLancarViolacaoDeIntegridade() {
        VotoService service = serviceAt("2026-02-12T01:00:00Z");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoAberta()));
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "assoc-1")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenThrow(new DataIntegrityViolationException("violacao"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("associado ja votou nesta pauta", ex.getReason());
    }

    @Test
    void deveNormalizarAssociadoIdAntesDePersistir() {
        VotoService service = serviceAt("2026-02-12T01:00:00Z");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoAberta()));
        when(votoRepository.existsByPautaIdAndAssociadoId(1L, "assoc-1")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Voto voto = service.registrar(1L, "  assoc-1  ", OpcaoVoto.SIM);

        assertNotNull(voto);
        assertEquals("assoc-1", voto.getAssociadoId());
    }

    @Test
    void deveRejeitarVotoQuandoSessaoEncerrada() {
        VotoService service = serviceAt("2026-02-12T02:00:00Z");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessaoEncerrada()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.registrar(1L, "assoc-1", OpcaoVoto.SIM)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("sessao de votacao encerrada para esta pauta", exception.getReason());
    }

    private VotoService serviceAt(String instant) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
        return new VotoService(votoRepository, pautaRepository, sessaoVotacaoRepository, clock);
    }

    private Pauta pauta(String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo);
        pauta.setCreatedAt(OffsetDateTime.parse("2026-02-12T00:00:00Z"));
        return pauta;
    }

    private SessaoVotacao sessaoAberta() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setInicio(OffsetDateTime.parse("2026-02-12T00:59:00Z"));
        sessao.setFim(OffsetDateTime.parse("2026-02-12T01:10:00Z"));
        return sessao;
    }

    private SessaoVotacao sessaoEncerrada() {
        SessaoVotacao sessao = new SessaoVotacao();
        sessao.setInicio(OffsetDateTime.parse("2026-02-12T00:00:00Z"));
        sessao.setFim(OffsetDateTime.parse("2026-02-12T01:00:00Z"));
        return sessao;
    }
}
