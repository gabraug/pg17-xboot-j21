package com.pg17xbootj21.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(1);
    }

    @Test
    void createSession_ShouldStoreSession() {
        String token = "test-token-123";
        String email = "user@test.com";

        sessionService.createSession(token, email);

        assertTrue(sessionService.isValidSession(token));
        assertEquals(email, sessionService.getEmailByToken(token));
    }

    @Test
    void isValidSession_WhenTokenDoesNotExist_ShouldReturnFalse() {
        String token = "nonexistent-token";

        boolean result = sessionService.isValidSession(token);

        assertFalse(result);
    }

    @Test
    void isValidSession_WhenTokenExpired_ShouldReturnFalse() throws InterruptedException {
        String token = "expired-token";
        String email = "user@test.com";

        sessionService.createSession(token, email);
        
        Thread.sleep(1500);

        boolean result = sessionService.isValidSession(token);

        assertFalse(result);
    }

    @Test
    void getEmailByToken_WhenTokenExists_ShouldReturnEmail() {
        String token = "valid-token";
        String email = "user@test.com";

        sessionService.createSession(token, email);

        String result = sessionService.getEmailByToken(token);

        assertEquals(email, result);
    }

    @Test
    void getEmailByToken_WhenTokenDoesNotExist_ShouldReturnNull() {
        String token = "nonexistent-token";

        String result = sessionService.getEmailByToken(token);

        assertNull(result);
    }

    @Test
    void invalidateSession_ShouldRemoveSession() {
        String token = "token-to-invalidate";
        String email = "user@test.com";

        sessionService.createSession(token, email);
        assertTrue(sessionService.isValidSession(token));

        sessionService.invalidateSession(token);

        assertFalse(sessionService.isValidSession(token));
        assertNull(sessionService.getEmailByToken(token));
    }

    @Test
    void createSession_WithMultipleTokens_ShouldStoreAll() {
        String token1 = "token-1";
        String token2 = "token-2";
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";

        sessionService.createSession(token1, email1);
        sessionService.createSession(token2, email2);

        assertTrue(sessionService.isValidSession(token1));
        assertTrue(sessionService.isValidSession(token2));
        assertEquals(email1, sessionService.getEmailByToken(token1));
        assertEquals(email2, sessionService.getEmailByToken(token2));
    }
}

