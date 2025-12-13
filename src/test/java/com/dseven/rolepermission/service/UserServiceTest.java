package com.dseven.rolepermission.service;

import com.dseven.rolepermission.entity.User;
import com.dseven.rolepermission.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password123", "test@example.com");
        testUser.setId(1L);
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("应该能够创建用户")
    void shouldCreateUser() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User createdUser = userService.createUser(testUser);

        // Then
        assertNotNull(createdUser);
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("创建用户时如果用户名已存在应该抛出异常")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(testUser));
        assertEquals("Username already exists: " + testUser.getUsername(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户时如果邮箱已存在应该抛出异常")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(testUser));
        assertEquals("Email already exists: " + testUser.getEmail(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该能够获取所有用户")
    void shouldGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("应该能够根据ID获取用户")
    void shouldGetUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("应该能够根据用户名获取用户")
    void shouldGetUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("应该能够更新用户")
    void shouldUpdateUser() {
        // Given
        User updatedDetails = new User("updateduser", "newpassword", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(1L, updatedDetails);

        // Then
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("更新不存在的用户应该抛出异常")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(1L, testUser));
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该能够删除用户")
    void shouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除不存在的用户应该抛出异常")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(1L));
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("应该能够切换用户状态")
    void shouldToggleUserStatus() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.toggleUserStatus(1L);

        // Then
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该能够获取所有启用的用户")
    void shouldGetAllEnabledUsers() {
        // Given
        List<User> enabledUsers = Arrays.asList(testUser);
        when(userRepository.findAllEnabledUsers()).thenReturn(enabledUsers);

        // When
        List<User> result = userService.getAllEnabledUsers();

        // Then
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAllEnabledUsers();
    }
}