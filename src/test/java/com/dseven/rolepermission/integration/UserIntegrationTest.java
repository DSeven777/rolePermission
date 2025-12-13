package com.dseven.rolepermission.integration;

import com.dseven.rolepermission.entity.User;
import com.dseven.rolepermission.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll(); // 清理数据库
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("完整的用户 CRUD 流程测试")
    void testCompleteUserCrudFlow() throws Exception {
        // 1. 创建用户
        User newUser = new User("integrationuser", "password123", "integration@test.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        // 2. 验证用户已保存到数据库
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("integrationuser", users.get(0).getUsername());

        // 3. 获取所有用户
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("integrationuser"));

        // 4. 根据 ID 获取用户
        User savedUser = users.get(0);
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value("integrationuser"));

        // 5. 更新用户
        User updatedUser = new User("updateduser", "newpassword", "updated@test.com");
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // 6. 获取所有启用的用户
        mockMvc.perform(get("/api/users/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].enabled").value(true));

        // 7. 切换用户状态
        mockMvc.perform(patch("/api/users/{id}/toggle-status", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        // 8. 删除用户
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk());

        // 9. 验证用户已删除
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("测试创建重复用户")
    void testCreatingDuplicateUser() throws Exception {
        // Given
        User user = new User("duplicate", "password", "duplicate@test.com");
        userRepository.save(user);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试批量创建和查询")
    void testBulkCreationAndQuery() throws Exception {
        // Given
        User user1 = new User("user1", "pass1", "user1@test.com");
        User user2 = new User("user2", "pass2", "user2@test.com");

        // When - 创建多个用户
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // Then - 验证所有用户
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 根据用户名查询
        mockMvc.perform(get("/api/users/username/{username}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }
}