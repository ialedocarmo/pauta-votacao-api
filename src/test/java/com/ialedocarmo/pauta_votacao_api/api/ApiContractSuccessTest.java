package com.ialedocarmo.pauta_votacao_api.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ialedocarmo.pauta_votacao_api.common.api.GlobalExceptionHandler;
import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.config.TimeConfig;
import com.ialedocarmo.pauta_votacao_api.pauta.api.PautaController;
import com.ialedocarmo.pauta_votacao_api.pauta.api.PautaResponse;
import com.ialedocarmo.pauta_votacao_api.pauta.domain.Pauta;
import com.ialedocarmo.pauta_votacao_api.pauta.service.PautaService;
import com.ialedocarmo.pauta_votacao_api.sessao.api.SessaoResponse;
import com.ialedocarmo.pauta_votacao_api.sessao.api.SessaoVotacaoController;
import com.ialedocarmo.pauta_votacao_api.sessao.domain.SessaoVotacao;
import com.ialedocarmo.pauta_votacao_api.sessao.service.SessaoVotacaoService;
import com.ialedocarmo.pauta_votacao_api.voto.api.VotoController;
import com.ialedocarmo.pauta_votacao_api.voto.api.VotoResponse;
import com.ialedocarmo.pauta_votacao_api.voto.domain.OpcaoVoto;
import com.ialedocarmo.pauta_votacao_api.voto.domain.Voto;
import com.ialedocarmo.pauta_votacao_api.voto.service.VotoService;
import java.net.URI;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {PautaController.class, SessaoVotacaoController.class, VotoController.class})
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class ApiContractSuccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private SessaoVotacaoService sessaoVotacaoService;

    @MockitoBean
    private VotoService votoService;

    @MockitoBean
    private CreatedResponseFactory createdResponseFactory;

    @Test
    void deveCriarPautaCom201LocationEBody() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-12T12:00:00Z");

        Pauta pauta = mock(Pauta.class);
        when(pauta.getId()).thenReturn(10L);
        when(pauta.getTitulo()).thenReturn("Reforma do Estatuto");
        when(pauta.getCreatedAt()).thenReturn(createdAt);

        PautaResponse response = new PautaResponse(10L, "Reforma do Estatuto", createdAt);

        when(pautaService.criar("Reforma do Estatuto")).thenReturn(pauta);
        when(createdResponseFactory.created(eq("/api/v1/pautas/10"), any(PautaResponse.class)))
                .thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/api/v1/pautas/10")).body(response));

        String payload = """
                {
                  "titulo": "Reforma do Estatuto"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost:8080/api/v1/pautas/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Reforma do Estatuto"));
    }

    @Test
    void deveAbrirSessaoCom201LocationEBody() throws Exception {
        OffsetDateTime inicio = OffsetDateTime.parse("2026-02-12T12:00:00Z");
        OffsetDateTime fim = OffsetDateTime.parse("2026-02-12T12:01:00Z");

        Pauta pauta = mock(Pauta.class);
        when(pauta.getId()).thenReturn(1L);

        SessaoVotacao sessao = mock(SessaoVotacao.class);
        when(sessao.getId()).thenReturn(20L);
        when(sessao.getPauta()).thenReturn(pauta);
        when(sessao.getInicio()).thenReturn(inicio);
        when(sessao.getFim()).thenReturn(fim);

        SessaoResponse response = new SessaoResponse(20L, 1L, inicio, fim, 60);

        when(sessaoVotacaoService.abrirSessao(1L, 60)).thenReturn(sessao);
        when(createdResponseFactory.created(eq("/api/v1/pautas/1/sessoes/20"), any(SessaoResponse.class)))
                .thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/api/v1/pautas/1/sessoes/20")).body(response));

        String payload = """
                {
                  "duracaoSegundos": 60
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost:8080/api/v1/pautas/1/sessoes/20"))
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.duracaoSegundos").value(60));
    }

    @Test
    void deveRegistrarVotoCom201LocationEBody() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-12T12:00:30Z");

        Pauta pauta = mock(Pauta.class);
        when(pauta.getId()).thenReturn(1L);

        Voto voto = mock(Voto.class);
        when(voto.getId()).thenReturn(30L);
        when(voto.getPauta()).thenReturn(pauta);
        when(voto.getAssociadoId()).thenReturn("assoc-1");
        when(voto.getVoto()).thenReturn(OpcaoVoto.SIM);
        when(voto.getCreatedAt()).thenReturn(createdAt);

        VotoResponse response = new VotoResponse(30L, 1L, "assoc-1", OpcaoVoto.SIM, createdAt);

        when(votoService.registrar(1L, "assoc-1", OpcaoVoto.SIM)).thenReturn(voto);
        when(createdResponseFactory.created(eq("/api/v1/pautas/1/votos/30"), any(VotoResponse.class)))
                .thenReturn(ResponseEntity.created(URI.create("http://localhost:8080/api/v1/pautas/1/votos/30")).body(response));

        String payload = """
                {
                  "associadoId": "assoc-1",
                  "voto": "SIM"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost:8080/api/v1/pautas/1/votos/30"))
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.associadoId").value("assoc-1"))
                .andExpect(jsonPath("$.voto").value("SIM"));
    }
}
