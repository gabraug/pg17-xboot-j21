package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.model.RequestHistory;
import com.pg17xbootj21.model.User;
import com.pg17xbootj21.repository.AccessRepository;
import com.pg17xbootj21.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final AccessRepository accessRepository;
    private final ModuleService moduleService;
    private final AccessService accessService;
    private final UserService userService;
    private final BusinessRuleService businessRuleService;
    private static final List<String> GENERIC_WORDS = List.of("teste", "aaa", "preciso");

    public RequestService(RequestRepository requestRepository, AccessRepository accessRepository,
                         ModuleService moduleService, AccessService accessService, 
                         UserService userService, BusinessRuleService businessRuleService) {
        this.requestRepository = requestRepository;
        this.accessRepository = accessRepository;
        this.moduleService = moduleService;
        this.accessService = accessService;
        this.userService = userService;
        this.businessRuleService = businessRuleService;
    }

    @Transactional
    public Request createRequest(String userId, List<String> moduleIds, String justification, boolean urgent) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateRequest(userId, moduleIds, justification);

        String protocol = generateProtocol();
        Instant createdAt = Instant.now();
        Instant expiresAt = Instant.now().plusSeconds(180 * 24 * 60 * 60L);

        String denialReason = businessRuleService.validateBusinessRules(userId, user.getDepartment(), moduleIds);
        String status = denialReason == null ? "ATIVO" : "NEGADO";

        Request request = new Request();
        request.setProtocol(protocol);
        request.setUserId(userId);
        request.setUserDepartment(user.getDepartment());
        request.setModules(moduleIds);
        request.setJustification(justification);
        request.setUrgent(urgent);
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        request.setExpiresAt(expiresAt);
        request.setDenialReason(denialReason);

        List<RequestHistory> history = new ArrayList<>();
        history.add(createHistoryEntry(request, createdAt, "CREATED"));
        if ("ATIVO".equals(status)) {
            history.add(createHistoryEntry(request, createdAt, "APPROVED"));
        } else {
            history.add(createHistoryEntry(request, createdAt, "DENIED"));
        }
        request.setHistory(history);

        requestRepository.save(request);

        if ("ATIVO".equals(status)) {
            createAccesses(userId, moduleIds, protocol, createdAt, expiresAt);
        }

        return request;
    }

    private void validateRequest(String userId, List<String> moduleIds, String justification) {
        List<Request> activeRequests = requestRepository.findByUserIdAndStatus(userId, "ATIVO");
        List<String> activeModuleIds = accessService.getActiveModuleIds(userId);

        for (String moduleId : moduleIds) {
            Module module = moduleService.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));

            if (!module.isActive()) {
                throw new RuntimeException("Module is not active: " + moduleId);
            }

            boolean hasActiveRequest = activeRequests.stream()
                    .anyMatch(req -> req.getModules().contains(moduleId) && "ATIVO".equals(req.getStatus()));

            if (hasActiveRequest) {
                throw new RuntimeException("Active request already exists for module: " + moduleId);
            }

            if (activeModuleIds.contains(moduleId)) {
                throw new RuntimeException("User already has active access to module: " + moduleId);
            }
        }

        if (isGenericJustification(justification)) {
            throw new RuntimeException("Justificativa insuficiente ou genérica");
        }
    }

    private boolean isGenericJustification(String justification) {
        if (justification == null || justification.trim().isEmpty()) {
            return true;
        }
        String lowerJustification = justification.toLowerCase().trim();
        return GENERIC_WORDS.stream().anyMatch(word -> 
            lowerJustification.equals(word) || lowerJustification.matches("^" + word + "\\s*$"));
    }

    public Request findRequestByProtocol(String userId, String protocol) {
        return requestRepository.findByProtocolAndUserId(protocol, userId).orElse(null);
    }

    @Transactional
    public Request cancelRequest(String userId, String protocol, String reason) {
        Request request = findRequestByProtocol(userId, protocol);
        if (request == null) {
            throw new RuntimeException("Request not found");
        }

        if (!"ATIVO".equals(request.getStatus())) {
            throw new RuntimeException("Only requests with ATIVO status can be cancelled");
        }

        Instant cancelledAt = Instant.now();
        request.setStatus("CANCELADO");

        RequestHistory historyEntry = createHistoryEntry(request, cancelledAt, "CANCELLED: " + reason);
        request.getHistory().add(historyEntry);

        requestRepository.save(request);
        revokeAccessesByProtocol(userId, protocol);

        return request;
    }

    private void revokeAccessesByProtocol(String userId, String protocol) {
        accessRepository.updateStatusByUserIdAndProtocol(userId, protocol, "ATIVO", "REVOGADO");
    }

    @Transactional
    public Request renewAccess(String userId, String originalProtocol) {
        Request originalRequest = findRequestByProtocol(userId, originalProtocol);
        if (originalRequest == null) {
            throw new RuntimeException("Request not found");
        }

        if (!"ATIVO".equals(originalRequest.getStatus())) {
            throw new RuntimeException("Request status must be ATIVO");
        }

        List<Access> accesses = accessService.getAccessesByProtocol(userId, originalProtocol);
        if (accesses.isEmpty()) {
            throw new RuntimeException("No active accesses found for this request");
        }

        Access firstAccess = accesses.get(0);
        Instant expiresAt = firstAccess.getExpiresAt();
        long daysUntilExpiration = calculateDaysUntilExpiration(expiresAt);
        
        if (daysUntilExpiration >= 30) {
            throw new RuntimeException("Access can only be renewed when less than 30 days until expiration");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> moduleIds = originalRequest.getModules();
        String justification = "Renovação de acesso - Solicitação original: " + originalProtocol;

        String denialReason = businessRuleService.validateBusinessRules(userId, user.getDepartment(), moduleIds);
        String status = denialReason == null ? "ATIVO" : "NEGADO";

        String newProtocol = generateProtocol();
        Instant createdAt = Instant.now();
        Instant newExpiresAt = Instant.now().plusSeconds(180 * 24 * 60 * 60L);

        Request newRequest = new Request();
        newRequest.setProtocol(newProtocol);
        newRequest.setUserId(userId);
        newRequest.setUserDepartment(user.getDepartment());
        newRequest.setModules(moduleIds);
        newRequest.setJustification(justification);
        newRequest.setUrgent(originalRequest.isUrgent());
        newRequest.setStatus(status);
        newRequest.setCreatedAt(createdAt);
        newRequest.setExpiresAt(newExpiresAt);
        newRequest.setDenialReason(denialReason);

        List<RequestHistory> history = new ArrayList<>();
        history.add(createHistoryEntry(newRequest, createdAt, "CREATED"));
        history.add(createHistoryEntry(newRequest, createdAt, "RENEWAL"));
        if ("ATIVO".equals(status)) {
            history.add(createHistoryEntry(newRequest, createdAt, "APPROVED"));
        } else {
            history.add(createHistoryEntry(newRequest, createdAt, "DENIED"));
        }
        newRequest.setHistory(history);

        requestRepository.save(newRequest);

        if ("ATIVO".equals(status)) {
            revokeOldAccesses(accesses);
            createAccesses(userId, moduleIds, newProtocol, createdAt, newExpiresAt);
        }

        return newRequest;
    }

    private long calculateDaysUntilExpiration(Instant expiresAt) {
        Instant now = Instant.now();
        return (expiresAt.toEpochMilli() - now.toEpochMilli()) / (1000 * 60 * 60 * 24);
    }

    private void revokeOldAccesses(List<Access> accesses) {
        for (Access access : accesses) {
            if ("ATIVO".equals(access.getStatus())) {
                access.setStatus("REVOGADO");
                accessRepository.save(access);
            }
        }
    }

    private void createAccesses(String userId, List<String> moduleIds, String protocol, Instant grantedAt, Instant expiresAt) {
        for (String moduleId : moduleIds) {
            Access access = new Access();
            access.setUserId(userId);
            access.setModuleId(moduleId);
            access.setStatus("ATIVO");
            access.setGrantedAt(grantedAt);
            access.setExpiresAt(expiresAt);
            access.setRequestProtocol(protocol);
            accessRepository.save(access);
        }
    }

    private String generateProtocol() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = requestRepository.count();
        int sequence = (int) (count + 1);
        return String.format("SOL-%s-%04d", date, sequence);
    }

    private RequestHistory createHistoryEntry(Request request, Instant date, String action) {
        RequestHistory entry = new RequestHistory();
        entry.setRequest(request);
        entry.setDate(date);
        entry.setAction(action);
        return entry;
    }

    public List<Request> searchRequests(String userId, String search, String status, String startDate, String endDate, Boolean urgent) {
        List<Request> allRequests = requestRepository.findByUserId(userId);
        
        return allRequests.stream()
                .filter(req -> matchesSearch(req, search))
                .filter(req -> matchesStatus(req, status))
                .filter(req -> matchesDateRange(req, startDate, endDate))
                .filter(req -> matchesUrgent(req, urgent))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(Request request, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }
        String lowerSearch = search.toLowerCase();
        if (request.getProtocol() != null && request.getProtocol().toLowerCase().contains(lowerSearch)) {
            return true;
        }
        if (request.getModules() != null) {
            for (String moduleId : request.getModules()) {
                Module module = moduleService.findById(moduleId).orElse(null);
                if (module != null && module.getName().toLowerCase().contains(lowerSearch)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesStatus(Request request, String status) {
        if (status == null || status.trim().isEmpty()) {
            return true;
        }
        return status.equalsIgnoreCase(request.getStatus());
    }

    private boolean matchesDateRange(Request request, String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        Instant createdAt = request.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        if (startDate != null) {
            try {
                Instant start = Instant.parse(startDate);
                if (createdAt.isBefore(start)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        if (endDate != null) {
            try {
                Instant end = Instant.parse(endDate);
                if (createdAt.isAfter(end)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesUrgent(Request request, Boolean urgent) {
        if (urgent == null) {
            return true;
        }
        return request.isUrgent() == urgent;
    }
}
