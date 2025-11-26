package com.pg17xbootj21.controller;

import com.pg17xbootj21.dto.*;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.RequestService;
import com.pg17xbootj21.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping
    public ResponseEntity<?> searchRequests(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean urgent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
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
            List<Request> requests = requestService.searchRequests(userId, search, status, startDate, endDate, urgent);
            
            int totalElements = requests.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            List<Request> pagedRequests = requests.subList(fromIndex, toIndex);
            
            List<RequestSummaryResponse> summaries = pagedRequests.stream()
                    .map(this::toSummary)
                    .collect(Collectors.toList());
            
            PagedResponse<RequestSummaryResponse> response = new PagedResponse<>(
                summaries, page, size, totalElements
            );
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{protocol}")
    public ResponseEntity<?> getRequestDetails(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String protocol) {
        
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
            Request request = requestService.findRequestByProtocol(userId, protocol);
            if (request == null) {
                ErrorResponse error = new ErrorResponse(
                    "Not Found",
                    "Request not found",
                    HttpStatus.NOT_FOUND.value()
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            RequestDetailsResponse response = toDetails(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private RequestSummaryResponse toSummary(Request request) {
        RequestSummaryResponse summary = new RequestSummaryResponse();
        summary.setProtocol(request.getProtocol());
        summary.setModules(request.getModules());
        summary.setStatus(request.getStatus());
        summary.setJustification(request.getJustification());
        summary.setUrgent(request.isUrgent());
        summary.setCreatedAt(request.getCreatedAt());
        summary.setExpiresAt(request.getExpiresAt());
        summary.setDenialReason(request.getDenialReason());
        return summary;
    }

    private RequestDetailsResponse toDetails(Request request) {
        RequestDetailsResponse details = new RequestDetailsResponse();
        details.setProtocol(request.getProtocol());
        details.setUserId(request.getUserId());
        details.setUserDepartment(request.getUserDepartment());
        details.setModules(request.getModules());
        details.setJustification(request.getJustification());
        details.setUrgent(request.isUrgent());
        details.setStatus(request.getStatus());
        details.setCreatedAt(request.getCreatedAt());
        details.setExpiresAt(request.getExpiresAt());
        details.setDenialReason(request.getDenialReason());
        details.setHistory(request.getHistory());
        return details;
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}

