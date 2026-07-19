package com.hospital.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.auth.JwtUtils;
import com.hospital.common.Result;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public Result<Map<String, Long>> register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        String username = body.getOrDefault("username", phone);
        String email = body.get("email");
        String realName = body.get("realName");
        String genderStr = body.get("gender");

        if (phone == null || password == null) {
            return Result.fail(400, "参数错误");
        }

        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exist != null) {
            return Result.fail(400, "手机号已注册");
        }

        User user = new User();
        user.setUsername(username != null ? username : phone);
        user.setPhone(phone);
        user.setPassword(BCrypt.hashpw(password));
        user.setEmail(email);
        user.setRealName(realName);
        if (genderStr != null) {
            user.setGender(Integer.valueOf(genderStr));
        }
        user.setAvatar("img/default-avatar.png");
        user.setStatus(1);
        userMapper.insert(user);

        Map<String, Long> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        return Result.ok("注册成功", data);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            return Result.fail(400, "手机号或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId());

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("realName", user.getRealName());
        userInfo.put("avatar", user.getAvatar());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);
        return Result.ok("登录成功", data);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("phone", user.getPhone());
        data.put("realName", user.getRealName());
        return Result.ok(data);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            return Result.fail(400, "原密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);
        return Result.ok("密码修改成功", null);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok("退出成功", null);
    }
}
