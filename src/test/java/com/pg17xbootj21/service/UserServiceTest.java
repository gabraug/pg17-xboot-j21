package com.pg17xbootj21.service;

import com.pg17xbootj21.model.User;
import com.pg17xbootj21.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({InstancioExtension.class, MockitoExtension.class})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        user1 = Instancio.of(User.class)
                .set(field(User::getId), "user1")
                .set(field(User::getEmail), "user1@test.com")
                .set(field(User::getName), "User One")
                .set(field(User::getDepartment), "TI")
                .set(field(User::getPassword), "hashedPassword1")
                .create();

        user2 = Instancio.of(User.class)
                .set(field(User::getId), "user2")
                .set(field(User::getEmail), "user2@test.com")
                .set(field(User::getName), "User Two")
                .set(field(User::getDepartment), "RH")
                .set(field(User::getPassword), "hashedPassword2")
                .create();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));

        Optional<User> result = userService.findById("user1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getId());
        assertEquals("user1@test.com", result.get().getEmail());
        verify(userRepository, times(1)).findById("user1");
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findById("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById("nonexistent");
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));

        Optional<User> result = userService.findByEmail("user2@test.com");

        assertTrue(result.isPresent());
        assertEquals("user2", result.get().getId());
        assertEquals("user2@test.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("user2@test.com");
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nonexistent@test.com");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
    }
}
