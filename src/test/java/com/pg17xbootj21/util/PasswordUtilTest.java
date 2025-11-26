package com.pg17xbootj21.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hash_ShouldGenerateDifferentHashForSamePassword() {
        String password = "testPassword123";
        
        String hash1 = PasswordUtil.hash(password);
        String hash2 = PasswordUtil.hash(password);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
        assertTrue(hash1.length() > 20);
        assertTrue(hash2.length() > 20);
    }

    @Test
    void hash_ShouldGenerateValidHash() {
        String password = "testPassword123";
        
        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    void matches_WhenPasswordMatches_ShouldReturnTrue() {
        String password = "testPassword123";
        String hash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.matches(password, hash);

        assertTrue(result);
    }

    @Test
    void matches_WhenPasswordDoesNotMatch_ShouldReturnFalse() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.matches(wrongPassword, hash);

        assertFalse(result);
    }

    @Test
    void matches_WhenHashIsInvalid_ShouldReturnFalse() {
        String password = "testPassword123";
        String invalidHash = "invalidHash";

        boolean result = PasswordUtil.matches(password, invalidHash);

        assertFalse(result);
    }

    @Test
    void hash_WithEmptyPassword_ShouldGenerateHash() {
        String password = "";
        
        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void matches_WithEmptyPassword_ShouldWork() {
        String password = "";
        String hash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.matches(password, hash);

        assertTrue(result);
    }

    @Test
    void hash_WithSpecialCharacters_ShouldGenerateHash() {
        String password = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void matches_WithSpecialCharacters_ShouldWork() {
        String password = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String hash = PasswordUtil.hash(password);

        boolean result = PasswordUtil.matches(password, hash);

        assertTrue(result);
    }
}

