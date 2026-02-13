package com.ialedocarmo.pauta_votacao_api.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ialedocarmo.pauta_votacao_api.pauta.api.CriarPautaRequest;
import com.ialedocarmo.pauta_votacao_api.pauta.api.PautaController;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-12T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void deveRetornar400ParaErroDeValidacaoComMensagemDeCampo() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(FIXED_CLOCK);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "criarPautaRequest");
        bindingResult.addError(new FieldError("criarPautaRequest", "titulo", "campo obrigatorio"));

        Method method = PautaController.class.getMethod("criar", CriarPautaRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("/api/v1/pautas", response.getBody().path());
        assertTrue(response.getBody().message().contains("titulo: campo obrigatorio"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void deveRetornar400ParaCorpoInvalido() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(FIXED_CLOCK);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas/1/votos");

        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("json invalido", inputMessage);

        ResponseEntity<ApiError> response = handler.handleNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("corpo da requisicao invalido", response.getBody().message());
        assertEquals("/api/v1/pautas/1/votos", response.getBody().path());
    }

    @Test
    void deveRetornar500ParaErroInesperado() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(FIXED_CLOCK);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/pautas/1/resultado");

        ResponseEntity<ApiError> response = handler.handleUnexpected(new RuntimeException("falha"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("erro interno inesperado", response.getBody().message());
        assertEquals("/api/v1/pautas/1/resultado", response.getBody().path());
    }
}
