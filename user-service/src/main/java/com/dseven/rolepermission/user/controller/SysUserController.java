package com.dseven.rolepermission.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dseven.rolepermission.common.entity.SysUser;
import com.dseven.rolepermission.common.result.Result;
import com.dseven.rolepermission.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/username/{username}")
    public Result<SysUser> getByUsername(@PathVariable("username") String username) {
        return Result.success(userService.getByUsername(username));
    }

    @PostMapping("/register")
    public Result<Boolean> registerUser(@RequestBody SysUser user) {
        return Result.success(userService.createUser(user));
    }

    @GetMapping("/exists/email")
    public Result<Boolean> existsByEmail(@RequestParam("email") String email) {
        boolean exists = userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email)) > 0;
        return Result.success(exists);
    }

    @PutMapping("/update")
    public Result<Boolean> updateUser(@RequestBody SysUser user) {
        return Result.success(userService.updateById(user));
    }
}
