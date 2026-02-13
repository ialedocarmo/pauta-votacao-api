package com.ialedocarmo.pauta_votacao_api.sessao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.repository.SessaoVotacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Test
    void deveRetornar404QuandoPautaNaoExistir() {
        SessaoVotacaoService service = service();

        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.abrirSessao(1L, null));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("pauta nao encontrada", ex.getReason());
    }

    @Test
    void deveRetornar409QuandoSessaoJaExistirParaPauta() {
        SessaoVotacaoService service = service();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.existsByPautaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.abrirSessao(1L, null));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("ja existe sessao cadastrada para a pauta", ex.getReason());
    }

    @Test
    void deveRetornar409QuandoSaveLancarViolacaoDeIntegridade() {
        SessaoVotacaoService service = service();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenThrow(new DataIntegrityViolationException("violacao"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.abrirSessao(1L, null));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("ja existe sessao cadastrada para a pauta", ex.getReason());
    }

    @Test
    void deveAbrirSessaoComDuracaoPadraoDeSessentaSegundos() {
        SessaoVotacaoService service = service();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta("Pauta teste")));
        when(sessaoVotacaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoVotacao sessao = service.abrirSessao(1L, null);

        assertNotNull(sessao);
        assertEquals(60, sessao.getFim().toEpochSecond() - sessao.getInicio().toEpochSecond());
    }

    private SessaoVotacaoService service() {
        return new SessaoVotacaoService(sessaoVotacaoRepository, pautaRepository, fixedClock());
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-02-12T01:00:00Z"), ZoneOffset.UTC);
    }

    private Pauta pauta(String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo);
        return pauta;
    }
}
