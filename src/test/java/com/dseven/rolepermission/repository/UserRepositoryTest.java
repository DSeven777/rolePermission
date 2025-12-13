package com.dseven.rolepermission.repository;

import com.dseven.rolepermission.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password123", "test@example.com");
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("应该能够保存用户")
    void shouldSaveUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
    }

    @Test
    @DisplayName("应该能够根据用户名查找用户")
    void shouldFindUserByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @DisplayName("应该能够根据邮箱查找用户")
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @DisplayName("应该能够检查用户名是否存在")
    void shouldCheckIfUsernameExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    @DisplayName("应该能够检查邮箱是否存在")
    void shouldCheckIfEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("应该能够根据用户名或邮箱查找用户")
    void shouldFindUserByUsernameOrEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When - 通过用户名查找
        Optional<User> foundByUsername = userRepository.findByUsernameOrEmail("testuser");

        // When - 通过邮箱查找
        Optional<User> foundByEmail = userRepository.findByUsernameOrEmail("test@example.com");

        // Then
        assertTrue(foundByUsername.isPresent());
        assertTrue(foundByEmail.isPresent());
        assertEquals(foundByUsername.get().getId(), foundByEmail.get().getId());
    }

    @Test
    @DisplayName("应该能够查找所有启用的用户")
    void shouldFindAllEnabledUsers() {
        // Given
        User enabledUser = new User("enabled", "password", "enabled@example.com");
        enabledUser.setEnabled(true);
        entityManager.persistAndFlush(enabledUser);

        User disabledUser = new User("disabled", "password", "disabled@example.com");
        disabledUser.setEnabled(false);
        entityManager.persistAndFlush(disabledUser);

        // When
        Iterable<User> enabledUsers = userRepository.findAllEnabledUsers();

        // Then
        long count = 0;
        for (User user : enabledUsers) {
            assertTrue(user.getEnabled());
            count++;
        }
        assertEquals(2, count); // 包括setUp中创建的testUser
    }

    @Test
    @DisplayName("不应该找到不存在的用户")
    void shouldNotFindNonExistentUser() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
    }
}