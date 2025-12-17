package com.dseven.rolepermission.common.feign;

import com.dseven.rolepermission.common.entity.SysUser;
import com.dseven.rolepermission.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", contextId = "remoteUserService")
public interface RemoteUserService {

    @GetMapping("/user/username/{username}")
    Result<SysUser> getByUsername(@PathVariable("username") String username);

    @PostMapping("/user/register")
    Result<Boolean> registerUser(@RequestBody SysUser user);

    @GetMapping("/user/exists/email")
    Result<Boolean> existsByEmail(@RequestParam("email") String email);

    @PutMapping("/user/update")
    Result<Boolean> updateUser(@RequestBody SysUser user);
}
