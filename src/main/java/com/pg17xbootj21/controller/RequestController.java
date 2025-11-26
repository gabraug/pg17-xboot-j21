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

        if (request.getModules() == null || request.getModules().isEmpty()) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "At least one module is required",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (request.getModules().size() > 3) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Must select between 1 and 3 modules",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        
        if (page < 0) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Page number must be greater than or equal to 0",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (size < 1 || size > 100) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Page size must be between 1 and 100",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (status != null && !status.isEmpty()) {
            String upperStatus = status.toUpperCase();
            if (!upperStatus.equals("ATIVO") && !upperStatus.equals("NEGADO") && !upperStatus.equals("CANCELADO")) {
                ErrorResponse error = new ErrorResponse(
                    "Bad Request",
                    "Status must be one of: ATIVO, NEGADO, CANCELADO",
                    HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
        
        if (startDate != null && !startDate.isEmpty() && !isValidDateFormat(startDate)) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Invalid start date format. Expected format: YYYY-MM-DD",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (endDate != null && !endDate.isEmpty() && !isValidDateFormat(endDate)) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Invalid end date format. Expected format: YYYY-MM-DD",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
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
        
        if (protocol == null || protocol.trim().isEmpty()) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Protocol is required",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (!protocol.matches("^SOL-\\d{8}-\\d{4}$")) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Invalid protocol format. Expected format: SOL-YYYYMMDD-NNNN",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
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

    @PostMapping("/renew")
    public ResponseEntity<?> renewAccess(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RenewAccessRequest request) {
        
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
            Request renewedRequest = requestService.renewAccess(userId, request.getRequestProtocol());

            CreateRequestResponse response = new CreateRequestResponse();
            response.setProtocol(renewedRequest.getProtocol());
            response.setStatus(renewedRequest.getStatus());

            if ("ATIVO".equals(renewedRequest.getStatus())) {
                response.setMessage("Solicitação criada com sucesso! Protocolo: " + renewedRequest.getProtocol() + ". Seus acessos já estão disponíveis!");
            } else {
                response.setDenialReason(renewedRequest.getDenialReason());
                response.setMessage("Solicitação negada. Motivo: " + renewedRequest.getDenialReason());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{protocol}/cancel")
    public ResponseEntity<?> cancelRequest(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String protocol,
            @Valid @RequestBody CancelRequestRequest request) {
        
        if (protocol == null || protocol.trim().isEmpty()) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Protocol is required",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (!protocol.matches("^SOL-\\d{8}-\\d{4}$")) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                "Invalid protocol format. Expected format: SOL-YYYYMMDD-NNNN",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
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
            Request cancelledRequest = requestService.cancelRequest(userId, protocol, request.getReason());
            RequestDetailsResponse response = toDetails(cancelledRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Bad Request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
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

    private boolean isValidDateFormat(String date) {
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

