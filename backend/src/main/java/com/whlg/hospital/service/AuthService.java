package com.whlg.hospital.service;

import com.whlg.hospital.dto.ChangePasswordRequest;
import com.whlg.hospital.dto.LoginRequest;
import com.whlg.hospital.dto.RegisterRequest;
import com.whlg.hospital.dto.SendCaptchaRequest;

import java.util.Map;

public interface AuthService {

    Map<String, Object> sendCaptcha(SendCaptchaRequest request);

    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);

    Map<String, Object> me();

    void changePassword(ChangePasswordRequest request);

    void logout(String authorization);
}