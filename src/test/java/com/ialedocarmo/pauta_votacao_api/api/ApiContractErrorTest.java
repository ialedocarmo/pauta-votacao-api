package com.ialedocarmo.pauta_votacao_api.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ialedocarmo.pauta_votacao_api.common.api.GlobalExceptionHandler;
import com.ialedocarmo.pauta_votacao_api.common.http.CreatedResponseFactory;
import com.ialedocarmo.pauta_votacao_api.config.TimeConfig;
import com.ialedocarmo.pauta_votacao_api.pauta.api.PautaController;
import com.ialedocarmo.pauta_votacao_api.pauta.service.PautaService;
import com.ialedocarmo.pauta_votacao_api.voto.api.VotoController;
import com.ialedocarmo.pauta_votacao_api.voto.service.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = {PautaController.class, VotoController.class})
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class ApiContractErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private VotoService votoService;

    @MockitoBean
    private CreatedResponseFactory createdResponseFactory;

    @Test
    void deveRetornar400QuandoTituloForInvalidoAoCriarPauta() throws Exception {
        String payload = """
                {
                  "titulo": "ab"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("titulo")))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas"));
    }

    @Test
    void deveRetornar400QuandoAssociadoIdUltrapassarLimite() throws Exception {
        String associadoIdLongo = "a".repeat(101);
        String payload = "{\"associadoId\":\"" + associadoIdLongo + "\",\"voto\":\"SIM\"}";

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("associado")))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/1/votos"));
    }

    @Test
    void deveRetornar400QuandoVotoForInvalido() throws Exception {
        String payload = """
                {
                  "associadoId": "assoc-1"
                }
                """;

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("voto")))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/1/votos"));
    }

    @Test
    void deveRetornar404QuandoResultadoDaPautaNaoExistir() throws Exception {
        when(pautaService.obterResultado(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "pauta nao encontrada"));

        mockMvc.perform(get("/api/v1/pautas/99/resultado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("pauta nao encontrada"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/99/resultado"));
    }
}
