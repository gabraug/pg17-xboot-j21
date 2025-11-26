package com.pg17xbootj21.util;

import com.pg17xbootj21.service.AuthService;
import com.pg17xbootj21.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthService authService;

    @Mock
    private SessionService sessionService;

    @Test
    void getUserIdFromRequest_WhenUserIdInAttribute_ShouldReturnUserId() {
        String userId = "user1";
        when(request.getAttribute(eq("userId"))).thenReturn(userId);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertEquals(userId, result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verifyNoInteractions(authService);
        verifyNoInteractions(sessionService);
    }

    @Test
    void getUserIdFromRequest_WhenValidBearerToken_ShouldReturnUserId() {
        String token = "valid-token-123";
        String userId = "user1";
        String authorization = "Bearer " + token;

        when(request.getAttribute(eq("userId"))).thenReturn(null);
        when(request.getHeader(eq("Authorization"))).thenReturn(authorization);
        when(sessionService.isValidSession(eq(token))).thenReturn(true);
        when(authService.getUserIdByToken(eq(token))).thenReturn(userId);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertEquals(userId, result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verify(request, times(1)).getHeader(eq("Authorization"));
        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, times(1)).getUserIdByToken(eq(token));
    }

    @Test
    void getUserIdFromRequest_WhenInvalidToken_ShouldReturnNull() {
        String token = "invalid-token";
        String authorization = "Bearer " + token;

        when(request.getAttribute(eq("userId"))).thenReturn(null);
        when(request.getHeader(eq("Authorization"))).thenReturn(authorization);
        when(sessionService.isValidSession(eq(token))).thenReturn(false);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertNull(result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verify(request, times(1)).getHeader(eq("Authorization"));
        verify(sessionService, times(1)).isValidSession(eq(token));
        verify(authService, never()).getUserIdByToken(eq(token));
    }

    @Test
    void getUserIdFromRequest_WhenNoAuthorizationHeader_ShouldReturnNull() {
        when(request.getAttribute(eq("userId"))).thenReturn(null);
        when(request.getHeader(eq("Authorization"))).thenReturn(null);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertNull(result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verify(request, times(1)).getHeader(eq("Authorization"));
        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
    }

    @Test
    void getUserIdFromRequest_WhenAuthorizationDoesNotStartWithBearer_ShouldReturnNull() {
        String authorization = "Basic token123";

        when(request.getAttribute(eq("userId"))).thenReturn(null);
        when(request.getHeader(eq("Authorization"))).thenReturn(authorization);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertNull(result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verify(request, times(1)).getHeader(eq("Authorization"));
        verifyNoInteractions(sessionService);
        verifyNoInteractions(authService);
    }

    @Test
    void getUserIdFromRequest_WhenTokenIsEmpty_ShouldReturnNull() {
        String authorization = "Bearer ";

        when(request.getAttribute(eq("userId"))).thenReturn(null);
        when(request.getHeader(eq("Authorization"))).thenReturn(authorization);
        when(sessionService.isValidSession(eq(""))).thenReturn(false);

        String result = SecurityUtil.getUserIdFromRequest(request, authService, sessionService);

        assertNull(result);
        verify(request, times(1)).getAttribute(eq("userId"));
        verify(request, times(1)).getHeader(eq("Authorization"));
        verify(sessionService, times(1)).isValidSession(eq(""));
        verifyNoInteractions(authService);
    }
}

