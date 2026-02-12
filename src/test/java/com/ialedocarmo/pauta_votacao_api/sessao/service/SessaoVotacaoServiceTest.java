package com.ialedocarmo.pauta_votacao_api.sessao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Test
    void deveAbrirSessaoComDuracaoPadraoDeSessentaSegundos() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-12T01:00:00Z"), ZoneOffset.UTC);
        SessaoVotacaoService service = new SessaoVotacaoService(sessaoVotacaoRepository, pautaRepository, clock);

        Pauta pauta = new Pauta();
        pauta.setTitulo("Pauta teste");

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoVotacao sessao = service.abrirSessao(1L, null);

        assertNotNull(sessao);
        assertEquals(60, sessao.getFim().toEpochSecond() - sessao.getInicio().toEpochSecond());
    }
}
