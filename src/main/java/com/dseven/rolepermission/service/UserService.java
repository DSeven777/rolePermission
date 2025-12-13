package com.dseven.rolepermission.service;

import com.dseven.rolepermission.entity.User;
import com.dseven.rolepermission.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 创建用户
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 根据ID获取用户
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 根据用户名获取用户
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 更新用户
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setEmail(userDetails.getEmail());
                    user.setPassword(userDetails.getPassword());
                    user.setEnabled(userDetails.getEnabled());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 删除用户
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // 启用/禁用用户
    public User toggleUserStatus(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(!user.getEnabled());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 获取所有启用的用户
    public List<User> getAllEnabledUsers() {
        return (List<User>) userRepository.findAllEnabledUsers();
    }
}