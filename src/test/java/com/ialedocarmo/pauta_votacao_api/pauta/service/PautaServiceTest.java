package com.ialedocarmo.pauta_votacao_api.pauta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoPautaResponse;
import com.ialedocarmo.pauta_votacao_api.pauta.api.ResultadoVotacao;
import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.repository.PautaRepository;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoRepository;
import com.ialedocarmo.pauta_votacao_api.voto.repository.VotoResumo;
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
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private VotoRepository votoRepository;

    @Test
    void deveLancar404QuandoPautaNaoExistir() {
        PautaService service = new PautaService(pautaRepository, votoRepository, fixedClock());
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.obterResultado(99L));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("pauta nao encontrada", ex.getReason());
    }

    @Test
    void deveCriarPautaComTituloTrimadoECreatedAtDoClock() {
        Clock clock = fixedClock();
        PautaService service = new PautaService(pautaRepository, votoRepository, clock);

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pauta pauta = service.criar("   Reforma do Estatuto   ");

        assertEquals("Reforma do Estatuto", pauta.getTitulo());
        assertEquals(OffsetDateTime.now(clock), pauta.getCreatedAt());
    }

    @Test
    void deveRetornarAprovadaQuandoTotalSimForMaiorQueTotalNao() {
        PautaService service = new PautaService(pautaRepository, votoRepository, fixedClock());
        stubResumo(1L, 10L, 7L);

        ResultadoPautaResponse resultado = service.obterResultado(1L);

        assertEquals(ResultadoVotacao.APROVADA, resultado.resultado());
        assertEquals(10L, resultado.totalSim());
        assertEquals(7L, resultado.totalNao());
        assertEquals(17L, resultado.totalVotos());
    }

    @Test
    void deveRetornarReprovadaQuandoTotalNaoForMaiorQueTotalSim() {
        PautaService service = new PautaService(pautaRepository, votoRepository, fixedClock());
        stubResumo(2L, 3L, 8L);

        ResultadoPautaResponse resultado = service.obterResultado(2L);

        assertEquals(ResultadoVotacao.REPROVADA, resultado.resultado());
        assertEquals(3L, resultado.totalSim());
        assertEquals(8L, resultado.totalNao());
        assertEquals(11L, resultado.totalVotos());
    }

    @Test
    void deveRetornarEmpateQuandoTotaisForemIguais() {
        PautaService service = new PautaService(pautaRepository, votoRepository, fixedClock());
        stubResumo(3L, 5L, 5L);

        ResultadoPautaResponse resultado = service.obterResultado(3L);

        assertEquals(ResultadoVotacao.EMPATE, resultado.resultado());
        assertEquals(5L, resultado.totalSim());
        assertEquals(5L, resultado.totalNao());
        assertEquals(10L, resultado.totalVotos());
    }

    @Test
    void deveRetornarEmpateComZeroVotosQuandoResumoForNulo() {
        PautaService service = new PautaService(pautaRepository, votoRepository, fixedClock());
        stubPauta(4L, "Pauta D");
        when(votoRepository.resumirPorPautaId(4L)).thenReturn(null);

        ResultadoPautaResponse resultado = service.obterResultado(4L);

        assertEquals(ResultadoVotacao.EMPATE, resultado.resultado());
        assertEquals(0L, resultado.totalSim());
        assertEquals(0L, resultado.totalNao());
        assertEquals(0L, resultado.totalVotos());
    }

    private void stubResumo(Long pautaId, long totalSim, long totalNao) {
        stubPauta(pautaId, "Pauta " + pautaId);
        VotoResumo resumo = mock(VotoResumo.class);
        when(votoRepository.resumirPorPautaId(pautaId)).thenReturn(resumo);
        when(resumo.getTotalSim()).thenReturn(totalSim);
        when(resumo.getTotalNao()).thenReturn(totalNao);
    }

    private void stubPauta(Long pautaId, String titulo) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(titulo);
        pauta.setCreatedAt(OffsetDateTime.parse("2026-02-12T00:00:00Z"));
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-02-12T01:00:00Z"), ZoneOffset.UTC);
    }
}
