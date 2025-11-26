package com.pg17xbootj21.controller;

import com.pg17xbootj21.dto.CreateRequestRequest;
import com.pg17xbootj21.dto.CreateRequestResponse;
import com.pg17xbootj21.dto.ErrorResponse;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.RequestService;
import com.pg17xbootj21.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final AuthService authService;
    private final SessionService sessionService;

    public RequestController(RequestService requestService, AuthService authService, SessionService sessionService) {
        this.requestService = requestService;
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateRequestRequest request) {
        
        String token = extractToken(authorization);
        if (token == null || !sessionService.isValidSession(token)) {
            ErrorResponse error = new ErrorResponse(
                "Unauthorized",
                "Invalid or expired token",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String userId = authService.getUserIdByToken(token);
        if (userId == null) {
            ErrorResponse error = new ErrorResponse(
                "Unauthorized",
                "User not found",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            Request createdRequest = requestService.createRequest(
                userId,
                request.getModules(),
                request.getJustification(),
                request.isUrgent()
            );

            CreateRequestResponse response = new CreateRequestResponse();
            response.setProtocol(createdRequest.getProtocol());
            response.setStatus(createdRequest.getStatus());

            if ("ATIVO".equals(createdRequest.getStatus())) {
                response.setMessage("Solicitação criada com sucesso! Protocolo: " + createdRequest.getProtocol() + ". Seus acessos já estão disponíveis!");
            } else {
                response.setDenialReason(createdRequest.getDenialReason());
                response.setMessage("Solicitação negada. Motivo: " + createdRequest.getDenialReason());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            CreateRequestResponse response = new CreateRequestResponse();
            response.setStatus("NEGADO");
            response.setDenialReason(e.getMessage());
            response.setMessage("Solicitação negada. Motivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

