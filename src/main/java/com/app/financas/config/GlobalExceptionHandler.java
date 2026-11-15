package com.app.financas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Padroniza as respostas de erro da API em JSON simples e evita vazar
 * mensagens internas de excecoes inesperadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(ResponseStatusException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", ex.getStatusCode().value());
        corpo.put("mensagem", ex.getReason() != null ? ex.getReason() : "Erro na requisição.");
        return ResponseEntity.status(ex.getStatusCode()).body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInesperado(Exception ex) {
        logger.error("Erro não tratado na API", ex);
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        corpo.put("mensagem", "Erro interno. Tente novamente mais tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }
}
