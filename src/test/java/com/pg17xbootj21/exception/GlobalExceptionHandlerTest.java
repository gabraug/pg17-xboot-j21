package com.pg17xbootj21.exception;

import com.pg17xbootj21.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ShouldReturnFirstMessage() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("dto", "field", "Mensagem inválida");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = Mockito.mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Mensagem inválida", response.getBody().getMessage());
    }

    @Test
    void handleMissingParameter_ShouldDescribeParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("status", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Required parameter is missing: status", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(new IllegalArgumentException("Erro"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erro", response.getBody().getMessage());
    }

    @Test
    void handleIOException_ShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleIOException(new IOException("Arquivo"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error processing request", response.getBody().getMessage());
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(new RuntimeException("Falha"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Falha", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(new Exception("Qualquer"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}

