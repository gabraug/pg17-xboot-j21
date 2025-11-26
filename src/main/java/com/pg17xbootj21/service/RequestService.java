package com.pg17xbootj21.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.model.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final ObjectMapper objectMapper;
    private final ModuleService moduleService;
    private final AccessService accessService;
    private final UserService userService;
    private final BusinessRuleService businessRuleService;
    private static final String REQUESTS_FILE = System.getProperty("user.dir") + "/data/requests.json";
    private static final String ACCESSES_FILE = System.getProperty("user.dir") + "/data/accesses.json";
    private static final List<String> GENERIC_WORDS = List.of("teste", "aaa", "preciso");

    public RequestService(ModuleService moduleService, AccessService accessService, UserService userService, BusinessRuleService businessRuleService) {
        this.objectMapper = new ObjectMapper();
        this.moduleService = moduleService;
        this.accessService = accessService;
        this.userService = userService;
        this.businessRuleService = businessRuleService;
    }

    public Request createRequest(String userId, List<String> moduleIds, String justification, boolean urgent) throws IOException {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateRequest(userId, moduleIds, justification);

        String protocol = generateProtocol();
        String createdAt = Instant.now().toString();
        String expiresAt = Instant.now().plusSeconds(180 * 24 * 60 * 60L).toString();

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

        List<Request.HistoryEntry> history = new ArrayList<>();
        history.add(createHistoryEntry(createdAt, "CREATED"));
        if ("ATIVO".equals(status)) {
            history.add(createHistoryEntry(createdAt, "APPROVED"));
        } else {
            history.add(createHistoryEntry(createdAt, "DENIED"));
        }
        request.setHistory(history);

        saveRequest(request);

        if ("ATIVO".equals(status)) {
            createAccesses(userId, moduleIds, protocol, createdAt, expiresAt);
        }

        return request;
    }

    private void validateRequest(String userId, List<String> moduleIds, String justification) throws IOException {
        List<Request> activeRequests = getActiveRequestsByUserId(userId);
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

    private List<Request> getActiveRequestsByUserId(String userId) throws IOException {
        return getAllRequests().stream()
                .filter(req -> req.getUserId().equals(userId) && "ATIVO".equals(req.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Request> getAllRequests() throws IOException {
        File file = new File(REQUESTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(file, new TypeReference<List<Request>>() {});
    }

    public Request findRequestByProtocol(String userId, String protocol) throws IOException {
        return getAllRequests().stream()
                .filter(req -> req.getProtocol().equals(protocol) && req.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public Request cancelRequest(String userId, String protocol, String reason) throws IOException {
        Request request = findRequestByProtocol(userId, protocol);
        if (request == null) {
            throw new RuntimeException("Request not found");
        }

        if (!"ATIVO".equals(request.getStatus())) {
            throw new RuntimeException("Only requests with ATIVO status can be cancelled");
        }

        String cancelledAt = Instant.now().toString();
        request.setStatus("CANCELADO");

        Request.HistoryEntry historyEntry = createHistoryEntry(cancelledAt, "CANCELLED: " + reason);
        request.getHistory().add(historyEntry);

        updateRequest(request);
        revokeAccessesByProtocol(userId, protocol);

        return request;
    }

    private void revokeAccessesByProtocol(String userId, String protocol) throws IOException {
        List<Access> allAccesses = accessService.getAllAccesses();
        for (Access access : allAccesses) {
            if (access.getUserId().equals(userId) 
                    && access.getRequestProtocol().equals(protocol)
                    && "ATIVO".equals(access.getStatus())) {
                access.setStatus("REVOGADO");
            }
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ACCESSES_FILE), allAccesses);
    }

    public Request renewAccess(String userId, String originalProtocol) throws IOException {
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
        String expiresAt = firstAccess.getExpiresAt();
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
        String createdAt = Instant.now().toString();
        String newExpiresAt = Instant.now().plusSeconds(180 * 24 * 60 * 60L).toString();

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

        List<Request.HistoryEntry> history = new ArrayList<>();
        history.add(createHistoryEntry(createdAt, "CREATED"));
        history.add(createHistoryEntry(createdAt, "RENEWAL"));
        if ("ATIVO".equals(status)) {
            history.add(createHistoryEntry(createdAt, "APPROVED"));
        } else {
            history.add(createHistoryEntry(createdAt, "DENIED"));
        }
        newRequest.setHistory(history);

        saveRequest(newRequest);

        if ("ATIVO".equals(status)) {
            revokeOldAccesses(accesses);
            createAccesses(userId, moduleIds, newProtocol, createdAt, newExpiresAt);
        }

        return newRequest;
    }

    private long calculateDaysUntilExpiration(String expiresAt) {
        try {
            Instant expiration = Instant.parse(expiresAt);
            Instant now = Instant.now();
            return (expiration.toEpochMilli() - now.toEpochMilli()) / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }

    private void revokeOldAccesses(List<Access> accesses) throws IOException {
        List<Access> allAccesses = accessService.getAllAccesses();
        for (Access oldAccess : accesses) {
            for (Access access : allAccesses) {
                if (access.getUserId().equals(oldAccess.getUserId()) 
                        && access.getModuleId().equals(oldAccess.getModuleId())
                        && access.getRequestProtocol().equals(oldAccess.getRequestProtocol())
                        && "ATIVO".equals(access.getStatus())) {
                    access.setStatus("REVOGADO");
                }
            }
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ACCESSES_FILE), allAccesses);
    }

    private void saveRequest(Request request) throws IOException {
        List<Request> requests = getAllRequests();
        requests.add(request);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(REQUESTS_FILE), requests);
    }

    private void updateRequest(Request updatedRequest) throws IOException {
        List<Request> requests = getAllRequests();
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getProtocol().equals(updatedRequest.getProtocol())) {
                requests.set(i, updatedRequest);
                break;
            }
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(REQUESTS_FILE), requests);
    }

    private void createAccesses(String userId, List<String> moduleIds, String protocol, String grantedAt, String expiresAt) throws IOException {
        List<Access> accesses = accessService.getAllAccesses();
        
        for (String moduleId : moduleIds) {
            Access access = new Access();
            access.setUserId(userId);
            access.setModuleId(moduleId);
            access.setStatus("ATIVO");
            access.setGrantedAt(grantedAt);
            access.setExpiresAt(expiresAt);
            access.setRequestProtocol(protocol);
            accesses.add(access);
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(ACCESSES_FILE), accesses);
    }

    private String generateProtocol() throws IOException {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Request> requests = getAllRequests();
        int sequence = requests.size() + 1;
        return String.format("SOL-%s-%04d", date, sequence);
    }

    private Request.HistoryEntry createHistoryEntry(String date, String action) {
        Request.HistoryEntry entry = new Request.HistoryEntry();
        entry.setDate(date);
        entry.setAction(action);
        return entry;
    }

    public List<Request> searchRequests(String userId, String search, String status, String startDate, String endDate, Boolean urgent) throws IOException {
        List<Request> allRequests = getAllRequests();
        
        return allRequests.stream()
                .filter(req -> req.getUserId().equals(userId))
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
                try {
                    Module module = moduleService.findById(moduleId).orElse(null);
                    if (module != null && module.getName().toLowerCase().contains(lowerSearch)) {
                        return true;
                    }
                } catch (IOException e) {
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
        String createdAt = request.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        if (startDate != null && createdAt.compareTo(startDate) < 0) {
            return false;
        }
        if (endDate != null && createdAt.compareTo(endDate) > 0) {
            return false;
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

