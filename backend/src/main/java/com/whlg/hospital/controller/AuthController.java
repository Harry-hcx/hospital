package com.whlg.hospital.controller;

import com.whlg.hospital.dto.ChangePasswordRequest;
import com.whlg.hospital.dto.LoginRequest;
import com.whlg.hospital.dto.RegisterRequest;
import com.whlg.hospital.dto.SendCaptchaRequest;
import com.whlg.hospital.service.AuthService;
import com.whlg.hospital.util.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sendCaptcha")
    public R<Map<String, Object>> sendCaptcha(@RequestBody SendCaptchaRequest request) {
        return R.ok(authService.sendCaptcha(request));
    }

    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authService.me());
    }

    @PostMapping("/change-password")
    public R<Object> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return R.ok();
    }

    @PostMapping("/logout")
    public R<Object> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return R.ok();
    }
}