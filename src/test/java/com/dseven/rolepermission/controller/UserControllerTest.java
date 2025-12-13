package com.dseven.rolepermission.controller;

import com.dseven.rolepermission.entity.User;
import com.dseven.rolepermission.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void shouldCreateUser() throws Exception {
        // Given
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("创建用户时如果用户名已存在应该返回400错误")
    void shouldReturnBadRequestWhenCreatingUserWithExistingUsername() throws Exception {
        // Given
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Username already exists: testuser"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists: testuser"));
    }

    @Test
    @DisplayName("应该能够获取所有用户")
    void shouldGetAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("应该能够根据ID获取用户")
    void shouldGetUserById() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("获取不存在的用户应该返回404")
    void shouldReturnNotFoundWhenGettingNonExistentUser() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("应该能够根据用户名获取用户")
    void shouldGetUserByUsername() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("应该能够更新用户")
    void shouldUpdateUser() throws Exception {
        // Given
        User updatedUser = new User("updateduser", "newpassword", "updated@example.com");
        updatedUser.setId(1L);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("更新不存在的用户应该返回404")
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
        // Given
        when(userService.updateUser(eq(1L), any(User.class)))
                .thenThrow(new RuntimeException("User not found with id: 1"));

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpected(status().isNotFound())
                .andExpect(content().string("User not found with id: 1"));
    }

    @Test
    @DisplayName("应该能够删除用户")
    void shouldDeleteUser() throws Exception {
        // Given
        // Since delete method returns void, we don't need to mock anything

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除不存在的用户应该返回404")
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        // Given
        doThrow(new RuntimeException("User not found with id: 1"))
                .when(userService).deleteUser(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: 1"));
    }

    @Test
    @DisplayName("应该能够切换用户状态")
    void shouldToggleUserStatus() throws Exception {
        // Given
        testUser.setEnabled(false);
        when(userService.toggleUserStatus(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(patch("/api/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("应该能够获取所有启用的用户")
    void shouldGetAllEnabledUsers() throws Exception {
        // Given
        List<User> enabledUsers = Arrays.asList(testUser);
        when(userService.getAllEnabledUsers()).thenReturn(enabledUsers);

        // When & Then
        mockMvc.perform(get("/api/users/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].enabled").value(true));
    }
}