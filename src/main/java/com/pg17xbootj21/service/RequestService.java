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

    private void saveRequest(Request request) throws IOException {
        List<Request> requests = getAllRequests();
        requests.add(request);
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
}

