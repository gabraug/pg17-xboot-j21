package com.pg17xbootj21.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pg17xbootj21.config.SecurityConfig;
import com.pg17xbootj21.dto.CancelRequestRequest;
import com.pg17xbootj21.dto.CreateRequestRequest;
import com.pg17xbootj21.dto.ErrorResponse;
import com.pg17xbootj21.dto.RenewAccessRequest;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.model.RequestHistory;
import com.pg17xbootj21.security.SecurityInterceptor;
import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.RequestService;
import com.pg17xbootj21.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RequestController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private SecurityInterceptor securityInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestController requestController;

    private static final String VALID_JUSTIFICATION = "Solicitação detalhada com mais de vinte caracteres.";
    private static final String VALID_AUTHORIZATION = "Bearer valid-token-123";

    private String token;
    private String userId;
    private Request request;

    @BeforeEach
    void setUp() throws Exception {
        token = "valid-token-123";
        userId = "user1";

        when(securityInterceptor.preHandle(
                argThat(request -> true),
                argThat(response -> true),
                argThat(handler -> true)
        )).thenReturn(true);
        
        request = new Request();
        request.setProtocol("SOL-20260101-0001");
        request.setUserId(userId);
        request.setStatus("ATIVO");
        request.setModules(Arrays.asList("module1"));
        request.setJustification(VALID_JUSTIFICATION);
        request.setUrgent(false);
        request.setCreatedAt(Instant.parse("2025-01-01T10:00:00Z"));
        request.setExpiresAt(Instant.parse("2025-06-30T10:00:00Z"));

        RequestHistory historyEntry = new RequestHistory();
        historyEntry.setAction("CREATED");
        historyEntry.setDate(Instant.parse("2025-01-01T10:00:00Z"));
        request.setHistory(Arrays.asList(historyEntry));
    }

    private CreateRequestRequest buildCreateRequestPayload() {
        CreateRequestRequest createRequest = new CreateRequestRequest();
        createRequest.setModules(Arrays.asList("module1"));
        createRequest.setJustification(VALID_JUSTIFICATION);
        createRequest.setUrgent(false);
        return createRequest;
    }

    private CancelRequestRequest buildCancelRequestPayload(String reason) {
        CancelRequestRequest cancelRequestRequest = new CancelRequestRequest();
        cancelRequestRequest.setReason(reason);
        return cancelRequestRequest;
    }

    private RenewAccessRequest buildRenewAccessRequest(String protocol) {
        RenewAccessRequest renewAccessRequest = new RenewAccessRequest();
        renewAccessRequest.setRequestProtocol(protocol);
        return renewAccessRequest;
    }

    @Test
    void createRequest_WhenValid_ShouldReturnCreated() throws Exception {
        String authorization = VALID_AUTHORIZATION;
        CreateRequestRequest createRequest = buildCreateRequestPayload();

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.createRequest(
            eq(userId),
            eq(Arrays.asList("module1")),
            eq(VALID_JUSTIFICATION),
            eq(false)
        )).thenReturn(request);

        mockMvc.perform(post("/requests")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocol").value("SOL-20260101-0001"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).createRequest(
            eq(userId),
            eq(Arrays.asList("module1")),
            eq(VALID_JUSTIFICATION),
            eq(false)
        );
    }

    @Test
    void createRequest_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        String authorization = "Bearer invalid-token";
        CreateRequestRequest createRequest = new CreateRequestRequest();
        createRequest.setModules(Arrays.asList("module1"));
        createRequest.setJustification(VALID_JUSTIFICATION);

        when(sessionService.isValidSession(eq("invalid-token"))).thenReturn(false);

        mockMvc.perform(post("/requests")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(sessionService, times(1)).isValidSession(eq("invalid-token"));
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void createRequest_WhenNoModules_ShouldReturnBadRequest() throws Exception {
        String authorization = "Bearer " + token;
        CreateRequestRequest createRequest = new CreateRequestRequest();
        createRequest.setJustification(VALID_JUSTIFICATION);

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);

        mockMvc.perform(post("/requests")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("At least one module is required"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void createRequest_WhenMoreThanThreeModules_ShouldReturnBadRequest() throws Exception {
        String authorization = "Bearer " + token;
        CreateRequestRequest createRequest = new CreateRequestRequest();
        createRequest.setModules(Arrays.asList("module1", "module2", "module3", "module4"));
        createRequest.setJustification(VALID_JUSTIFICATION);

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);

        mockMvc.perform(post("/requests")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Must select between 1 and 3 modules"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void createRequest_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        CreateRequestRequest createRequest = buildCreateRequestPayload();

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(null);

        mockMvc.perform(post("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verifyNoInteractions(requestService);
    }

    @Test
    void createRequest_WhenBusinessRulesFail_ShouldReturnBadRequest() throws Exception {
        CreateRequestRequest createRequest = buildCreateRequestPayload();

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        )).thenThrow(new RuntimeException("Limite excedido"));

        mockMvc.perform(post("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("NEGADO"))
                .andExpect(jsonPath("$.denialReason").value("Limite excedido"))
                .andExpect(jsonPath("$.message").value("Solicitação negada. Motivo: Limite excedido"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        );
    }

    @Test
    void createRequest_WhenRequestIsDenied_ShouldReturnDenialMessage() throws Exception {
        CreateRequestRequest createRequest = buildCreateRequestPayload();
        Request deniedRequest = new Request();
        deniedRequest.setProtocol("SOL-20260101-0002");
        deniedRequest.setStatus("NEGADO");
        deniedRequest.setDenialReason("Departamento sem permissão");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        )).thenReturn(deniedRequest);

        mockMvc.perform(post("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEGADO"))
                .andExpect(jsonPath("$.denialReason").value("Departamento sem permissão"))
                .andExpect(jsonPath("$.message").value("Solicitação negada. Motivo: Departamento sem permissão"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        );
    }

    @Test
    void createRequest_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        CreateRequestRequest createRequest = buildCreateRequestPayload();

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        )).thenAnswer(invocation -> { throw new Exception("Database unavailable"); });

        mockMvc.perform(post("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Database unavailable"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).createRequest(
                eq(userId),
                eq(Arrays.asList("module1")),
                eq(VALID_JUSTIFICATION),
                eq(false)
        );
    }

    @Test
    void searchRequests_WhenValid_ShouldReturnRequests() throws Exception {
        List<Request> requests = Arrays.asList(request);

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.searchRequests(
            eq(userId),
            eq("Portal"),
            eq("ATIVO"),
            eq("2025-01-01"),
            eq("2025-01-31"),
            eq(true)
        )).thenReturn(requests);

        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("search", "Portal")
                .param("status", "ATIVO")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
                .param("urgent", "true")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].protocol").value("SOL-20260101-0001"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).searchRequests(
            eq(userId),
            eq("Portal"),
            eq("ATIVO"),
            eq("2025-01-01"),
            eq("2025-01-31"),
            eq(true)
        );
    }

    @Test
    void searchRequests_WhenPageIsNegative_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page number must be greater than or equal to 0"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenSizeIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenStatusIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("status", "pendente")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status must be one of: ATIVO, NEGADO, CANCELADO"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenStartDateIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("startDate", "2025/01/01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid start date format. Expected format: YYYY-MM-DD"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenEndDateIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .param("endDate", "2025-13-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid end date format. Expected format: YYYY-MM-DD"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(null);

        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verifyNoInteractions(requestService);
    }

    @Test
    void searchRequests_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.searchRequests(
                eq(userId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
        )).thenThrow(new IllegalStateException("Repository failure"));

        mockMvc.perform(get("/requests")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Repository failure"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).searchRequests(
                eq(userId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
        );
    }

    @Test
    void getRequestDetails_WhenValid_ShouldReturnRequest() throws Exception {
        String authorization = "Bearer " + token;
        String protocol = "SOL-20260101-0001";

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.findRequestByProtocol(eq(userId), eq(protocol))).thenReturn(request);

        mockMvc.perform(get("/requests/" + protocol)
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocol").value(protocol))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.history[0].action").value("CREATED"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).findRequestByProtocol(eq(userId), eq(protocol));
    }

    @Test
    void getRequestDetails_WhenRequestNotFound_ShouldReturnNotFound() throws Exception {
        String authorization = "Bearer " + token;
        String protocol = "SOL-20260101-0001";

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.findRequestByProtocol(eq(userId), eq(protocol))).thenReturn(null);

        mockMvc.perform(get("/requests/" + protocol)
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).findRequestByProtocol(eq(userId), eq(protocol));
    }

    @Test
    void getRequestDetails_WhenProtocolIsBlank_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/ ")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Protocol is required"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void getRequestDetails_WhenProtocolFormatIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/INVALID")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid protocol format. Expected format: SOL-YYYYMMDD-NNNN"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void getRequestDetails_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        mockMvc.perform(get("/requests/SOL-20260101-0001")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void getRequestDetails_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(null);

        mockMvc.perform(get("/requests/SOL-20260101-0001")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verifyNoInteractions(requestService);
    }

    @Test
    void getRequestDetails_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        String protocol = "SOL-20260101-0001";

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.findRequestByProtocol(eq(userId), eq(protocol)))
                .thenThrow(new IllegalStateException("Repository unavailable"));

        mockMvc.perform(get("/requests/" + protocol)
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Repository unavailable"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).findRequestByProtocol(eq(userId), eq(protocol));
    }

    @Test
    void getRequestDetails_WhenProtocolIsNull_ShouldReturnBadRequestDirectly() {
        ResponseEntity<?> response = requestController.getRequestDetails(VALID_AUTHORIZATION, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals("Protocol is required", body.getMessage());

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void renewAccess_WhenValid_ShouldReturnCreated() throws Exception {
        RenewAccessRequest renewRequest = buildRenewAccessRequest("SOL-20260101-0001");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.renewAccess(eq(userId), eq("SOL-20260101-0001"))).thenReturn(request);

        mockMvc.perform(post("/requests/renew")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocol").value("SOL-20260101-0001"))
                .andExpect(jsonPath("$.status").value("ATIVO"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).renewAccess(eq(userId), eq("SOL-20260101-0001"));
    }

    @Test
    void renewAccess_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        RenewAccessRequest renewRequest = buildRenewAccessRequest("SOL-20260101-0001");

        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        mockMvc.perform(post("/requests/renew")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renewRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void renewAccess_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        RenewAccessRequest renewRequest = buildRenewAccessRequest("SOL-20260101-0001");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(null);

        mockMvc.perform(post("/requests/renew")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renewRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verifyNoInteractions(requestService);
    }

    @Test
    void renewAccess_WhenRuntimeExceptionOccurs_ShouldReturnBadRequest() throws Exception {
        RenewAccessRequest renewRequest = buildRenewAccessRequest("SOL-20260101-0001");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.renewAccess(eq(userId), eq("SOL-20260101-0001")))
                .thenThrow(new RuntimeException("Não é possível renovar agora"));

        mockMvc.perform(post("/requests/renew")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renewRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Não é possível renovar agora"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).renewAccess(eq(userId), eq("SOL-20260101-0001"));
    }

    @Test
    void renewAccess_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        RenewAccessRequest renewRequest = buildRenewAccessRequest("SOL-20260101-0001");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.renewAccess(eq(userId), eq("SOL-20260101-0001")))
                .thenAnswer(invocation -> { throw new Exception("Falha geral"); });

        mockMvc.perform(post("/requests/renew")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(renewRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Falha geral"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).renewAccess(eq(userId), eq("SOL-20260101-0001"));
    }

    @Test
    void cancelRequest_WhenValid_ShouldReturnOk() throws Exception {
        String authorization = "Bearer " + token;
        String protocol = "SOL-20260101-0001";
        String reason = "No longer needed";

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.cancelRequest(eq(userId), eq(protocol), eq(reason))).thenReturn(request);

        mockMvc.perform(post("/requests/" + protocol + "/cancel")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history[0].action").value("CREATED"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).cancelRequest(eq(userId), eq(protocol), eq(reason));
    }

    @Test
    void cancelRequest_WhenProtocolMissing_ShouldReturnBadRequestDirectly() {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");

        ResponseEntity<?> response = requestController.cancelRequest(VALID_AUTHORIZATION, " ", cancelRequestRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals("Protocol is required", body.getMessage());

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void cancelRequest_WhenProtocolFormatInvalid_ShouldReturnBadRequest() throws Exception {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");

        mockMvc.perform(post("/requests/INVALID/cancel")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequestRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid protocol format. Expected format: SOL-YYYYMMDD-NNNN"));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void cancelRequest_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");
        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        mockMvc.perform(post("/requests/SOL-20260101-0001/cancel")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequestRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verifyNoInteractions(authService);
        verifyNoInteractions(requestService);
    }

    @Test
    void cancelRequest_WhenUserNotFound_ShouldReturnUnauthorized() throws Exception {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(null);

        mockMvc.perform(post("/requests/SOL-20260101-0001/cancel")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequestRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verifyNoInteractions(requestService);
    }

    @Test
    void cancelRequest_WhenRuntimeExceptionOccurs_ShouldReturnBadRequest() throws Exception {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.cancelRequest(eq(userId), eq("SOL-20260101-0001"), eq("Motivo detalhado válido")))
                .thenThrow(new RuntimeException("Já cancelado"));

        mockMvc.perform(post("/requests/SOL-20260101-0001/cancel")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequestRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já cancelado"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).cancelRequest(eq(userId), eq("SOL-20260101-0001"), eq("Motivo detalhado válido"));
    }

    @Test
    void cancelRequest_WhenUnexpectedExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        CancelRequestRequest cancelRequestRequest = buildCancelRequestPayload("Motivo detalhado válido");

        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);
        when(requestService.cancelRequest(eq(userId), eq("SOL-20260101-0001"), eq("Motivo detalhado válido")))
                .thenAnswer(invocation -> { throw new Exception("Erro interno"); });

        mockMvc.perform(post("/requests/SOL-20260101-0001/cancel")
                .header("Authorization", VALID_AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequestRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Erro interno"));

        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
        verify(requestService, times(1)).cancelRequest(eq(userId), eq("SOL-20260101-0001"), eq("Motivo detalhado válido"));
    }
}

