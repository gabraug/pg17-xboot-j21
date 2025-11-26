package com.pg17xbootj21.service;

import com.pg17xbootj21.model.SessionInfo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private static final long DEFAULT_TOKEN_EXPIRATION_SECONDS = 15 * 60L;
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final long tokenExpirationSeconds;

    public SessionService() {
        this(DEFAULT_TOKEN_EXPIRATION_SECONDS);
    }

    public SessionService(long tokenExpirationSeconds) {
        this.tokenExpirationSeconds = tokenExpirationSeconds;
    }

    public void createSession(String token, String email) {
        Instant expiresAt = Instant.now().plusSeconds(tokenExpirationSeconds);
        sessions.put(token, new SessionInfo(email, expiresAt));
    }

    public boolean isValidSession(String token) {
        SessionInfo session = sessions.get(token);
        if (session == null) {
            return false;
        }
        if (Instant.now().isAfter(session.getExpiresAt())) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    public String getEmailByToken(String token) {
        SessionInfo session = sessions.get(token);
        return session != null ? session.getEmail() : null;
    }

    public void invalidateSession(String token) {
        sessions.remove(token);
    }
}

