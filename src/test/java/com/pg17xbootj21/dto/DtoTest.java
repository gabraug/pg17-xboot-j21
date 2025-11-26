package com.pg17xbootj21.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void dtoGettersAndSetters_ShouldWorkForAllClasses() {
        ErrorResponse errorResponse = new ErrorResponse("Bad Request", "Erro", 400);
        assertEquals("Erro", errorResponse.getMessage());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("secret");
        assertEquals("user@test.com", loginRequest.getEmail());
        assertEquals("secret", loginRequest.getPassword());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken("token");
        loginResponse.setName("User");
        loginResponse.setEmail("user@test.com");
        assertEquals("token", loginResponse.getAccessToken());
        assertEquals("User", loginResponse.getName());

        CreateRequestRequest createRequestRequest = new CreateRequestRequest();
        createRequestRequest.setModules(List.of("module1", "module2"));
        createRequestRequest.setJustification("Justificativa detalhada para acesso.");
        createRequestRequest.setUrgent(true);
        assertTrue(createRequestRequest.isUrgent());
        assertEquals(2, createRequestRequest.getModules().size());

        CreateRequestResponse createRequestResponse = new CreateRequestResponse();
        createRequestResponse.setProtocol("SOL-20260101-0001");
        createRequestResponse.setStatus("ATIVO");
        createRequestResponse.setMessage("Mensagem");
        createRequestResponse.setDenialReason("Motivo");
        assertEquals("SOL-20260101-0001", createRequestResponse.getProtocol());
        assertEquals("Mensagem", createRequestResponse.getMessage());

        HistoryEntryResponse historyEntryResponse = new HistoryEntryResponse();
        historyEntryResponse.setAction("CREATED");
        historyEntryResponse.setDate("2025-01-01T00:00:00Z");
        assertEquals("CREATED", historyEntryResponse.getAction());

        ModuleResponse moduleResponse = new ModuleResponse();
        moduleResponse.setName("Portal");
        moduleResponse.setDescription("Descrição");
        moduleResponse.setAllowedDepartments(List.of("TI"));
        moduleResponse.setIncompatibleModules(List.of("mod2"));
        moduleResponse.setActive(true);
        assertTrue(moduleResponse.isActive());
        assertEquals(1, moduleResponse.getAllowedDepartments().size());

        PagedResponse<String> pagedResponse = new PagedResponse<>(List.of("item"), 0, 10, 1);
        assertEquals(1, pagedResponse.getContent().size());
        assertEquals(0, pagedResponse.getPage());

        RenewAccessRequest renewAccessRequest = new RenewAccessRequest();
        renewAccessRequest.setRequestProtocol("SOL-20260101-0001");
        assertEquals("SOL-20260101-0001", renewAccessRequest.getRequestProtocol());

        CancelRequestRequest cancelRequestRequest = new CancelRequestRequest();
        cancelRequestRequest.setReason("Motivo para cancelamento válido");
        assertEquals("Motivo para cancelamento válido", cancelRequestRequest.getReason());

        RequestSummaryResponse summaryResponse = new RequestSummaryResponse();
        summaryResponse.setProtocol("SOL-20260101-0001");
        summaryResponse.setModules(List.of("module1"));
        summaryResponse.setStatus("ATIVO");
        summaryResponse.setJustification("Justificativa");
        summaryResponse.setUrgent(false);
        summaryResponse.setCreatedAt("2025-01-01T00:00:00Z");
        summaryResponse.setExpiresAt("2025-06-01T00:00:00Z");
        summaryResponse.setDenialReason("Motivo");
        assertEquals("SOL-20260101-0001", summaryResponse.getProtocol());

        RequestDetailsResponse detailsResponse = new RequestDetailsResponse();
        detailsResponse.setProtocol("SOL-20260101-0001");
        detailsResponse.setUserId("user1");
        detailsResponse.setUserDepartment("TI");
        detailsResponse.setModules(List.of("module1"));
        detailsResponse.setJustification("Justificativa");
        detailsResponse.setUrgent(true);
        detailsResponse.setStatus("ATIVO");
        detailsResponse.setCreatedAt("2025-01-01T00:00:00Z");
        detailsResponse.setExpiresAt("2025-06-01T00:00:00Z");
        detailsResponse.setDenialReason("Motivo");
        detailsResponse.setHistory(List.of(historyEntryResponse));
        assertEquals("user1", detailsResponse.getUserId());
        assertEquals(1, detailsResponse.getHistory().size());

        SearchRequestsRequest searchRequestsRequest = new SearchRequestsRequest();
        searchRequestsRequest.setSearch("Portal");
        searchRequestsRequest.setStatus("ATIVO");
        searchRequestsRequest.setStartDate("2025-01-01");
        searchRequestsRequest.setEndDate("2025-01-31");
        searchRequestsRequest.setUrgent(true);
        assertEquals("Portal", searchRequestsRequest.getSearch());
        assertTrue(searchRequestsRequest.getUrgent());
    }
}

