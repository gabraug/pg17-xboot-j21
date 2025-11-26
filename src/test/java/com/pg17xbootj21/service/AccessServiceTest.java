package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.repository.AccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessRepository accessRepository;

    private AccessService accessService;

    private Access access1;
    private Access access2;
    private Access access3;
    private List<Access> accesses;

    @BeforeEach
    void setUp() {
        accessService = new AccessService(accessRepository);

        access1 = new Access();
        access1.setUserId("user1");
        access1.setModuleId("module1");
        access1.setStatus("ATIVO");
        access1.setRequestProtocol("SOL-20260101-0001");
        access1.setGrantedAt(Instant.parse("2026-01-01T00:00:00Z"));
        access1.setExpiresAt(Instant.parse("2026-07-01T00:00:00Z"));

        access2 = new Access();
        access2.setUserId("user1");
        access2.setModuleId("module2");
        access2.setStatus("ATIVO");
        access2.setRequestProtocol("SOL-20260101-0002");
        access2.setGrantedAt(Instant.parse("2026-01-01T00:00:00Z"));
        access2.setExpiresAt(Instant.parse("2026-07-01T00:00:00Z"));

        access3 = new Access();
        access3.setUserId("user2");
        access3.setModuleId("module1");
        access3.setStatus("REVOGADO");
        access3.setRequestProtocol("SOL-20260101-0003");
        access3.setGrantedAt(Instant.parse("2026-01-01T00:00:00Z"));
        access3.setExpiresAt(Instant.parse("2026-07-01T00:00:00Z"));

        accesses = Arrays.asList(access1, access2, access3);
    }

    @Test
    void getAllAccesses_WhenAccessesExist_ShouldReturnAllAccesses() {
        when(accessRepository.findAll()).thenReturn(accesses);

        List<Access> result = accessService.getAllAccesses();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(accessRepository, times(1)).findAll();
    }

    @Test
    void getAllAccesses_WhenNoAccessesExist_ShouldReturnEmptyList() {
        when(accessRepository.findAll()).thenReturn(Collections.emptyList());

        List<Access> result = accessService.getAllAccesses();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(accessRepository, times(1)).findAll();
    }

    @Test
    void getActiveAccessesByUserId_ShouldReturnOnlyActiveAccesses() {
        List<Access> activeAccesses = Arrays.asList(access1, access2);
        when(accessRepository.findByUserIdAndStatus(eq("user1"), eq("ATIVO"))).thenReturn(activeAccesses);

        List<Access> result = accessService.getActiveAccessesByUserId("user1");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> "ATIVO".equals(a.getStatus())));
        assertTrue(result.stream().allMatch(a -> "user1".equals(a.getUserId())));
        verify(accessRepository, times(1)).findByUserIdAndStatus(eq("user1"), eq("ATIVO"));
    }

    @Test
    void getActiveAccessesByUserId_WhenNoActiveAccesses_ShouldReturnEmptyList() {
        when(accessRepository.findByUserIdAndStatus(eq("user3"), eq("ATIVO"))).thenReturn(Collections.emptyList());

        List<Access> result = accessService.getActiveAccessesByUserId("user3");

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(accessRepository, times(1)).findByUserIdAndStatus(eq("user3"), eq("ATIVO"));
    }

    @Test
    void hasActiveAccess_WhenUserHasActiveAccess_ShouldReturnTrue() {
        when(accessRepository.findByUserIdAndModuleIdAndStatus(eq("user1"), eq("module1"), eq("ATIVO")))
                .thenReturn(Arrays.asList(access1));

        boolean result = accessService.hasActiveAccess("user1", "module1");

        assertTrue(result);
        verify(accessRepository, times(1)).findByUserIdAndModuleIdAndStatus(eq("user1"), eq("module1"), eq("ATIVO"));
    }

    @Test
    void hasActiveAccess_WhenUserDoesNotHaveActiveAccess_ShouldReturnFalse() {
        when(accessRepository.findByUserIdAndModuleIdAndStatus(eq("user1"), eq("module3"), eq("ATIVO")))
                .thenReturn(Collections.emptyList());

        boolean result = accessService.hasActiveAccess("user1", "module3");

        assertFalse(result);
        verify(accessRepository, times(1)).findByUserIdAndModuleIdAndStatus(eq("user1"), eq("module3"), eq("ATIVO"));
    }

    @Test
    void getActiveModuleIds_ShouldReturnListOfModuleIds() {
        List<Access> activeAccesses = Arrays.asList(access1, access2);
        when(accessRepository.findByUserIdAndStatus(eq("user1"), eq("ATIVO"))).thenReturn(activeAccesses);

        List<String> result = accessService.getActiveModuleIds("user1");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("module1"));
        assertTrue(result.contains("module2"));
        verify(accessRepository, times(1)).findByUserIdAndStatus(eq("user1"), eq("ATIVO"));
    }

    @Test
    void getAccessesByProtocol_ShouldReturnAccessesForProtocol() {
        when(accessRepository.findByUserIdAndRequestProtocolAndStatus(eq("user1"), eq("SOL-20260101-0001"), eq("ATIVO")))
                .thenReturn(Arrays.asList(access1));

        List<Access> result = accessService.getAccessesByProtocol("user1", "SOL-20260101-0001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SOL-20260101-0001", result.get(0).getRequestProtocol());
        assertEquals("ATIVO", result.get(0).getStatus());
        verify(accessRepository, times(1)).findByUserIdAndRequestProtocolAndStatus(eq("user1"), eq("SOL-20260101-0001"), eq("ATIVO"));
    }

    @Test
    void getAccessesByProtocol_WhenNoAccessesFound_ShouldReturnEmptyList() {
        when(accessRepository.findByUserIdAndRequestProtocolAndStatus(eq("user1"), eq("SOL-20260101-9999"), eq("ATIVO")))
                .thenReturn(Collections.emptyList());

        List<Access> result = accessService.getAccessesByProtocol("user1", "SOL-20260101-9999");

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(accessRepository, times(1)).findByUserIdAndRequestProtocolAndStatus(eq("user1"), eq("SOL-20260101-9999"), eq("ATIVO"));
    }
}
