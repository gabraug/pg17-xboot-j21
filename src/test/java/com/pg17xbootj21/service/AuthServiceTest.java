package com.pg17xbootj21.service;

import com.pg17xbootj21.model.User;
import com.pg17xbootj21.util.PasswordUtil;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
class AuthServiceTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = PasswordUtil.hash("password123");
        
        user = Instancio.of(User.class)
                .set(field(User::getId), "user1")
                .set(field(User::getEmail), "user@test.com")
                .set(field(User::getPassword), hashedPassword)
                .set(field(User::getName), "Test User")
                .set(field(User::getDepartment), "TI")
                .create();
    }

    @Test
    void authenticate_WhenCredentialsAreValid_ShouldReturnUser() {
        when(userService.findByEmail(eq("user@test.com"))).thenReturn(Optional.of(user));

        User result = authService.authenticate("user@test.com", "password123");

        assertNotNull(result);
        assertEquals("user1", result.getId());
        assertEquals("user@test.com", result.getEmail());
        verify(userService, times(1)).findByEmail(eq("user@test.com"));
    }

    @Test
    void authenticate_WhenEmailDoesNotExist_ShouldReturnNull() {
        when(userService.findByEmail(eq("nonexistent@test.com"))).thenReturn(Optional.empty());

        User result = authService.authenticate("nonexistent@test.com", "password123");

        assertNull(result);
        verify(userService, times(1)).findByEmail(eq("nonexistent@test.com"));
    }

    @Test
    void authenticate_WhenPasswordIsInvalid_ShouldReturnNull() {
        when(userService.findByEmail(eq("user@test.com"))).thenReturn(Optional.of(user));

        User result = authService.authenticate("user@test.com", "wrongpassword");

        assertNull(result);
        verify(userService, times(1)).findByEmail(eq("user@test.com"));
    }

    @Test
    void authenticate_WhenServiceThrowsException_ShouldThrowRuntimeException() {
        when(userService.findByEmail(eq("user@test.com"))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("user@test.com", "password123");
        });
        
        verify(userService, times(1)).findByEmail(eq("user@test.com"));
    }

    @Test
    void createSession_ShouldCreateSessionAndReturnToken() {
        String email = "user@test.com";
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(sessionService).createSession(tokenCaptor.capture(), eq(email));

        String token = authService.createSession(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        String capturedToken = tokenCaptor.getValue();
        assertEquals(token, capturedToken);
        verify(sessionService, times(1)).createSession(eq(capturedToken), eq(email));
    }

    @Test
    void getUserIdByToken_WhenTokenIsValid_ShouldReturnUserId() {
        String token = "valid-token";
        String email = "user@test.com";

        when(sessionService.getEmailByToken(eq(token))).thenReturn(email);
        when(userService.findByEmail(eq(email))).thenReturn(Optional.of(user));

        String result = authService.getUserIdByToken(token);

        assertEquals("user1", result);
        verify(sessionService, times(1)).getEmailByToken(eq(token));
        verify(userService, times(1)).findByEmail(eq(email));
    }

    @Test
    void getUserIdByToken_WhenTokenIsInvalid_ShouldReturnNull() {
        String token = "invalid-token";

        when(sessionService.getEmailByToken(eq(token))).thenReturn(null);

        String result = authService.getUserIdByToken(token);

        assertNull(result);
        verify(sessionService, times(1)).getEmailByToken(eq(token));
        verifyNoInteractions(userService);
    }

    @Test
    void getUserIdByToken_WhenUserDoesNotExist_ShouldReturnNull() {
        String token = "valid-token";
        String email = "nonexistent@test.com";

        when(sessionService.getEmailByToken(eq(token))).thenReturn(email);
        when(userService.findByEmail(eq(email))).thenReturn(Optional.empty());

        String result = authService.getUserIdByToken(token);

        assertNull(result);
        verify(sessionService, times(1)).getEmailByToken(eq(token));
        verify(userService, times(1)).findByEmail(eq(email));
    }

    @Test
    void getUserIdByToken_WhenServiceThrowsException_ShouldThrowRuntimeException() {
        String token = "valid-token";
        String email = "user@test.com";

        when(sessionService.getEmailByToken(eq(token))).thenReturn(email);
        when(userService.findByEmail(eq(email))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            authService.getUserIdByToken(token);
        });
        
        verify(sessionService, times(1)).getEmailByToken(eq(token));
        verify(userService, times(1)).findByEmail(eq(email));
    }
}

