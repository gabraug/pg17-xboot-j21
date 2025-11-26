package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.model.Module;
import com.pg17xbootj21.model.Request;
import com.pg17xbootj21.model.User;
import com.pg17xbootj21.repository.AccessRepository;
import com.pg17xbootj21.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private AccessRepository accessRepository;

    @Mock
    private ModuleService moduleService;

    @Mock
    private AccessService accessService;

    @Mock
    private UserService userService;

    @Mock
    private BusinessRuleService businessRuleService;

    @InjectMocks
    private RequestService requestService;

    private User user;
    private Module module1;
    private Module module2;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(
                requestRepository,
                accessRepository,
                moduleService,
                accessService,
                userService,
                businessRuleService
        );

        user = new User();
        user.setId("user1");
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setDepartment("TI");

        module1 = new Module();
        module1.setId("module1");
        module1.setName("Module One");
        module1.setActive(true);
        module1.setAllowedDepartments(Arrays.asList("TI", "RH"));

        module2 = new Module();
        module2.setId("module2");
        module2.setName("Module Two");
        module2.setActive(true);
        module2.setAllowedDepartments(Arrays.asList("TI"));
    }

    @Test
    void createRequest_WhenValid_ShouldCreateRequest() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification for access";
        boolean urgent = false;

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(businessRuleService.validateBusinessRules(eq(userId), eq("TI"), eq(moduleIds))).thenReturn(null);
        when(requestRepository.save(argThat(req -> req.getUserId().equals(userId) && req.getStatus().equals("ATIVO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Request result = requestService.createRequest(userId, moduleIds, justification, urgent);

        assertNotNull(result);
        assertEquals("ATIVO", result.getStatus());
        assertEquals(userId, result.getUserId());
        assertEquals(justification, result.getJustification());
        assertEquals(urgent, result.isUrgent());
        assertNotNull(result.getProtocol());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpiresAt());
        verify(userService, times(1)).findById(eq(userId));
        verify(moduleService, times(1)).findById(eq("module1"));
        verify(businessRuleService, times(1)).validateBusinessRules(eq(userId), eq("TI"), eq(moduleIds));
        verify(requestRepository, times(1)).findByUserIdAndStatus(eq(userId), eq("ATIVO"));
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
        verify(requestRepository, times(1)).save(argThat(req -> req.getUserId().equals(userId) && req.getStatus().equals("ATIVO")));
    }

    @Test
    void createRequest_WhenUserNotFound_ShouldThrowException() {
        String userId = "nonexistent";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification";

        when(userService.findById(eq(userId))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
        verify(moduleService, never()).findById(eq("module1"));
    }

    @Test
    void createRequest_WhenModuleNotFound_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("nonexistent");
        String justification = "Valid justification";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("nonexistent"))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
        verify(moduleService, times(1)).findById(eq("nonexistent"));
    }

    @Test
    void createRequest_WhenModuleNotActive_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification";

        Module inactiveModule = new Module();
        inactiveModule.setId("module1");
        inactiveModule.setActive(false);

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(inactiveModule));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
        verify(moduleService, times(1)).findById(eq("module1"));
    }

    @Test
    void createRequest_WhenGenericJustification_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "teste";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void createRequest_WhenJustificationIsAaa_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "aaa";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void createRequest_WhenJustificationIsPreciso_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "preciso";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void createRequest_WhenJustificationIsEmpty_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void createRequest_WhenJustificationIsNull_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = null;

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void createRequest_WhenActiveRequestExists_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification";

        Request existingRequest = new Request();
        existingRequest.setProtocol("SOL-20260101-0001");
        existingRequest.setUserId(userId);
        existingRequest.setStatus("ATIVO");
        existingRequest.setModules(Arrays.asList("module1"));

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Arrays.asList(existingRequest));
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
        verify(requestRepository, times(1)).findByUserIdAndStatus(eq(userId), eq("ATIVO"));
    }

    @Test
    void createRequest_WhenUserAlreadyHasAccess_ShouldThrowException() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Arrays.asList("module1"));
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));

        assertThrows(RuntimeException.class, () -> {
            requestService.createRequest(userId, moduleIds, justification, false);
        });

        verify(userService, times(1)).findById(eq(userId));
        verify(accessService, times(1)).getActiveModuleIds(eq(userId));
    }

    @Test
    void createRequest_WhenBusinessRuleFails_ShouldCreateDeniedRequest() {
        String userId = "user1";
        List<String> moduleIds = Arrays.asList("module1");
        String justification = "Valid justification";
        String denialReason = "Limite de módulos ativos atingido";

        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(moduleService.findById(eq("module1"))).thenReturn(Optional.of(module1));
        when(requestRepository.findByUserIdAndStatus(eq(userId), eq("ATIVO"))).thenReturn(Collections.emptyList());
        when(accessService.getActiveModuleIds(eq(userId))).thenReturn(Collections.emptyList());
        when(businessRuleService.validateBusinessRules(eq(userId), eq("TI"), eq(moduleIds))).thenReturn(denialReason);
        when(requestRepository.save(argThat(req -> req.getUserId().equals(userId) && req.getStatus().equals("NEGADO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Request result = requestService.createRequest(userId, moduleIds, justification, false);

        assertNotNull(result);
        assertEquals("NEGADO", result.getStatus());
        assertEquals(denialReason, result.getDenialReason());
        verify(businessRuleService, times(1)).validateBusinessRules(eq(userId), eq("TI"), eq(moduleIds));
        verify(requestRepository, times(1)).save(argThat(req -> req.getUserId().equals(userId) && req.getStatus().equals("NEGADO")));
    }

    @Test
    void cancelRequest_WhenValid_ShouldCancelRequest() {
        String userId = "user1";
        String protocol = "SOL-20260101-0001";
        String reason = "No longer needed";

        Request request = new Request();
        request.setProtocol(protocol);
        request.setUserId(userId);
        request.setStatus("ATIVO");
        request.setHistory(new ArrayList<>());
        request.setModules(Arrays.asList("module1"));

        when(requestRepository.findByProtocolAndUserId(eq(protocol), eq(userId))).thenReturn(Optional.of(request));
        when(requestRepository.save(argThat(req -> req.getProtocol().equals(protocol) && req.getStatus().equals("CANCELADO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(accessRepository).updateStatusByUserIdAndProtocol(eq(userId), eq(protocol), eq("ATIVO"), eq("REVOGADO"));

        Request result = requestService.cancelRequest(userId, protocol, reason);

        assertNotNull(result);
        assertEquals("CANCELADO", result.getStatus());
        assertTrue(result.getHistory().stream().anyMatch(h -> h.getAction().contains("CANCELLED")));
        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(protocol), eq(userId));
        verify(requestRepository, times(1)).save(argThat(req -> req.getProtocol().equals(protocol) && req.getStatus().equals("CANCELADO")));
        verify(accessRepository, times(1)).updateStatusByUserIdAndProtocol(eq(userId), eq(protocol), eq("ATIVO"), eq("REVOGADO"));
    }

    @Test
    void cancelRequest_WhenRequestNotFound_ShouldThrowException() {
        String userId = "user1";
        String protocol = "SOL-20260101-0001";
        String reason = "No longer needed";

        when(requestRepository.findByProtocolAndUserId(eq(protocol), eq(userId))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            requestService.cancelRequest(userId, protocol, reason);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(protocol), eq(userId));
    }

    @Test
    void cancelRequest_WhenStatusNotActive_ShouldThrowException() {
        String userId = "user1";
        String protocol = "SOL-20260101-0001";
        String reason = "No longer needed";

        Request request = new Request();
        request.setProtocol(protocol);
        request.setUserId(userId);
        request.setStatus("NEGADO");

        when(requestRepository.findByProtocolAndUserId(eq(protocol), eq(userId))).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class, () -> {
            requestService.cancelRequest(userId, protocol, reason);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(protocol), eq(userId));
    }

    @Test
    void findRequestByProtocol_WhenRequestExists_ShouldReturnRequest() {
        String userId = "user1";
        String protocol = "SOL-20260101-0001";

        Request request = new Request();
        request.setProtocol(protocol);
        request.setUserId(userId);

        when(requestRepository.findByProtocolAndUserId(eq(protocol), eq(userId))).thenReturn(Optional.of(request));

        Request result = requestService.findRequestByProtocol(userId, protocol);

        assertNotNull(result);
        assertEquals(protocol, result.getProtocol());
        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(protocol), eq(userId));
    }

    @Test
    void findRequestByProtocol_WhenRequestDoesNotExist_ShouldReturnNull() {
        String userId = "user1";
        String protocol = "SOL-20260101-0001";

        when(requestRepository.findByProtocolAndUserId(eq(protocol), eq(userId))).thenReturn(Optional.empty());

        Request result = requestService.findRequestByProtocol(userId, protocol);

        assertNull(result);
        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(protocol), eq(userId));
    }

    @Test
    void renewAccess_WhenValid_ShouldCreateNewRequest() {
        String userId = "user1";
        String originalProtocol = "SOL-20260101-0001";
        Request originalRequest = new Request();
        originalRequest.setProtocol(originalProtocol);
        originalRequest.setUserId(userId);
        originalRequest.setStatus("ATIVO");
        originalRequest.setModules(Arrays.asList("module1"));
        originalRequest.setUrgent(false);
        originalRequest.setHistory(new ArrayList<>());

        Access access = new Access();
        access.setUserId(userId);
        access.setModuleId("module1");
        access.setStatus("ATIVO");
        access.setRequestProtocol(originalProtocol);
        access.setGrantedAt(Instant.now().minusSeconds(150 * 24 * 60 * 60L));
        access.setExpiresAt(Instant.now().plusSeconds(20 * 24 * 60 * 60L));

        when(requestRepository.findByProtocolAndUserId(eq(originalProtocol), eq(userId))).thenReturn(Optional.of(originalRequest));
        when(accessService.getAccessesByProtocol(eq(userId), eq(originalProtocol))).thenReturn(Arrays.asList(access));
        when(userService.findById(eq(userId))).thenReturn(Optional.of(user));
        when(businessRuleService.validateBusinessRules(eq(userId), eq("TI"), eq(Arrays.asList("module1")))).thenReturn(null);
        when(requestRepository.count()).thenReturn(1L);
        when(requestRepository.save(argThat(req -> req.getProtocol() != null && req.getStatus().equals("ATIVO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accessRepository.save(argThat(acc -> acc != null && acc.getUserId().equals(userId) && acc.getStatus().equals("REVOGADO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accessRepository.save(argThat(acc -> acc != null && acc.getUserId().equals(userId) && acc.getStatus().equals("ATIVO"))))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Request result = requestService.renewAccess(userId, originalProtocol);

        assertNotNull(result);
        assertNotNull(result.getProtocol());
        assertNotEquals(originalProtocol, result.getProtocol());
        assertEquals("ATIVO", result.getStatus());
        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(originalProtocol), eq(userId));
        verify(accessService, times(1)).getAccessesByProtocol(eq(userId), eq(originalProtocol));
        verify(userService, times(1)).findById(eq(userId));
    }

    @Test
    void renewAccess_WhenRequestNotFound_ShouldThrowException() {
        String userId = "user1";
        String originalProtocol = "SOL-20260101-0001";

        when(requestRepository.findByProtocolAndUserId(eq(originalProtocol), eq(userId))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            requestService.renewAccess(userId, originalProtocol);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(originalProtocol), eq(userId));
    }

    @Test
    void renewAccess_WhenStatusNotActive_ShouldThrowException() {
        String userId = "user1";
        String originalProtocol = "SOL-20260101-0001";
        Request originalRequest = new Request();
        originalRequest.setProtocol(originalProtocol);
        originalRequest.setStatus("NEGADO");

        when(requestRepository.findByProtocolAndUserId(eq(originalProtocol), eq(userId))).thenReturn(Optional.of(originalRequest));

        assertThrows(RuntimeException.class, () -> {
            requestService.renewAccess(userId, originalProtocol);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(originalProtocol), eq(userId));
    }

    @Test
    void renewAccess_WhenNoActiveAccesses_ShouldThrowException() {
        String userId = "user1";
        String originalProtocol = "SOL-20260101-0001";
        Request originalRequest = new Request();
        originalRequest.setProtocol(originalProtocol);
        originalRequest.setStatus("ATIVO");

        when(requestRepository.findByProtocolAndUserId(eq(originalProtocol), eq(userId))).thenReturn(Optional.of(originalRequest));
        when(accessService.getAccessesByProtocol(eq(userId), eq(originalProtocol))).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> {
            requestService.renewAccess(userId, originalProtocol);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(originalProtocol), eq(userId));
        verify(accessService, times(1)).getAccessesByProtocol(eq(userId), eq(originalProtocol));
    }

    @Test
    void renewAccess_WhenMoreThan30DaysUntilExpiration_ShouldThrowException() {
        String userId = "user1";
        String originalProtocol = "SOL-20260101-0001";
        Request originalRequest = new Request();
        originalRequest.setProtocol(originalProtocol);
        originalRequest.setStatus("ATIVO");
        originalRequest.setModules(Arrays.asList("module1"));

        Access access = new Access();
        access.setExpiresAt(Instant.now().plusSeconds(35 * 24 * 60 * 60L));

        when(requestRepository.findByProtocolAndUserId(eq(originalProtocol), eq(userId))).thenReturn(Optional.of(originalRequest));
        when(accessService.getAccessesByProtocol(eq(userId), eq(originalProtocol))).thenReturn(Arrays.asList(access));

        assertThrows(RuntimeException.class, () -> {
            requestService.renewAccess(userId, originalProtocol);
        });

        verify(requestRepository, times(1)).findByProtocolAndUserId(eq(originalProtocol), eq(userId));
        verify(accessService, times(1)).getAccessesByProtocol(eq(userId), eq(originalProtocol));
    }

    @Test
    void searchRequests_WhenNoFilters_ShouldReturnAllRequests() {
        String userId = "user1";
        Request request1 = new Request();
        request1.setProtocol("SOL-20260101-0001");
        request1.setStatus("ATIVO");
        request1.setCreatedAt(Instant.now());
        Request request2 = new Request();
        request2.setProtocol("SOL-20260101-0002");
        request2.setStatus("NEGADO");
        request2.setCreatedAt(Instant.now().minusSeconds(3600));

        when(requestRepository.findByUserId(eq(userId))).thenReturn(Arrays.asList(request1, request2));

        List<Request> result = requestService.searchRequests(userId, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(requestRepository, times(1)).findByUserId(eq(userId));
    }

    @Test
    void searchRequests_WhenFilteringByStatus_ShouldReturnFilteredRequests() {
        String userId = "user1";
        Request request1 = new Request();
        request1.setProtocol("SOL-20260101-0001");
        request1.setStatus("ATIVO");
        request1.setCreatedAt(Instant.now());
        Request request2 = new Request();
        request2.setProtocol("SOL-20260101-0002");
        request2.setStatus("NEGADO");
        request2.setCreatedAt(Instant.now().minusSeconds(3600));

        when(requestRepository.findByUserId(eq(userId))).thenReturn(Arrays.asList(request1, request2));

        List<Request> result = requestService.searchRequests(userId, null, "ATIVO", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ATIVO", result.get(0).getStatus());
        verify(requestRepository, times(1)).findByUserId(eq(userId));
    }

    @Test
    void searchRequests_WhenFilteringBySearch_ShouldReturnFilteredRequests() {
        String userId = "user1";
        Request request1 = new Request();
        request1.setProtocol("SOL-20260101-0001");
        request1.setStatus("ATIVO");
        request1.setCreatedAt(Instant.now());
        Request request2 = new Request();
        request2.setProtocol("SOL-20260102-0002");
        request2.setStatus("ATIVO");
        request2.setCreatedAt(Instant.now().minusSeconds(3600));

        when(requestRepository.findByUserId(eq(userId))).thenReturn(Arrays.asList(request1, request2));

        List<Request> result = requestService.searchRequests(userId, "20260101", null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SOL-20260101-0001", result.get(0).getProtocol());
        verify(requestRepository, times(1)).findByUserId(eq(userId));
    }

    @Test
    void searchRequests_WhenFilteringByUrgent_ShouldReturnFilteredRequests() {
        String userId = "user1";
        Request request1 = new Request();
        request1.setProtocol("SOL-20260101-0001");
        request1.setStatus("ATIVO");
        request1.setUrgent(true);
        request1.setCreatedAt(Instant.now());
        Request request2 = new Request();
        request2.setProtocol("SOL-20260101-0002");
        request2.setStatus("ATIVO");
        request2.setUrgent(false);
        request2.setCreatedAt(Instant.now().minusSeconds(3600));

        when(requestRepository.findByUserId(eq(userId))).thenReturn(Arrays.asList(request1, request2));

        List<Request> result = requestService.searchRequests(userId, null, null, null, null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isUrgent());
        verify(requestRepository, times(1)).findByUserId(eq(userId));
    }
}
